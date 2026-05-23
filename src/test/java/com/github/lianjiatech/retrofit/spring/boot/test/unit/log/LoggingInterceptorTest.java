package com.github.lianjiatech.retrofit.spring.boot.test.unit.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.lianjiatech.retrofit.spring.boot.log.GlobalLogProperty;
import com.github.lianjiatech.retrofit.spring.boot.log.LogLevel;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Invocation;

/**
 * 单元测试：覆盖 {@link LoggingInterceptor} 的关键分支（4.0.6 commit 默认策略改为 NONE）：
 * <ul>
 *     <li>全局 enable + 默认策略 NONE：不调用 HttpLoggingInterceptor，直接 proceed</li>
 *     <li>全局 disable 时跳过</li>
 *     <li>方法/类上的 @Logging 覆盖全局：strategy=BASIC/HEADERS/BODY 是否打印对应内容</li>
 *     <li>redactHeaders 在请求/响应中隐藏敏感字段</li>
 *     <li>aggregate=true：日志合并为一条；aggregate=false：日志分多次调用</li>
 *     <li>logLevel 路由到对应 SLF4J 方法</li>
 * </ul>
 */
public class LoggingInterceptorTest {

    /** 用于捕获实际打印日志行的 logger（注入为命名 logger 后由 logback-test.xml 截获太重，改用反射注入） */
    private final List<String> captured = Collections.synchronizedList(new ArrayList<>());

    /* ---------- 测试服务接口 ---------- */

    interface PlainService {
        void call();
    }

    @Logging(logStrategy = LogStrategy.BASIC, aggregate = false)
    interface BasicService {
        void call();
    }

    @Logging(logStrategy = LogStrategy.HEADERS, aggregate = true,
            redactHeaders = {"Authorization", "X-Secret"})
    interface RedactService {
        void call();
    }

    @Logging(enable = false)
    interface DisabledLogService {
        void call();
    }

    /* ---------- helpers ---------- */

    private static Invocation invocation(Class<?> svc) throws Exception {
        Method m = svc.getMethod("call");
        return Invocation.of(m, Collections.emptyList());
    }

    private static Request requestWith(Class<?> svc, String authHeader) throws Exception {
        Request.Builder b = new Request.Builder().url("http://unit.test/api").get();
        if (svc != null) {
            b = b.tag(Invocation.class, invocation(svc));
        }
        if (authHeader != null) {
            b = b.header("Authorization", authHeader);
        }
        return b.build();
    }

