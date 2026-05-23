package com.github.lianjiatech.retrofit.spring.boot.test.unit.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetryFailedException;
import com.github.lianjiatech.retrofit.spring.boot.retry.GlobalRetryProperty;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Invocation;

/**
 * 纯单元测试：覆盖 {@link RetryInterceptor} 的关键分支：
 * <ul>
 *     <li>无 {@link Invocation} tag 时直通</li>
 *     <li>{@link RetryRule#RESPONSE_STATUS_NOT_2XX} 命中、不命中、穷尽</li>
 *     <li>{@link RetryRule#OCCUR_IO_EXCEPTION} / {@link RetryRule#OCCUR_EXCEPTION} 的命中与传递</li>
 *     <li>方法级 {@link Retry} 覆盖全局配置</li>
 *     <li>失败响应资源关闭、间隔生效</li>
 * </ul>
 */
public class RetryInterceptorTest {

    /** 仅用于触发默认 @Retry 行为：retryRules = {NOT_2XX, IO_EXCEPTION}，maxRetries=2，intervalMs=100 */
    @Retry
    interface DefaultRetryService {
        void call();
    }

    interface PlainService {
        void call();
    }

    @Retry(enable = false)
    interface DisabledRetryService {
        void call();
    }

    @Retry(maxRetries = 3, intervalMs = 5, retryRules = RetryRule.RESPONSE_STATUS_NOT_2XX)
    interface OnlyStatusRetryService {
        void call();
    }

    @Retry(maxRetries = 3, intervalMs = 5, retryRules = RetryRule.OCCUR_IO_EXCEPTION)
    interface OnlyIoRetryService {
        void call();
    }

    @Retry(maxRetries = 3, intervalMs = 5, retryRules = RetryRule.OCCUR_EXCEPTION)
    interface OccurExceptionRetryService {
        void call();
    }

    @Retry(maxRetries = 2, intervalMs = 50, retryRules = RetryRule.RESPONSE_STATUS_NOT_2XX)
    interface IntervalService {
        void call();
    }

    /* ---------- 通用辅助 ---------- */

    private static GlobalRetryProperty disabledGlobal() {
        GlobalRetryProperty p = new GlobalRetryProperty();
        p.setEnable(false);
        return p;
    }

    private static GlobalRetryProperty enabledGlobal() {
        GlobalRetryProperty p = new GlobalRetryProperty();
        p.setEnable(true);
        p.setMaxRetries(2);
        p.setIntervalMs(5);
        p.setRetryRules(new RetryRule[] {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION});
        return p;
    }

    private static Request requestWithInvocation(Invocation invocation) {
        Request.Builder b = new Request.Builder().url("http://unit.test/retry").get();
        if (invocation != null) {
            b = b.tag(Invocation.class, invocation);
        }
        return b.build();
    }

    private static Invocation invocation(Class<?> svc) throws Exception {
        Method m = svc.getMethod("call");
        return Invocation.of(m, Collections.emptyList());
    }

    /** 可记录 close() 调用次数的响应体 */
    private static final class TrackingResponseBody extends ResponseBody {
        final AtomicBoolean closed = new AtomicBoolean(false);
        private final Buffer buffer;

        TrackingResponseBody(String body) {
            this.buffer = new Buffer().writeUtf8(body);
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse("application/json");
        }

        @Override
        public long contentLength() {
            return buffer.size();
        }

        @Override
        public BufferedSource source() {
            return buffer;
        }

        @Override
        public void close() {
            closed.set(true);
            super.close();
        }
    }

