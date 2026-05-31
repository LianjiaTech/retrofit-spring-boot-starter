package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 重试策略：支持固定间隔（FIXED）和指数退避（EXPONENTIAL），并可叠加随机抖动（jitter）。
 * <p>
 * Retry strategy: supports fixed interval (FIXED) and exponential backoff (EXPONENTIAL),
 * with optional random jitter.
 *
 * @author 陈添明
 */
class RetryStrategy {

    private int remainingRetries;
    private final int baseIntervalMs;
    private final int maxIntervalMs;
    private final BackoffStrategy backoffStrategy;
    private final double jitter;
    private int attempt;

    RetryStrategy(int maxRetries, int baseIntervalMs, int maxIntervalMs, BackoffStrategy backoffStrategy,
            double jitter) {
        this.remainingRetries = maxRetries;
        this.baseIntervalMs = baseIntervalMs;
        this.maxIntervalMs = maxIntervalMs;
        this.backoffStrategy = backoffStrategy == null ? BackoffStrategy.FIXED : backoffStrategy;
        this.jitter = Math.max(0.0, Math.min(1.0, jitter));
        this.attempt = 0;
    }

    public boolean shouldRetry() {
        return remainingRetries > 0;
    }

    public void retry() {
        remainingRetries--;
        waitUntilNextTry();
        attempt++;
    }

    private void waitUntilNextTry() {
        try {
            Thread.sleep(calculateDelay());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 计算本次重试前的等待时间。
     * <ul>
     *     <li>EXPONENTIAL：min(base * 2^attempt, maxIntervalMs)，attempt 从 0 起（首次重试即 base）</li>
     *     <li>FIXED：base</li>
     * </ul>
     * 若 jitter &gt; 0，再叠加 delay * (1 + jitter * random[0,1))。
     */
    long calculateDelay() {
        long delay;
        if (backoffStrategy == BackoffStrategy.EXPONENTIAL) {
            // 用 double 计算指数，避免 long 溢出，再封顶到 maxIntervalMs
            double exponential = baseIntervalMs * Math.pow(2, attempt);
            delay = (long) Math.min(exponential, maxIntervalMs);
        } else {
            delay = baseIntervalMs;
        }
        if (jitter > 0.0) {
            double randomFactor = ThreadLocalRandom.current().nextDouble();
            delay = (long) (delay * (1.0 + jitter * randomFactor));
        }
        return delay;
    }
}
