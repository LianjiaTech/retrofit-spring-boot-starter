package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.lianjiatech.retrofit.spring.boot.metrics.DefaultRetrofitTagsProvider;
import com.github.lianjiatech.retrofit.spring.boot.metrics.MetricsProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Invocation;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;

/**
 * {@link DefaultRetrofitTagsProvider} 单元测试。
 *
 * @author 陈添明
 */
public class DefaultRetrofitTagsProviderTest {

    interface SampleApi {
        @GET("user/{id}")
        String getUser(long id);

        @POST("user/create")
        String createUser();

        @HTTP(method = "DELETE", path = "user/del", hasBody = true)
        String deleteUser();

        @GET
        String dynamicUrl();
    }

    private static Method method(String name) {
        for (Method m : SampleApi.class.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalArgumentException(name);
    }

    private static Request requestFor(String url, String httpMethod, Method m) {
        Request.Builder b = new Request.Builder()
                .url(url)
                .method(httpMethod, httpMethod.equals("GET") || httpMethod.equals("DELETE")
                        ? null : okhttp3.RequestBody.create(new byte[0]));
        if (m != null) {
            // 用 4 参数版的 Invocation.of：需要传 service Class、实例与 method、args。
            // 测试中不会真正调用 instance，传 dummy 即可。
            @SuppressWarnings("unchecked")
            Class<SampleApi> svc = (Class<SampleApi>)m.getDeclaringClass();
            b.tag(Invocation.class, Invocation.of(svc, DUMMY_API, m, java.util.Collections.emptyList()));
        }
        return b.build();
    }

    private static final SampleApi DUMMY_API = (SampleApi)java.lang.reflect.Proxy.newProxyInstance(
            SampleApi.class.getClassLoader(),
            new Class[] {SampleApi.class},
            (proxy, method, args) -> {
                throw new UnsupportedOperationException("dummy");
            });

    private static Response responseWith(int status, Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(status)
                .message("OK")
                .body(ResponseBody.create(new byte[0], null))
                .build();
    }

    @Test
    public void successResponseTagsAreComplete() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://api.example.com/user/1", "GET", method("getUser"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);

        assertEquals("SampleApi", tagValue(tags, "client"));
        assertEquals("getUser", tagValue(tags, "method"));
        assertEquals("GET", tagValue(tags, "http.method"));
        assertEquals("2xx", tagValue(tags, "status"));
        assertEquals("SUCCESS", tagValue(tags, "outcome"));
        // 默认带 uri，不带 host
        assertEquals("user/{id}", tagValue(tags, "uri"));
        assertNull(tagValue(tags, "host"));
    }

    @Test
    public void statusBucketsCoverAllRanges() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));

        assertEquals("2xx", tagValue(provider.getTags(req, responseWith(204, req), null), "status"));
        assertEquals("3xx", tagValue(provider.getTags(req, responseWith(301, req), null), "status"));
        assertEquals("4xx", tagValue(provider.getTags(req, responseWith(404, req), null), "status"));
        assertEquals("5xx", tagValue(provider.getTags(req, responseWith(503, req), null), "status"));
        assertEquals("REDIRECTION", tagValue(provider.getTags(req, responseWith(301, req), null), "outcome"));
        assertEquals("CLIENT_ERROR", tagValue(provider.getTags(req, responseWith(404, req), null), "outcome"));
        assertEquals("SERVER_ERROR", tagValue(provider.getTags(req, responseWith(503, req), null), "outcome"));
    }

    @Test
    public void exceptionTagsUseIoErrorBucket() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));
        Tags tags = provider.getTags(req, null, new IOException("connect refused"));

        assertEquals("IO_ERROR", tagValue(tags, "status"));
        assertEquals("IO_ERROR", tagValue(tags, "outcome"));
    }

    @Test
    public void uriTagDisabledByConfig() {
        MetricsProperty property = new MetricsProperty();
        property.getTags().setUri(false);
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(property);
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);

        assertNull(tagValue(tags, "uri"));
    }

    @Test
    public void hostTagEnabledByConfig() {
        MetricsProperty property = new MetricsProperty();
        property.getTags().setHost(true);
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(property);
        Request req = requestFor("http://api.example.com/user/1", "GET", method("getUser"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);

        assertEquals("api.example.com", tagValue(tags, "host"));
    }

    @Test
    public void extraTagsAreAppended() {
        MetricsProperty property = new MetricsProperty();
        Map<String, String> extra = new HashMap<>();
        extra.put("app", "demo");
        extra.put("env", "prod");
        property.setExtraTags(extra);

        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(property);
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);

        assertEquals("demo", tagValue(tags, "app"));
        assertEquals("prod", tagValue(tags, "env"));
    }

    @Test
    public void invocationMissingFallsBackToUnknown() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/1", "GET", null);
        Tags tags = provider.getTags(req, responseWith(200, req), null);

        assertEquals("UNKNOWN", tagValue(tags, "client"));
        assertEquals("UNKNOWN", tagValue(tags, "method"));
        assertEquals("NONE", tagValue(tags, "uri"));
    }

    @Test
    public void httpAnnotationPathIsResolved() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/del", "DELETE", method("deleteUser"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);
        assertEquals("user/del", tagValue(tags, "uri"));
    }

    @Test
    public void emptyPathFallsBackToNone() {
        // @GET 没有 path 值（典型 @Url 动态 URL 场景），应回退到 NONE
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/dynamic", "GET", method("dynamicUrl"));
        Tags tags = provider.getTags(req, responseWith(200, req), null);
        assertEquals("NONE", tagValue(tags, "uri"));
    }

    @Test
    public void uriTemplateIsCachedAcrossCalls() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));
        provider.getTags(req, responseWith(200, req), null);
        provider.getTags(req, responseWith(500, req), null);
        // 同一个 Method 应只在缓存中出现一次
        assertEquals(1, provider.uriCacheView().size());
    }

    @Test
    public void inProgressTagsExcludeDynamicDimensions() {
        DefaultRetrofitTagsProvider provider = new DefaultRetrofitTagsProvider(new MetricsProperty());
        Request req = requestFor("http://x/user/1", "GET", method("getUser"));
        Tags tags = provider.getInProgressTags(req);

        // status / outcome 不应出现在 LongTaskTimer 的 tag 中（因为还没结束）
        assertNull(tagValue(tags, "status"));
        assertNull(tagValue(tags, "outcome"));
        // 静态维度仍在
        assertEquals("SampleApi", tagValue(tags, "client"));
        assertEquals("getUser", tagValue(tags, "method"));
    }

    @Test
    public void httpUrlBuiltCorrectly() {
        // 哨兵：保证测试 helper 没有 URL 解析异常
        assertTrue(HttpUrl.parse("http://x/user/1") != null);
    }

    private static String tagValue(Tags tags, String key) {
        for (Tag t : tags) {
            if (t.getKey().equals(key)) {
                return t.getValue();
            }
        }
        return null;
    }
}