    private static Response response(Request req, int code, TrackingResponseBody body) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code == 200 ? "OK" : "Err")
                .body(body)
                .build();
    }

    /* ---------- 直通分支 ---------- */

    @Test
    public void noInvocationTag_proceedsDirectly_noRetry() throws IOException {
        RetryInterceptor interceptor = new RetryInterceptor(enabledGlobal());
        Request request = requestWithInvocation(null); // 无 Invocation tag
        TrackingResponseBody body = new TrackingResponseBody("{}");
        Response expected = response(request, 500, body);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(expected);

        Response actual = interceptor.intercept(chain);

        assertSame(expected, actual);
        verify(chain, times(1)).proceed(any(Request.class));
    }

    @Test
    public void globalDisabledAndNoAnnotation_noRetry_evenOn500() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(PlainService.class));
        TrackingResponseBody body = new TrackingResponseBody("err");
        Response failed = response(request, 500, body);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        Response actual = interceptor.intercept(chain);
        assertSame(failed, actual);
        verify(chain, times(1)).proceed(any(Request.class));
    }

    @Test
    public void retryDisabledOnMethod_overridesEnabledGlobal_noRetry() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(enabledGlobal());
        Request request = requestWithInvocation(invocation(DisabledRetryService.class));
        TrackingResponseBody body = new TrackingResponseBody("err");
        Response failed = response(request, 500, body);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        Response actual = interceptor.intercept(chain);
        assertSame(failed, actual);
        verify(chain, times(1)).proceed(any(Request.class));
    }

    /* ---------- RESPONSE_STATUS_NOT_2XX ---------- */

    @Test
    public void successResponse_2xx_returnsImmediately() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyStatusRetryService.class));
        TrackingResponseBody body = new TrackingResponseBody("{}");
        Response ok = response(request, 200, body);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(ok);

        Response actual = interceptor.intercept(chain);
        assertSame(ok, actual);
        verify(chain, times(1)).proceed(any(Request.class));
        // 成功响应未被关闭，应交由调用方消费
        assertTrue("成功响应不应被拦截器关闭", !body.closed.get());
    }

    @Test
    public void status5xx_thenSuccess_returnsSuccess_andClosesFailedResponses() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyStatusRetryService.class));

        TrackingResponseBody fail1 = new TrackingResponseBody("err1");
        TrackingResponseBody fail2 = new TrackingResponseBody("err2");
        TrackingResponseBody okBody = new TrackingResponseBody("{}");
        Response r1 = response(request, 500, fail1);
        Response r2 = response(request, 500, fail2);
        Response ok = response(request, 200, okBody);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(r1, r2, ok);

        Response actual = interceptor.intercept(chain);

        assertSame(ok, actual);
        verify(chain, times(3)).proceed(any(Request.class));
        // 关键：失败响应在重试前应被关闭，避免连接泄漏
        assertTrue("第一次失败响应应被关闭", fail1.closed.get());
        assertTrue("第二次失败响应应被关闭", fail2.closed.get());
        assertTrue("最终成功响应不应被关闭", !okBody.closed.get());
    }

    @Test
    public void status5xx_exhausted_returnsLastFailedResponse() throws Exception {
        // OnlyStatusRetryService: maxRetries=3，全部失败 -> 共调用 1+3=4 次，最终返回最后一次失败响应
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyStatusRetryService.class));

        TrackingResponseBody[] bodies = {
                new TrackingResponseBody("e1"),
                new TrackingResponseBody("e2"),
                new TrackingResponseBody("e3"),
                new TrackingResponseBody("e4"),
        };
        Response[] resps = {
                response(request, 500, bodies[0]),
                response(request, 500, bodies[1]),
                response(request, 500, bodies[2]),
                response(request, 500, bodies[3]),
        };

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(resps[0], resps[1], resps[2], resps[3]);

        Response actual = interceptor.intercept(chain);

        // 穷尽时不抛异常（仅 RESPONSE_STATUS_NOT_2XX 规则下），返回最后一次响应交业务处理
        assertSame(resps[3], actual);
        verify(chain, times(4)).proceed(any(Request.class));
        // 前 3 次失败的响应都应被关闭，最后一次保留给调用方
        assertTrue(bodies[0].closed.get());
        assertTrue(bodies[1].closed.get());
        assertTrue(bodies[2].closed.get());
        assertTrue("最后一次响应应交给调用方，不应在拦截器关闭", !bodies[3].closed.get());
    }

    @Test
    public void status5xx_butStatusRuleMissing_doesNotRetry() throws Exception {
        // 全局启用，但规则只配 OCCUR_IO_EXCEPTION，5xx 响应不在规则集里：直接返回，不重试
        GlobalRetryProperty p = new GlobalRetryProperty();
        p.setEnable(true);
        p.setMaxRetries(3);
        p.setIntervalMs(5);
        p.setRetryRules(new RetryRule[] {RetryRule.OCCUR_IO_EXCEPTION});
        RetryInterceptor interceptor = new RetryInterceptor(p);

        Request request = requestWithInvocation(invocation(PlainService.class));
        TrackingResponseBody body = new TrackingResponseBody("err");
        Response failed = response(request, 500, body);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        Response actual = interceptor.intercept(chain);
        assertSame(failed, actual);
        verify(chain, times(1)).proceed(any(Request.class));
    }

    /* ---------- IOException 路径 ---------- */

    @Test
    public void ioException_thenSuccess_returnsSuccess() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyIoRetryService.class));
        TrackingResponseBody okBody = new TrackingResponseBody("{}");
        Response ok = response(request, 200, okBody);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request))
                .thenThrow(new IOException("first"))
                .thenThrow(new IOException("second"))
                .thenReturn(ok);

        Response actual = interceptor.intercept(chain);

        assertSame(ok, actual);
        verify(chain, times(3)).proceed(any(Request.class));
    }

    @Test
    public void ioException_exhausted_throwsRetryFailedException() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyIoRetryService.class));

        IOException last = new IOException("last");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request))
                .thenThrow(new IOException("1"))
                .thenThrow(new IOException("2"))
                .thenThrow(new IOException("3"))
                .thenThrow(last);

        try {
            interceptor.intercept(chain);
            fail("应抛出 RetryFailedException");
        } catch (RetryFailedException e) {
            assertSame("cause 应保留最后一次原始异常", last, e.getCause());
            assertTrue(e.getMessage().contains("Retry Failed"));
        }
        // maxRetries=3 -> 1 + 3 = 4 次
        verify(chain, times(4)).proceed(any(Request.class));
    }

    @Test
    public void nonIoException_withOnlyIoRule_doesNotRetry_rethrowsOriginal() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OnlyIoRetryService.class));

        IllegalStateException boom = new IllegalStateException("boom");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenThrow(boom);

        try {
            interceptor.intercept(chain);
            fail("应抛出 IllegalStateException");
        } catch (IllegalStateException e) {
            assertSame(boom, e);
        }
        // 只配了 OCCUR_IO_EXCEPTION 规则，非 IOException 不应重试
        verify(chain, times(1)).proceed(any(Request.class));
    }

    @Test
    public void nonIoException_withOccurExceptionRule_isRetried() throws Exception {
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(OccurExceptionRetryService.class));
        TrackingResponseBody okBody = new TrackingResponseBody("{}");
        Response ok = response(request, 200, okBody);

        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request))
                .thenThrow(new IllegalStateException("transient"))
                .thenReturn(ok);

        Response actual = interceptor.intercept(chain);

        assertSame(ok, actual);
        verify(chain, times(2)).proceed(any(Request.class));
    }

    /* ---------- intervalMs ---------- */

    @Test
    public void intervalMs_isRespected() throws Exception {
        // maxRetries=2, intervalMs=50 => 至少有 2 次 sleep(50)
        RetryInterceptor interceptor = new RetryInterceptor(disabledGlobal());
        Request request = requestWithInvocation(invocation(IntervalService.class));

        AtomicInteger n = new AtomicInteger();
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenAnswer(new Answer<Response>() {
            @Override
            public Response answer(InvocationOnMock inv) {
                int i = n.incrementAndGet();
                int code = i < 3 ? 500 : 200;
                return response(request, code, new TrackingResponseBody("x"));
            }
        });

        long t0 = System.nanoTime();
        Response actual = interceptor.intercept(chain);
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

        assertEquals(200, actual.code());
        // 2 次重试至少 sleep 100ms，留点抖动余量取 80ms 阈值
        assertTrue("intervalMs 应生效，实际耗时=" + elapsedMs + "ms", elapsedMs >= 80);
    }
}
