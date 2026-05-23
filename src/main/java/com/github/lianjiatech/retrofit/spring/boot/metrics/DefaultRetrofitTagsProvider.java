package com.github.lianjiatech.retrofit.spring.boot.metrics;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * 默认 {@link RetrofitTagsProvider} 实现。
 *
 * <p>tag 维度（按生成顺序）：
 * <ol>
 *     <li>{@code client}: Retrofit 接口的简单类名，如 {@code UserService}</li>
 *     <li>{@code method}: Java 方法名</li>
 *     <li>{@code uri}: Retrofit 注解上的 path 模板（保留占位符 {@code {id}}），未声明时为 {@code NONE}；可通过配置关闭</li>
 *     <li>{@code http.method}: HTTP 方法，如 {@code GET}/{@code POST}</li>
 *     <li>{@code status}: 状态码桶（{@code 2xx}/{@code 3xx}/{@code 4xx}/{@code 5xx}/{@code IO_ERROR}/{@code NONE}）</li>
 *     <li>{@code outcome}: 业务结果（{@code SUCCESS}/{@code REDIRECTION}/{@code CLIENT_ERROR}/{@code SERVER_ERROR}/{@code IO_ERROR}/{@code UNKNOWN}）</li>
 *     <li>{@code host}: 仅当配置启用时输出</li>
 *     <li>用户配置的 {@code extra-tags} 全部追加</li>
 * </ol>
 *
 * <p><b>基数控制</b>：所有 tag 取值都是有界集合或派生自接口/方法的静态信息，不展开 {@code @Path} 占位符，
 * 避免 Micrometer 报告维度无限增长。
 *
 * @author 陈添明
 */
public class DefaultRetrofitTagsProvider implements RetrofitTagsProvider {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String NONE = "NONE";

    private final MetricsProperty property;

    /**
     * 缓存 (Method -> uri 模板)。Method 引用稳定且数量受 Retrofit 接口规模限制，无需弱引用。
     */
    private final ConcurrentMap<Method, String> uriCache = new ConcurrentHashMap<>(64);

    public DefaultRetrofitTagsProvider(MetricsProperty property) {
        this.property = property;
    }

    @Override
    public Tags getTags(Request request, Response response, Throwable exception) {
        Invocation invocation = request.tag(Invocation.class);
        String client = invocation == null ? UNKNOWN : invocation.service().getSimpleName();
        String method = invocation == null ? UNKNOWN : invocation.method().getName();

        // tag 顺序对 Micrometer 来说不影响，但保持稳定有助于调试与文档。
        Tags tags = Tags.of(
                Tag.of("client", client),
                Tag.of("method", method),
                Tag.of("http.method", request.method()),
                Tag.of("status", statusBucket(response, exception)),
                Tag.of("outcome", outcome(response, exception))
        );

        if (property.getTags().isUri()) {
            tags = tags.and(Tag.of("uri", uriTemplate(invocation)));
        }
        if (property.getTags().isHost()) {
            tags = tags.and(Tag.of("host", request.url().host()));
        }
        Map<String, String> extra = property.safeExtraTags();
        if (!extra.isEmpty()) {
            for (Map.Entry<String, String> e : extra.entrySet()) {
                tags = tags.and(Tag.of(e.getKey(), e.getValue()));
            }
        }
        return tags;
    }

    private String statusBucket(Response response, Throwable exception) {
        if (response != null) {
            int code = response.code();
            if (code >= 200 && code < 300) {
                return "2xx";
            }
            if (code >= 300 && code < 400) {
                return "3xx";
            }
            if (code >= 400 && code < 500) {
                return "4xx";
            }
            if (code >= 500 && code < 600) {
                return "5xx";
            }
            return NONE;
        }
        if (exception != null) {
            return "IO_ERROR";
        }
        return NONE;
    }

    private String outcome(Response response, Throwable exception) {
        if (response != null) {
            int code = response.code();
            if (code < 200) {
                return UNKNOWN;
            }
            if (code < 300) {
                return "SUCCESS";
            }
            if (code < 400) {
                return "REDIRECTION";
            }
            if (code < 500) {
                return "CLIENT_ERROR";
            }
            if (code < 600) {
                return "SERVER_ERROR";
            }
            return UNKNOWN;
        }
        if (exception != null) {
            return "IO_ERROR";
        }
        return UNKNOWN;
    }

    /**
     * 从 Retrofit 注解中提取 path 模板。优先解析 {@link GET}/{@link POST} 等内置注解的 {@code value()}，
     * 兜底使用 {@link HTTP#path()}。注解都不存在则返回 {@code NONE}（一般是 {@code @Url} 动态 URL 场景）。
     */
    private String uriTemplate(Invocation invocation) {
        if (invocation == null) {
            return NONE;
        }
        return uriCache.computeIfAbsent(invocation.method(), DefaultRetrofitTagsProvider::resolveUriTemplate);
    }

    private static String resolveUriTemplate(Method method) {
        // 按调用频率与匹配概率排序：GET/POST 最常见。
        for (Annotation annotation : method.getAnnotations()) {
            String value = readPath(annotation);
            if (value != null) {
                return value.isEmpty() ? NONE : value;
            }
        }
        return NONE;
    }

    private static String readPath(Annotation annotation) {
        if (annotation instanceof GET) {
            return ((GET)annotation).value();
        }
        if (annotation instanceof POST) {
            return ((POST)annotation).value();
        }
        if (annotation instanceof PUT) {
            return ((PUT)annotation).value();
        }
        if (annotation instanceof DELETE) {
            return ((DELETE)annotation).value();
        }
        if (annotation instanceof HEAD) {
            return ((HEAD)annotation).value();
        }
        if (annotation instanceof PATCH) {
            return ((PATCH)annotation).value();
        }
        if (annotation instanceof OPTIONS) {
            return ((OPTIONS)annotation).value();
        }
        if (annotation instanceof HTTP) {
            return ((HTTP)annotation).path();
        }
        return null;
    }

    /**
     * 仅暴露给测试使用，验证缓存工作正常。
     */
    public Map<Method, String> uriCacheView() {
        return new LinkedHashMap<>(uriCache);
    }
}
