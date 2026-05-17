package com.github.lianjiatech.retrofit.spring.boot.retry;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetryFailedException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
@Slf4j
public class RetryInterceptor implements Interceptor {

    protected final GlobalRetryProperty globalRetryProperty;

    public RetryInterceptor(GlobalRetryProperty globalRetryProperty) {
        this.globalRetryProperty = globalRetryProperty;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        // 获取重试配置
        Retry retry = AnnotationExtendUtils.findMergedAnnotation(invocation.method(), invocation.service(), Retry.class);
        if (!needRetry(retry)) {
            return chain.proceed(request);
        }
        // 重试
        int maxRetries = retry == null ? globalRetryProperty.getMaxRetries() : retry.maxRetries();
        int intervalMs = retry == null ? globalRetryProperty.getIntervalMs() : retry.intervalMs();
        RetryRule[] retryRules = retry == null ? globalRetryProperty.getRetryRules() : retry.retryRules();
        return retryIntercept(maxRetries, intervalMs, retryRules, chain);
    }

    protected boolean needRetry(Retry retry) {
        if (globalRetryProperty.isEnable()) {
            if (retry == null) {
                return true;
            }
            return retry.enable();
        } else {
            return retry != null && retry.enable();
        }
    }

    protected Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain)
            throws IOException {
        Set<RetryRule> retryRuleSet = toRetryRuleSet(retryRules);
        RetryStrategy retryStrategy = new RetryStrategy(maxRetries, intervalMs);
        Request request = chain.request();
        while (true) {
            Response response = null;
            try {
                response = chain.proceed(request);
                // 如果响应状态码是2xx就不用重试，直接返回 response
                if (!retryRuleSet.contains(RetryRule.RESPONSE_STATUS_NOT_2XX) || response.isSuccessful()) {
                    return response;
                }
                if (!retryStrategy.shouldRetry()) {
                    // 最后一次还没成功，返回最后一次response
                    return response;
                }
                // 即将重试：先释放当前响应资源
                response.close();
                response = null;
                retryStrategy.retry();
                log.warn("The response fails, retry is performed! The request is {}", request);
            } catch (Exception e) {
                // 失败路径：异常发生时 chain.proceed 内部已释放连接，response 一定为 null；
                // 仍兜底关闭以防自定义实现遗漏。
                if (response != null) {
                    response.close();
                }
                if (shouldThrowEx(retryRuleSet, e)) {
                    rethrowWithoutRetry(e);
                }
                if (!retryStrategy.shouldRetry()) {
                    // 最后一次还没成功，抛出异常
                    throw new RetryFailedException(
                            "Retry Failed: Total " + maxRetries + " attempts made at interval " + intervalMs + "ms",
                            e);
                }
                retryStrategy.retry();
                log.warn("The response fails, retry is performed! The request is {} ", request, e);
            }
        }
    }

    protected boolean shouldThrowEx(Set<RetryRule> retryRuleSet, Exception e) {
        if (retryRuleSet.contains(RetryRule.OCCUR_EXCEPTION)) {
            return false;
        }
        if (retryRuleSet.contains(RetryRule.OCCUR_IO_EXCEPTION)) {
            return !(e instanceof IOException);
        }
        return true;
    }

    private static Set<RetryRule> toRetryRuleSet(RetryRule[] retryRules) {
        if (retryRules == null || retryRules.length == 0) {
            return EnumSet.noneOf(RetryRule.class);
        }
        return EnumSet.copyOf(Arrays.asList(retryRules));
    }

    /**
     * 不重试时直接向上传递：保留 {@link IOException} 与 {@link RuntimeException}，其它受检异常再包装。
     */
    private static void rethrowWithoutRetry(Exception e) throws IOException {
        if (e instanceof IOException) {
            throw (IOException)e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        }
        throw new RuntimeException(e);
    }

}
