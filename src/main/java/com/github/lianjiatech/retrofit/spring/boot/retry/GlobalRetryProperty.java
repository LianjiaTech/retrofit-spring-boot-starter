package com.github.lianjiatech.retrofit.spring.boot.retry;

import lombok.Data;

/**
 * 全局重试配置
 * @author 陈添明
 */
@Data
public class GlobalRetryProperty {

    /**
     * 是否启用全局重试，启用的话，所有HTTP请求都会自动重试。
     * 否则的话，只有被 {@link com.github.lianjiatech.retrofit.spring.boot.retry.Retry}标注的接口才会执行重试。
     * 接口上Retry注解属性优先于全局配置。
     */
    private boolean enable = false;

    /**
     * 最大重试次数
     * The maximum number of retries
     */
    private int maxRetries = 2;

    /**
     * 重试基础间隔时间（毫秒）。FIXED 为固定间隔；EXPONENTIAL 为首次重试基础间隔。
     * Base retry interval in milliseconds.
     */
    private int intervalMs = 100;

    /**
     * 退避策略，默认 FIXED（固定间隔）
     * Backoff strategy, default FIXED
     */
    private BackoffStrategy backoffStrategy = BackoffStrategy.FIXED;

    /**
     * 最大退避间隔上限（毫秒），仅 EXPONENTIAL 生效
     * Maximum backoff interval cap in milliseconds, only applies to EXPONENTIAL
     */
    private int maxIntervalMs = 30000;

    /**
     * 抖动系数 [0.0, 1.0]，0.0 表示无抖动
     * Jitter factor in range [0.0, 1.0], 0.0 means no jitter
     */
    private double jitter = 0.0;

    /**
     * 触发重试的具体 HTTP 状态码列表，空表示任意非2xx都触发
     * Status codes that trigger retry, empty means any non-2xx triggers
     */
    private int[] retryStatusCodes = {};

    /**
     * 触发重试的异常类型列表，空表示匹配 RetryRule 的任意异常都触发
     * Exception classes that trigger retry, empty means any exception matching RetryRule triggers
     */
    private Class<? extends Throwable>[] retryExceptionClasses = new Class[] {};

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     * Retry rule
     */
    private RetryRule[] retryRules = {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};
}
