package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Retry {

    /**
     * 是否启用重试
     *
     * @return 是否启用重试
     */
    boolean enable() default true;

    /**
     * 最大重试次数，最大可设置为100
     * <p>
     * The maximum number of retries, the maximum can be set to 100
     *
     * @return 最大重试次数；The maximum number of retries
     */
    int maxRetries() default 2;

    /**
     * 重试基础间隔时间（毫秒）。
     * <ul>
     *     <li>FIXED 策略下为每次重试的固定间隔</li>
     *     <li>EXPONENTIAL 策略下为首次重试的基础间隔，后续按指数递增</li>
     * </ul>
     * <p>
     * Base retry interval in milliseconds.
     * <ul>
     *     <li>FIXED: constant interval between retries</li>
     *     <li>EXPONENTIAL: initial interval, multiplied by 2 on each subsequent retry</li>
     * </ul>
     *
     * @return 重试基础间隔；Base retry interval
     */
    int intervalMs() default 100;

    /**
     * 退避策略，默认 FIXED（固定间隔），保持向后兼容。
     * <p>
     * Backoff strategy, default FIXED (constant interval) for backward compatibility.
     *
     * @return 退避策略；Backoff strategy
     */
    BackoffStrategy backoffStrategy() default BackoffStrategy.FIXED;

    /**
     * 最大退避间隔上限（毫秒），仅在 EXPONENTIAL 策略下生效，防止间隔无限增长。
     * <p>
     * Maximum backoff interval cap in milliseconds. Only applies to EXPONENTIAL strategy
     * to prevent unbounded interval growth.
     *
     * @return 最大退避间隔；Maximum backoff interval
     */
    int maxIntervalMs() default 30000;

    /**
     * 抖动系数，取值范围 [0.0, 1.0]。0.0 表示无抖动（默认，向后兼容）。
     * <p>
     * 实际延迟 = 计算延迟 * (1 + jitter * random)，其中 random 为 [0, 1) 的随机数，
     * 用于避免多客户端同步重试导致的惊群效应。
     * <p>
     * Jitter factor in range [0.0, 1.0]. 0.0 means no jitter (default, backward compatible).
     * <p>
     * Actual delay = computed delay * (1 + jitter * random), where random is in [0, 1).
     * Helps avoid the thundering herd problem with synchronized retries.
     *
     * @return 抖动系数；Jitter factor
     */
    double jitter() default 0.0;

    /**
     * 触发重试的具体 HTTP 状态码列表。空数组（默认）表示任意非2xx状态码都触发重试，
     * 与历史行为一致；非空时仅在响应状态码命中列表时才重试。需配合 {@link RetryRule#RESPONSE_STATUS_NOT_2XX}。
     * <p>
     * 例如：{@code {502, 503, 504}} 表示仅在返回 502/503/504 时重试。
     * <p>
     * Specific HTTP status codes that trigger retry. Empty array (default) means any non-2xx
     * status code triggers retry (legacy behavior); when non-empty, only retries when the
     * response status code matches the list. Works with {@link RetryRule#RESPONSE_STATUS_NOT_2XX}.
     * <p>
     * Example: {@code {502, 503, 504}} — only retry on 502/503/504.
     *
     * @return 触发重试的状态码列表；Status codes that trigger retry
     */
    int[] retryStatusCodes() default {};

    /**
     * 触发重试的异常类型列表。空数组（默认）表示匹配 {@link RetryRule} 的任意异常都触发重试，
     * 与历史行为一致；非空时仅在异常类型命中列表时才重试。
     * <p>
     * 例如：{@code {java.net.SocketTimeoutException.class}} 表示仅在发生 SocketTimeoutException 时重试。
     * <p>
     * Exception classes that trigger retry. Empty array (default) means any exception matching
     * the configured {@link RetryRule} triggers retry (legacy behavior); when non-empty, only
     * retries when the exception is an instance of one of the listed classes.
     * <p>
     * Example: {@code {java.net.SocketTimeoutException.class}} — only retry on SocketTimeoutException.
     *
     * @return 触发重试的异常类型；Exception classes that trigger retry
     */
    Class<? extends Throwable>[] retryExceptionClasses() default {};

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     *
     * @return Retry rule
     */
    RetryRule[] retryRules() default {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};
}