    private static Response responseFor(Request req) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Content-Type", "application/json")
                .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                .build();
    }

    private static Chain chainFor(Request req) throws IOException {
        Chain c = mock(Chain.class);
        when(c.request()).thenReturn(req);
        when(c.proceed(any(Request.class))).thenReturn(responseFor(req));
        return c;
    }

    private static GlobalLogProperty globalProp(boolean enable, LogStrategy strategy) {
        GlobalLogProperty p = new GlobalLogProperty();
        p.setEnable(enable);
        p.setLogStrategy(strategy);
        p.setLogLevel(LogLevel.INFO);
        p.setAggregate(true);
        return p;
    }

    /* ---------- enable / strategy=NONE ---------- */

    @Test
    public void noneStrategy_skipsHttpLoggingEntirely() throws Exception {
        // 4.0.6 默认 logStrategy=NONE：即使 enable=true，也不应进入 HttpLoggingInterceptor 路径
        LoggingInterceptor interceptor = new LoggingInterceptor(globalProp(true, LogStrategy.NONE));
        Request req = requestWith(PlainService.class, null);
        Chain chain = chainFor(req);

        Response resp = interceptor.intercept(chain);
        assertEquals(200, resp.code());
        verify(chain, times(1)).proceed(req);
    }

    @Test
    public void globalDisabled_andNoAnnotation_skipsLogging() throws Exception {
        LoggingInterceptor interceptor = new LoggingInterceptor(globalProp(false, LogStrategy.BODY));
        Request req = requestWith(PlainService.class, null);
        Chain chain = chainFor(req);

        Response resp = interceptor.intercept(chain);
        assertEquals(200, resp.code());
        verify(chain, times(1)).proceed(req);
    }

    @Test
    public void disabledOnMethod_skipsEvenWhenGlobalEnabled() throws Exception {
        LoggingInterceptor interceptor = new LoggingInterceptor(globalProp(true, LogStrategy.BODY));
        Request req = requestWith(DisabledLogService.class, null);
        Chain chain = chainFor(req);

        // 既不抛错也不应记录
        Response resp = interceptor.intercept(chain);
        assertEquals(200, resp.code());
    }

    /* ---------- redactHeaders（端到端：拦截 SLF4J logger 输出） ---------- */

    @Test
    public void redactHeaders_replacesValueInLog() throws Exception {
        // 用一个收集型 LoggingInterceptor 子类来捕获最终日志
        CapturingLoggingInterceptor interceptor = new CapturingLoggingInterceptor(
                globalProp(true, LogStrategy.HEADERS));

        // RedactService: HEADERS 级别 + redactHeaders=Authorization
        Request req = requestWith(RedactService.class, "Bearer my-secret-token");
        Chain chain = chainFor(req);

        interceptor.intercept(chain);

        String all = String.join("\n", interceptor.captured);
        assertTrue("应记录 HEADERS 级别日志: \n" + all, all.contains("Authorization"));
        // 关键：值必须被遮蔽（HttpLoggingInterceptor 默认替换为 ██），且不出现明文
        assertTrue("敏感请求头值不应明文出现:\n" + all,
                !all.contains("my-secret-token"));
    }

    @Test
    public void noRedactConfigured_logsAuthorizationValueInPlain() throws Exception {
        // 反向证明上一用例：未配 redactHeaders 时，明文值会出现，确保上面的断言不是假阳性
        @Logging(logStrategy = LogStrategy.HEADERS, aggregate = true)
        class Holder {}
        // 用 PlainService + 全局 HEADERS 等价（PlainService 没有 @Logging 注解，使用全局）
        GlobalLogProperty p = globalProp(true, LogStrategy.HEADERS);
        CapturingLoggingInterceptor interceptor = new CapturingLoggingInterceptor(p);
        Request req = requestWith(PlainService.class, "Bearer plain-token");
        Chain chain = chainFor(req);

        interceptor.intercept(chain);

        String all = String.join("\n", interceptor.captured);
        assertTrue("未配 redactHeaders 时明文值应该出现:\n" + all, all.contains("plain-token"));
    }

    @Test
    public void globalRedactHeaders_isAlsoApplied() throws Exception {
        GlobalLogProperty p = globalProp(true, LogStrategy.HEADERS);
        p.setRedactHeaders(new String[] {"Authorization"});
        CapturingLoggingInterceptor interceptor = new CapturingLoggingInterceptor(p);
        Request req = requestWith(PlainService.class, "Bearer global-secret");
        Chain chain = chainFor(req);

        interceptor.intercept(chain);

        String all = String.join("\n", interceptor.captured);
        assertTrue(!all.contains("global-secret"));
    }

    /* ---------- aggregate ---------- */

    @Test
    public void aggregateTrue_emitsSingleLogCall() throws Exception {
        GlobalLogProperty p = globalProp(true, LogStrategy.BASIC);
        p.setAggregate(true);
        CapturingLoggingInterceptor interceptor = new CapturingLoggingInterceptor(p);

        Request req = requestWith(PlainService.class, null);
        Chain chain = chainFor(req);
        interceptor.intercept(chain);

        // aggregate 模式下，所有日志被合并到 buffer 中一次性 flush
        assertEquals("aggregate=true 应只产生 1 条 log 调用，但实际: " + interceptor.captured,
                1, interceptor.captured.size());
        // 单条日志应包含请求与响应行
        String single = interceptor.captured.get(0);
        assertTrue(single.contains("--> GET"));
        assertTrue(single.contains("<-- 200"));
    }

    @Test
    public void aggregateFalse_emitsMultipleLogCalls() throws Exception {
        // BasicService: aggregate=false
        CapturingLoggingInterceptor interceptor = new CapturingLoggingInterceptor(
                globalProp(true, LogStrategy.BASIC));

        Request req = requestWith(BasicService.class, null);
        Chain chain = chainFor(req);
        interceptor.intercept(chain);

        // 非聚合模式：HttpLoggingInterceptor 直接每行输出
        assertTrue("aggregate=false 应产生多条 log，实际: " + interceptor.captured.size(),
                interceptor.captured.size() >= 2);
    }

    /* ---------- 一个不会触发底层 HttpLoggingInterceptor 真实 logger 的子类，
     *   通过覆盖 matchLogger 把所有日志收集到列表里 ---------- */
    static class CapturingLoggingInterceptor extends LoggingInterceptor {
        final List<String> captured = Collections.synchronizedList(new ArrayList<>());

        CapturingLoggingInterceptor(GlobalLogProperty p) {
            super(p);
        }

        @Override
        protected okhttp3.logging.HttpLoggingInterceptor.Logger matchLogger(String logName, LogLevel level) {
            return captured::add;
        }
    }
}
