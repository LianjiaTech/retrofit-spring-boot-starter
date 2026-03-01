package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import lombok.Data;

/**
 * @author chentianming
 * @since 2026/3/1
 */
@Data
public class SentinelDegradeRuleProperty {

    /**
     * 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
     * <p>
     * Circuit breaking strategy (0: average RT, 1: exception ratio, 2: exception count).
     */
    private int grade = 0;

    /**
     * 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
     * <p>
     * Threshold count. The exact meaning depends on the field of grade.
     * 1. In average RT mode, it means the maximum response time(RT) in milliseconds.
     * 2. In exception ratio mode, it means exception ratio which between 0.0 and 1.0.
     * 3. In exception count mode, it means exception count
     */
    private double count = 1000;

    /**
     * 熔断时长，单位为 s
     * <p>
     * Recovery timeout (in seconds) when circuit breaker opens. After the timeout, the circuit breaker will
     * transform to half-open state for trying a few requests.
     */
    private int timeWindow = 5;


    /**
     * （在有效统计时间范围内）能够触发熔断的最小请求数
     * <p>
     * Minimum number of requests (in an active statistic time span) that can trigger circuit breaking.
     *
     */
    private int minRequestAmount = 5;

    /**
     * RT 模式下慢请求率的阈值
     * <p>
     * The threshold of slow request ratio in RT mode.
     *
     */
    private double slowRatioThreshold = 1.0d;

    /**
     * 时间间隔统计持续时间，单位为毫秒
     * <p>
     * The interval statistics duration in millisecond.
     *
     */
    private int statIntervalMs = 1000;
}
