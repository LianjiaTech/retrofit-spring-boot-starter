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


    private static final int LIMIT_RETRIES = 10;

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
        if (retry == null) {
            // 不用重试
            return chain.proceed(request);
        }
        // 重试
        int maxRetries = retry.maxRetries();
        int intervalMs = retry.intervalMs();
        RetryRule[] retryRules = retry.retryRules();
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
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    protected abstract Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain) throws IOException, InterruptedException;

}
