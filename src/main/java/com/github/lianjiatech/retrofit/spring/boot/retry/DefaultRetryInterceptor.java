package com.github.lianjiatech.retrofit.spring.boot.retry;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author 陈添明
 */
public class DefaultRetryInterceptor extends BaseRetryInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(DefaultRetryInterceptor.class);

    @Override
    protected Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain) {
        HashSet<RetryRule> retryRuleSet = (HashSet<RetryRule>) Arrays.stream(retryRules).collect(Collectors.toSet());
        RetryStrategy retryStrategy = new RetryStrategy(maxRetries, intervalMs);
        while (true) {
            try {
                Request request = chain.request();
                Response response = chain.proceed(request);
                // 如果响应状态码是2xx就不用重试，直接返回 response
                if (!retryRuleSet.contains(RetryRule.RESPONSE_STATUS_NOT_2XX) || response.isSuccessful()) {
                    return response;
                } else {
                    if (!retryStrategy.shouldRetry()) {
                        // 最后一次还没成功，返回最后一次response
                        return response;
                    }
                    // 执行重试
                    retryStrategy.retry();
                    logger.debug("The response fails, retry is performed! The response code is " + response.code());
                    response.close();
                }
            } catch (Exception e) {
                if (shouldThrowEx(retryRuleSet, e)) {
                    throw new RuntimeException(e);
                } else {
                    if (!retryStrategy.shouldRetry()) {
                        // 最后一次还没成功，抛出异常
                        throw new RuntimeException("Retry Failed: Total " + maxRetries
                                + " attempts made at interval " + intervalMs
                                + "ms");
                    }
                    retryStrategy.retry();
                }
            }
        }
    }

    private boolean shouldThrowEx(HashSet<RetryRule> retryRuleSet, Exception e) {
        if (retryRuleSet.contains(RetryRule.OCCUR_EXCEPTION)) {
            return false;
        }
        if (retryRuleSet.contains(RetryRule.OCCUR_IO_EXCEPTION)) {
            return !(e instanceof IOException);
        }
        return true;
    }

}
