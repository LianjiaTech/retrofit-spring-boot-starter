package com.github.lianjiatech.retrofit.spring.boot.test.unit.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;

/**
 * 单元测试：覆盖 {@link BasePathMatchInterceptor} 的 include / exclude 路径匹配逻辑。
 */
public class BasePathMatchInterceptorTest {

    private static final class CountingInterceptor extends BasePathMatchInterceptor {
        final AtomicInteger doInterceptCalls = new AtomicInteger();

        @Override
        protected Response doIntercept(Chain chain) throws IOException {
            doInterceptCalls.incrementAndGet();
            return chain.proceed(chain.request());
        }
    }

    private static Request request(String url) {
        return new Request.Builder().url(url).get().build();
    }

    private static Response defaultResponse(Request req) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                .build();
    }

    private static Chain chainFor(Request req) throws IOException {
        Chain c = mock(Chain.class);
        when(c.request()).thenReturn(req);
        when(c.proceed(req)).thenReturn(defaultResponse(req));
        return c;
    }

    @Test
    public void noIncludeNoExclude_skipsDoIntercept() throws Exception {
        // include / exclude 都为空 => 全部跳过 doIntercept
        CountingInterceptor it = new CountingInterceptor();
        Request req = request("http://h/api/anything");
        Chain chain = chainFor(req);

        Response resp = it.intercept(chain);
        assertEquals(200, resp.code());
        assertEquals(0, it.doInterceptCalls.get());
        verify(chain, times(1)).proceed(req);
    }

    @Test
    public void includeMatched_invokesDoIntercept() throws Exception {
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/**"});
        Request req = request("http://h/api/users/1");
        Chain chain = chainFor(req);

        it.intercept(chain);
        assertEquals(1, it.doInterceptCalls.get());
    }

    @Test
    public void includeNotMatched_skipsDoIntercept() throws Exception {
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/**"});
        Request req = request("http://h/internal/health");
        Chain chain = chainFor(req);

        it.intercept(chain);
        assertEquals(0, it.doInterceptCalls.get());
    }

    @Test
    public void excludeTakesPrecedenceOverInclude() throws Exception {
        // 同时命中 include 与 exclude 时，exclude 优先
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/**"});
        it.setExclude(new String[] {"/api/auth/**"});

        Request req = request("http://h/api/auth/login");
        Chain chain = chainFor(req);

        it.intercept(chain);
        assertEquals("命中 exclude 时不应执行 doIntercept", 0, it.doInterceptCalls.get());
    }

    @Test
    public void excludeNotMatched_andIncludeMatched_invokesDoIntercept() throws Exception {
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/**"});
        it.setExclude(new String[] {"/api/auth/**"});

        Request req = request("http://h/api/users/1");
        Chain chain = chainFor(req);

        it.intercept(chain);
        assertEquals(1, it.doInterceptCalls.get());
    }

    @Test
    public void multiplePatterns_anyMatchTriggers() throws Exception {
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/**", "/v2/**"});

        // 命中第二个
        Request req = request("http://h/v2/foo");
        Chain chain = chainFor(req);
        it.intercept(chain);
        assertEquals(1, it.doInterceptCalls.get());

        // 都不命中
        Request req2 = request("http://h/other/bar");
        Chain chain2 = chainFor(req2);
        it.intercept(chain2);
        assertEquals("第二次未命中：doIntercept 计数不变", 1, it.doInterceptCalls.get());
    }

    @Test
    public void singleStarMatchesOneSegmentOnly() throws Exception {
        // AntPathMatcher: '*' 仅匹配单段
        CountingInterceptor it = new CountingInterceptor();
        it.setInclude(new String[] {"/api/*"});

        Request matched = request("http://h/api/users");
        Chain c1 = chainFor(matched);
        it.intercept(c1);
        assertEquals(1, it.doInterceptCalls.get());

        Request notMatched = request("http://h/api/users/1");
        Chain c2 = chainFor(notMatched);
        it.intercept(c2);
        assertEquals("跨段路径不应被 /api/* 命中", 1, it.doInterceptCalls.get());
    }
}
