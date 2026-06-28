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
        Retry retry =
                AnnotationExtendUtils.findMergedAnnotation(invocation.method(), invocation.service(), Retry.class);
        if (!needRetry(retry)) {
            return chain.proceed(request);
        }
        // 重试配置：注解优先于全局
        int maxRetries = retry == null ? globalRetryProperty.getMaxRetries() : retry.maxRetries();
        int intervalMs = retry == null ? globalRetryProperty.getIntervalMs() : retry.intervalMs();
        int maxIntervalMs = retry == null ? globalRetryProperty.getMaxIntervalMs() : retry.maxIntervalMs();
        BackoffStrategy backoffStrategy =
                retry == null ? globalRetryProperty.getBackoffStrategy() : retry.backoffStrategy();
        double jitter = retry == null ? globalRetryProperty.getJitter() : retry.jitter();
        RetryRule[] retryRules = retry == null ? globalRetryProperty.getRetryRules() : retry.retryRules();
        int[] retryStatusCodes = retry == null ? globalRetryProperty.getRetryStatusCodes() : retry.retryStatusCodes();
        Class<? extends Throwable>[] retryExceptionClasses =
                retry == null ? globalRetryProperty.getRetryExceptionClasses() : retry.retryExceptionClasses();
        return retryIntercept(maxRetries, intervalMs, maxIntervalMs, backoffStrategy, jitter, retryRules,
                retryStatusCodes, retryExceptionClasses, chain);
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

    protected Response retryIntercept(int maxRetries, int intervalMs, int maxIntervalMs,
            BackoffStrategy backoffStrategy,
            double jitter, RetryRule[] retryRules, int[] retryStatusCodes,
            Class<? extends Throwable>[] retryExceptionClasses, Chain chain) throws IOException {
        Set<RetryRule> retryRuleSet = toRetryRuleSet(retryRules);
        RetryStrategy retryStrategy = new RetryStrategy(maxRetries, intervalMs, maxIntervalMs, backoffStrategy, jitter);
        Request request = chain.request();
        while (true) {
            Response response = null;
            try {
                response = chain.proceed(request);
                // 状态码不在重试范围内（2xx，或未命中 retryStatusCodes 条件），直接返回 response
                if (!shouldRetryOnStatus(retryRuleSet, retryStatusCodes, response)) {
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
                if (shouldThrowEx(retryRuleSet, retryExceptionClasses, e)) {
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

    /**
     * 判断当前响应是否应触发重试。
     * <p>
     * 先要求命中 {@link RetryRule#RESPONSE_STATUS_NOT_2XX} 且响应非2xx；
     * 当 {@code retryStatusCodes} 非空时，进一步收窄为仅当状态码命中列表才重试。
     * @param retryRuleSet      已激活的重试规则集合
     * @param retryStatusCodes  需要重试的 HTTP 状态码列表
     * @param response          当前 HTTP 响应
     * @return 是否应触发重试
     */
    protected boolean shouldRetryOnStatus(Set<RetryRule> retryRuleSet, int[] retryStatusCodes, Response response) {
        if (!retryRuleSet.contains(RetryRule.RESPONSE_STATUS_NOT_2XX) || response.isSuccessful()) {
            return false;
        }
        if (retryStatusCodes != null && retryStatusCodes.length > 0) {
            return contains(retryStatusCodes, response.code());
        }
        return true;
    }

    protected boolean shouldThrowEx(Set<RetryRule> retryRuleSet, Class<? extends Throwable>[] retryExceptionClasses,
            Exception e) {
        // 先按 RetryRule 粗粒度判定是否属于可重试异常
        boolean ruleMatched;
        if (retryRuleSet.contains(RetryRule.OCCUR_EXCEPTION)) {
            ruleMatched = true;
        } else if (retryRuleSet.contains(RetryRule.OCCUR_IO_EXCEPTION)) {
            ruleMatched = e instanceof IOException;
        } else {
            ruleMatched = false;
        }
        if (!ruleMatched) {
            return true;
        }
        // retryExceptionClasses 非空时进一步收窄：仅当异常命中列表才重试
        if (retryExceptionClasses != null && retryExceptionClasses.length > 0) {
            return !isInstanceOfAny(retryExceptionClasses, e);
        }
        return false;
    }

    private static boolean isInstanceOfAny(Class<? extends Throwable>[] classes, Throwable e) {
        for (Class<? extends Throwable> clazz : classes) {
            if (clazz != null && clazz.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(int[] codes, int code) {
        for (int c : codes) {
            if (c == code) {
                return true;
            }
        }
        return false;
    }

    private static Set<RetryRule> toRetryRuleSet(RetryRule[] retryRules) {
        if (retryRules == null || retryRules.length == 0) {
            return EnumSet.noneOf(RetryRule.class);
        }
        return EnumSet.copyOf(Arrays.asList(retryRules));
    }

    /**
     * 不重试时直接向上传递：保留 {@link IOException} 与 {@link RuntimeException}，其它受检异常再包装。
     * @param e 需要传递的异常
     * @throws IOException 当异常为 IOException 或其子类时直接抛出
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
