package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.DefaultRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;

/**
 * @author 陈添明
 */
public class RetryProperty {

    /**
     * 是否启用全局重试，启用的话，所有HTTP请求都会自动重试。
     * 否则的话，只有被 {@link com.github.lianjiatech.retrofit.spring.boot.retry.Retry}标注的接口才会执行重试。
     * 接口上Retry注解属性优先于全局配置。
     */
    private boolean enableGlobalRetry = true;

    /**
     * 全局最大重试次数，最大可设置为100
     * The maximum number of retries, the maximum can be set to 100
     */
    private int globalMaxRetries = 2;

    /**
     * 全局重试时间间隔
     * Retry interval
     */
    private int globalIntervalMs = 100;

    /**
     * 重试规则，默认 响应状态码不是2xx 或者 发生IO异常 时触发重试
     * Retry rule
     */

    private RetryRule[] globalRetryRules = {RetryRule.RESPONSE_STATUS_NOT_2XX, RetryRule.OCCUR_IO_EXCEPTION};


    /**
     * retry interceptor
     */
    private Class<? extends BaseRetryInterceptor> retryInterceptor = DefaultRetryInterceptor.class;


    public boolean isEnableGlobalRetry() {
        return enableGlobalRetry;
    }

    public void setEnableGlobalRetry(boolean enableGlobalRetry) {
        this.enableGlobalRetry = enableGlobalRetry;
    }

    public int getGlobalMaxRetries() {
        return globalMaxRetries;
    }

    public void setGlobalMaxRetries(int globalMaxRetries) {
        this.globalMaxRetries = globalMaxRetries;
    }

    public int getGlobalIntervalMs() {
        return globalIntervalMs;
    }

    public void setGlobalIntervalMs(int globalIntervalMs) {
        this.globalIntervalMs = globalIntervalMs;
    }

    public RetryRule[] getGlobalRetryRules() {
        return globalRetryRules;
    }

    public void setGlobalRetryRules(RetryRule[] globalRetryRules) {
        this.globalRetryRules = globalRetryRules;
    }

    public Class<? extends BaseRetryInterceptor> getRetryInterceptor() {
        return retryInterceptor;
    }

    public void setRetryInterceptor(Class<? extends BaseRetryInterceptor> retryInterceptor) {
        this.retryInterceptor = retryInterceptor;
    }
}
