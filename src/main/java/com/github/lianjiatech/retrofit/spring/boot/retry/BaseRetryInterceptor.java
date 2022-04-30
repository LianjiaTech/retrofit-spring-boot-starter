package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.io.IOException;
import java.lang.reflect.Method;

import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * 请求重试拦截器
 * Request retry interceptor
 *
 * @author 陈添明
 */
public abstract class BaseRetryInterceptor implements Interceptor {

    private boolean enableGlobalRetry;

    private int globalMaxRetries;

    private int globalIntervalMs;

    private RetryRule[] globalRetryRules;

    private static final int LIMIT_RETRIES = 100;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        assert invocation != null;
        Method method = invocation.method();
        // 获取重试配置
        Retry retry = AnnotationExtendUtils.findAnnotation(method, Retry.class);

        if (!needRetry(retry)) {
            return chain.proceed(request);
        }

        // 重试
        int maxRetries = retry == null ? globalMaxRetries : retry.maxRetries();
        int intervalMs = retry == null ? globalIntervalMs : retry.intervalMs();
        RetryRule[] retryRules = retry == null ? globalRetryRules : retry.retryRules();
        // 最多重试100次
        maxRetries = Math.min(maxRetries, LIMIT_RETRIES);
        return retryIntercept(maxRetries, intervalMs, retryRules, chain);

    }

    private boolean needRetry(Retry retry) {

        if (enableGlobalRetry) {
            // 开启全局重试的情况下
            // 没配置@Retry，需要重试
            if (retry == null) {
                return true;
            }
            // 配置了@Retry，enable==true，需要重试
            if (retry.enable()) {
                return true;
            }
        } else {
            // 未开启全局重试
            // 配置了@Retry，enable==true，需要重试
            if (retry != null && retry.enable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * process a retryable request
     * The access level here is set to protected, which can facilitate business personalized expansion
     * 这里访问级别设置为protected，可方便业务个性化扩展
     *
     * @param maxRetries 最大重试次数。Maximum number of retries
     * @param intervalMs 重试时间间隔。Retry interval
     * @param retryRules 重试规则。Retry rules
     * @param chain      执行链。Execution chain
     * @return 请求响应。Response
     */
    protected abstract Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain);


    public void setEnableGlobalRetry(boolean enableGlobalRetry) {
        this.enableGlobalRetry = enableGlobalRetry;
    }

    public void setGlobalMaxRetries(int globalMaxRetries) {
        this.globalMaxRetries = globalMaxRetries;
    }

    public void setGlobalIntervalMs(int globalIntervalMs) {
        this.globalIntervalMs = globalIntervalMs;
    }

    public void setGlobalRetryRules(RetryRule[] globalRetryRules) {
        this.globalRetryRules = globalRetryRules;
    }
}
