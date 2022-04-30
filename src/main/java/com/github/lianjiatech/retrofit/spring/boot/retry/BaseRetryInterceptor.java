package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.io.IOException;
import java.lang.reflect.Method;

import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import lombok.Data;
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
@Data
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
     * 重试拦截
     *
     * @param maxRetries 最大重试次数。Maximum number of retries
     * @param intervalMs 重试时间间隔。Retry interval
     * @param retryRules 重试规则。Retry rules
     * @param chain      执行链。Execution chain
     * @return 请求响应。Response
     */
    protected abstract Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain);
}
