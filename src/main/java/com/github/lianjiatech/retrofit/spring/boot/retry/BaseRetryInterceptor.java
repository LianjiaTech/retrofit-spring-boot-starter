package com.github.lianjiatech.retrofit.spring.boot.retry;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;

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
        Retry retry;
        if (method.isAnnotationPresent(Retry.class)) {
            retry = method.getAnnotation(Retry.class);
        } else {
            Class<?> declaringClass = method.getDeclaringClass();
            retry = declaringClass.getAnnotation(Retry.class);
        }

        // 没有@Retry 且未开启全局重试
        if (retry == null && !enableGlobalRetry) {
            return chain.proceed(request);
        }
        // 重试
        int maxRetries = retry == null ? globalMaxRetries : retry.maxRetries();
        int intervalMs = retry == null ? globalIntervalMs : retry.intervalMs();
        RetryRule[] retryRules = retry == null ? globalRetryRules : retry.retryRules();
        // 最多重试10次
        maxRetries = maxRetries > LIMIT_RETRIES ? LIMIT_RETRIES : maxRetries;
        try {
            return retryIntercept(maxRetries, intervalMs, retryRules, chain);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    protected abstract Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain) throws IOException, InterruptedException;


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
