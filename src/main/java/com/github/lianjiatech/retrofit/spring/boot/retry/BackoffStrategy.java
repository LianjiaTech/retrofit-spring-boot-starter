package com.github.lianjiatech.retrofit.spring.boot.retry;

/**
 * 退避策略
 * Backoff strategy
 *
 * @author 陈添明
 */
public enum BackoffStrategy {

    /**
     * 固定间隔：每次重试间隔相同
     * Fixed interval: constant delay between retries
     */
    FIXED,

    /**
     * 指数退避：延迟按 base * 2^attempt 递增，封顶 maxIntervalMs
     * Exponential backoff: delay grows as base * 2^attempt, capped at maxIntervalMs
     */
    EXPONENTIAL,
}
