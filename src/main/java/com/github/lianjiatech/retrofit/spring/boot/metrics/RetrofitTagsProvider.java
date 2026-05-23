package com.github.lianjiatech.retrofit.spring.boot.metrics;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Retrofit 指标标签生成 SPI。用户可通过 Spring 容器替换默认实现，定制指标的 tag 维度。
 *
 * <p>实现需要保证：
 * <ul>
 *     <li>同一方法（无论是否成功）产生的 tag 顺序与名称一致，避免 Micrometer 在不同场景下创建多个 Meter；</li>
 *     <li>tag 的取值集合应有界，避免基数爆炸（例如不要把动态 path 参数、query 参数放进 tag）。</li>
 * </ul>
 *
 * @author 陈添明
 */
public interface RetrofitTagsProvider {

    /**
     * 根据请求与可选响应生成 tags。
     *
     * @param request   原始 OkHttp 请求
     * @param response  响应；为 null 表示请求未拿到响应（IO 异常 / 取消等）
     * @param exception 当请求抛出异常时非空
     * @return 该次调用的 tag 集合
     */
    Tags getTags(Request request, Response response, Throwable exception);

    /**
     * 进行中请求的 tag。LongTaskTimer 不知道结果，因此只能给出"静态"维度。
     */
    default Tags getInProgressTags(Request request) {
        return Tags.of(getTags(request, null, null).stream()
                .filter(t -> !"status".equals(t.getKey()) && !"outcome".equals(t.getKey()))
                .toArray(Tag[]::new));
    }
}
