package com.github.lianjiatech.retrofit.spring.boot.metrics;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.*;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 基于 Micrometer 的指标采集拦截器。
 *
 * <p>采集的指标（前缀由配置项 {@code retrofit.metrics.metric-name-prefix} 控制，默认 {@code retrofit.client}）：
 * <ul>
 *     <li>{@code retrofit.client.requests} — Timer，每次 HTTP 调用的耗时与状态分桶；</li>
 *     <li>{@code retrofit.client.requests.active} — LongTaskTimer，进行中的请求数与最长存活时间；</li>
 *     <li>{@code retrofit.client.errors} — Counter，请求异常计数（按 exception 类名分组）。</li>
 * </ul>
 *
 * <p><b>位置约束</b>：作为 OkHttp 应用拦截器（{@code addInterceptor}），位于
 * {@code RetryInterceptor} 之后、{@code LoggingInterceptor} 之前。这样：
 * <ul>
 *     <li>每次 HTTP 尝试都会被独立计入 timer，重试不会污染单次请求的耗时；</li>
 *     <li>日志看到的耗时与 metrics 报告一致；</li>
 *     <li>降级 / 错误解码 / 服务发现链路上的延迟也被计入。</li>
 * </ul>
 *
 * <p><b>异常处理</b>：拦截器永远不会因为指标采集失败而吞掉业务异常 —— 任何 Micrometer 内部错误都会被
 * 静默忽略（debug 级别可观测），原始 IO 异常 / 业务异常照常上抛。
 *
 * @author 陈添明
 */
public class MetricsInterceptor implements Interceptor {

    private static final String REQUESTS_METER = "requests";
    private static final String ACTIVE_METER = "requests.active";
    private static final String ERRORS_METER = "errors";

    private final MeterRegistry registry;

    private final RetrofitTagsProvider tagsProvider;

    private final MetricsProperty property;

    private final String requestsMetric;
    private final String activeMetric;
    private final String errorsMetric;

    public MetricsInterceptor(MeterRegistry registry, RetrofitTagsProvider tagsProvider, MetricsProperty property) {
        this.registry = registry;
        this.tagsProvider = tagsProvider;
        this.property = property;
        String prefix = property.getMetricNamePrefix();
        this.requestsMetric = prefix + "." + REQUESTS_METER;
        this.activeMetric = prefix + "." + ACTIVE_METER;
        this.errorsMetric = prefix + "." + ERRORS_METER;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startNanos = System.nanoTime();

        // LongTaskTimer 必须在请求开始前 start，结束时 stop。任何阶段抛错都需要在 finally 释放。
        LongTaskTimer.Sample activeSample = startActive(request);

        Response response = null;
        Throwable error = null;
        try {
            response = chain.proceed(request);
            return response;
        } catch (IOException | RuntimeException e) {
            error = e;
            throw e;
        } finally {
            stopActive(activeSample);
            try {
                recordTimer(request, response, error, System.nanoTime() - startNanos);
                if (error != null) {
                    recordError(request, error);
                }
            } catch (RuntimeException meterError) {
                // 指标采集是辅助能力，不应影响业务。Micrometer 自身错误（极少）静默忽略。
            }
        }
    }

    private LongTaskTimer.Sample startActive(Request request) {
        try {
            LongTaskTimer ltt = LongTaskTimer.builder(activeMetric)
                    .description("Active Retrofit client requests")
                    .tags(tagsProvider.getInProgressTags(request))
                    .register(registry);
            return ltt.start();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void stopActive(LongTaskTimer.Sample sample) {
        if (sample == null) {
            return;
        }
        try {
            sample.stop();
        } catch (RuntimeException ignored) {
            // 同 start，不影响主流程。
        }
    }

    private void recordTimer(Request request, Response response, Throwable error, long durationNanos) {
        Tags tags = tagsProvider.getTags(request, response, error);
        Timer.Builder builder = Timer.builder(requestsMetric)
                .description("Retrofit client request duration")
                .tags(tags);
        double[] percentiles = property.getPercentiles();
        if (percentiles != null && percentiles.length > 0) {
            builder.publishPercentiles(percentiles);
        }
        java.time.Duration[] sla = property.getSla();
        if (sla != null && sla.length > 0) {
            builder.serviceLevelObjectives(sla);
        }
        builder.register(registry).record(durationNanos, TimeUnit.NANOSECONDS);
    }

    private void recordError(Request request, Throwable error) {
        Tags base = tagsProvider.getTags(request, null, error);
        // 加一个 exception 类名维度，便于区分超时 / 连接被拒等不同 IO 失败模式；类名集合受 JDK / OkHttp 限定，可控。
        Tags tags = base.and("exception", error.getClass().getSimpleName());
        Counter.builder(errorsMetric)
                .description("Retrofit client request errors")
                .tags(tags)
                .register(registry)
                .increment();
    }
}
