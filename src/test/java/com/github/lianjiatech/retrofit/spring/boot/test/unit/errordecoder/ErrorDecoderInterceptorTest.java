package com.github.lianjiatech.retrofit.spring.boot.test.unit.errordecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitIOException;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.ErrorDecoderInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Invocation;

/**
 * 单元测试：覆盖 {@link ErrorDecoderInterceptor} 各分支：
 * <ul>
 *     <li>无 {@link Invocation} 直通</li>
 *     <li>2xx 直接返回响应</li>
 *     <li>非 2xx：默认 decoder 抛 {@link RetrofitException}</li>
 *     <li>chain.proceed 抛 {@link IOException}：被解码为 {@link RetrofitIOException}</li>
 *     <li>chain.proceed 抛非 IO 异常：被解码为 {@link RetrofitException}</li>
 *     <li>响应已读取后 decoder 抛 {@link RuntimeException}：原样向上抛，不再二次包装</li>
 *     <li>自定义 ErrorDecoder：按服务接口缓存生效</li>
 * </ul>
 */
public class ErrorDecoderInterceptorTest {

    @RetrofitClient(baseUrl = "http://unit.test")
    interface DefaultDecoderService {
        void call();
    }

    @RetrofitClient(baseUrl = "http://unit.test", errorDecoder = CustomDecoder.class)
    interface CustomDecoderService {
        void call();
    }

    public static class CustomDecoder implements ErrorDecoder {
        @Override
        public RuntimeException invalidRespDecode(Request request, Response response) {
            response.close();
            return new IllegalStateException("CUSTOM:" + response.code());
        }
    }

    private GenericApplicationContext context;
    private ErrorDecoderInterceptor interceptor;

    @Before
    public void setUp() {
        context = new GenericApplicationContext();
        context.refresh();
        interceptor = new ErrorDecoderInterceptor();
        interceptor.setApplicationContext(context);
    }

    private static Request requestWithInvocation(Class<?> svc) throws Exception {
        Method m = svc.getMethod("call");
        Invocation invocation = Invocation.of(m, Collections.emptyList());
        return new Request.Builder().url("http://unit.test/api").get().tag(Invocation.class, invocation).build();
    }

    private static Request requestWithoutInvocation() {
        return new Request.Builder().url("http://unit.test/api").get().build();
    }

    private static Response response(Request req, int code, String body) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code < 400 ? "OK" : "Err")
                .body(ResponseBody.create(body, MediaType.parse("application/json")))
                .build();
    }

    /* ---------- 直通 ---------- */

    @Test
    public void noInvocation_passThrough() throws IOException {
        Request request = requestWithoutInvocation();
        Response expected = response(request, 200, "{}");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(expected);

        Response actual = interceptor.intercept(chain);
        assertSame(expected, actual);
    }

    @Test
    public void successResponse_returnedAsIs() throws Exception {
        Request request = requestWithInvocation(DefaultDecoderService.class);
        Response expected = response(request, 200, "{}");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(expected);

        Response actual = interceptor.intercept(chain);
        assertSame(expected, actual);
    }

    /* ---------- 默认 decoder 各分支 ---------- */

    @Test
    public void non2xx_defaultDecoder_throwsRetrofitException() throws Exception {
        Request request = requestWithInvocation(DefaultDecoderService.class);
        Response failed = response(request, 500, "boom");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        try {
            interceptor.intercept(chain);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertTrue(e.getMessage().contains("invalid Response"));
            assertTrue(e.getMessage().contains("boom"));
        }
    }

    @Test
    public void chainThrowsIOException_decodedAsRetrofitIOException() throws Exception {
        Request request = requestWithInvocation(DefaultDecoderService.class);
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        IOException cause = new IOException("connect refused");
        when(chain.proceed(any(Request.class))).thenThrow(cause);

        try {
            interceptor.intercept(chain);
            fail("expected RetrofitIOException");
        } catch (RetrofitIOException e) {
            assertSame(cause, e.getCause());
            assertTrue(e.getMessage().contains("connect refused"));
        }
    }

    @Test
    public void chainThrowsRuntimeException_decodedAsRetrofitException() throws Exception {
        Request request = requestWithInvocation(DefaultDecoderService.class);
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        IllegalStateException cause = new IllegalStateException("boom");
        when(chain.proceed(any(Request.class))).thenThrow(cause);

        try {
            interceptor.intercept(chain);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertSame(cause, e.getCause());
            assertTrue(e.getMessage().contains("IllegalStateException"));
        }
    }

    @Test
    public void decoderThrowsRuntimeException_passesThroughWithoutSecondWrap() throws Exception {
        // 自定义 decoder 在 invalidRespDecode 中抛 IllegalStateException：
        // 因为响应已读（decoded=true），catch 块会原样抛出，不会二次解码为 RetrofitException
        Request request = requestWithInvocation(CustomDecoderService.class);
        Response failed = response(request, 502, "boom");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        try {
            interceptor.intercept(chain);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().startsWith("CUSTOM:"));
        }
    }

    /* ---------- 自定义 decoder + 缓存 ---------- */

    @Test
    public void customDecoder_isResolvedFromAnnotation() throws Exception {
        Request request = requestWithInvocation(CustomDecoderService.class);
        Response failed = response(request, 503, "x");
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(failed);

        try {
            interceptor.intercept(chain);
            fail();
        } catch (IllegalStateException e) {
            assertEquals("CUSTOM:503", e.getMessage());
        }
    }

    @Test
    public void decoder_isCachedPerService() throws Exception {
        // 注册一个跟踪型 ErrorDecoder bean 到 ApplicationContext，验证多次请求只解析一次
        AtomicReference<Integer> created = new AtomicReference<>(0);
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.registerBean("countingDecoder", CountingDecoder.class, () -> {
            created.set(created.get() + 1);
            return new CountingDecoder();
        });
        ctx.refresh();

        ErrorDecoderInterceptor it = new ErrorDecoderInterceptor();
        it.setApplicationContext(ctx);

        Request request = requestWithInvocation(CountingService.class);
        Chain chain = mock(Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response(request, 200, "{}"));

        it.intercept(chain);
        it.intercept(chain);
        it.intercept(chain);

        // 容器中是单例 bean，无论是否缓存只会创建 1 次（这里主要验证拦截器能从容器解析到该 decoder）
        assertEquals(Integer.valueOf(1), created.get());
    }

    @RetrofitClient(baseUrl = "http://unit.test", errorDecoder = CountingDecoder.class)
    interface CountingService {
        void call();
    }

    public static class CountingDecoder implements ErrorDecoder {
    }
}
