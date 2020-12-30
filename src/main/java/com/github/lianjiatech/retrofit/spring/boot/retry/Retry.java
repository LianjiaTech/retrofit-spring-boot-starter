package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Retry {

    /**
     * 最大重试次数，最大可设置为100
     * The maximum number of retries, the maximum can be set to 100
     *
     * @return 最大重试次数；The maximum number of retries
     */
    int maxRetries() default 2;

    /**
     * 重试时间间隔
     * Retry interval
     *
     * @return 重试时间间隔；Retry interval
     */
    int intervalMs() default 100;

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     * Retry rule
     * @return Retry rule
     */
    RetryRule[] retryRules() default {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};
}
