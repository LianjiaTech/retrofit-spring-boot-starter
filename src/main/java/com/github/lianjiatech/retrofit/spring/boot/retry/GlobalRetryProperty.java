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
     * 重试时间间隔
     * Retry interval
     */
    private int intervalMs = 100;

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     * Retry rule
     */

    private RetryRule[] retryRules = {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};
}
