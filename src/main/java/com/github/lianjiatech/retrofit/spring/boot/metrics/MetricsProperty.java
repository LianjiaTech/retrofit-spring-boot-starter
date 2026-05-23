package com.github.lianjiatech.retrofit.spring.boot.metrics;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * Micrometer 指标采集配置。
 *
 * <p>仅当类路径存在 {@code io.micrometer.core.instrument.MeterRegistry} 且 Spring 容器中存在
 * {@code MeterRegistry} Bean 时，相关自动配置才会生效。该开关用于在以上前提具备时显式关闭指标采集。
 *
 * @author 陈添明
 */
@Data
public class MetricsProperty {

    /**
     * 是否启用指标采集。默认 true：在 MeterRegistry 可用的前提下开箱即用。
     * 若用户希望即使引入了 Micrometer 也不采集 Retrofit 指标，可显式设为 false。
     */
    private boolean enable = true;

    /**
     * Timer 发布的分位数，默认 P50/P95/P99。设为空数组表示不发布分位数。
     */
    private double[] percentiles = {0.5, 0.95, 0.99};

    /**
     * Timer 的 SLO（服务等级目标）边界，用于直方图分桶。默认包含常见的请求耗时分桶。
     * 设为空数组表示不发布直方图。
     */
    private Duration[] sla = {
            Duration.ofMillis(50),
            Duration.ofMillis(100),
            Duration.ofMillis(300),
            Duration.ofSeconds(1),
            Duration.ofSeconds(3)
    };

    /**
     * 标签控制开关。
     */
    private Tags tags = new Tags();

    /**
     * 全局静态附加标签，会被加到所有 Retrofit 指标上。常用于注入应用名、环境等。
     */
    private Map<String, String> extraTags = Collections.emptyMap();

    /**
     * 指标名前缀。默认 {@code retrofit.client}，最终生成 {@code retrofit.client.requests} 等指标。
     * 多客户端隔离时可改为不同前缀。
     */
    private String metricNamePrefix = "retrofit.client";

    @Data
    public static class Tags {

        /**
         * 是否带 host 标签。默认关闭以控制基数（动态 baseUrl 场景下 host 数量可能很大）。
         */
        private boolean host = false;

        /**
         * 是否带 uri 标签。默认开启。uri 取自 Retrofit 注解上的路径模板（不展开 {@code @Path}），
         * 因此基数受方法数量约束，可控。
         */
        private boolean uri = true;
    }

    /**
     * 内部使用：将用户配置的 extraTags 转为可预测顺序的副本，避免外部修改。
     */
    public Map<String, String> safeExtraTags() {
        return extraTags == null || extraTags.isEmpty() ? Collections.emptyMap() : new LinkedHashMap<>(extraTags);
    }
}
