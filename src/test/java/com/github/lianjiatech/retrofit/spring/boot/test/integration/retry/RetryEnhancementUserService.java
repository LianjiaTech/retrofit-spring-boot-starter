package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import java.net.SocketTimeoutException;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.retry.BackoffStrategy;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 重试增强场景下使用的 service：
 * <ul>
 *     <li>{@link #getUserExponential}：EXPONENTIAL 退避 + jitter</li>
 *     <li>{@link #getUserStatusCode503}：仅 503 状态码触发重试</li>
 *     <li>{@link #getUserOnlyTimeout}：仅 SocketTimeoutException 触发重试</li>
 * </ul>
 *
 * @author 陈添明
 */
@Timeout(connectTimeoutMs = 200, readTimeoutMs = 200, writeTimeoutMs = 200)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface RetryEnhancementUserService {

    @GET("getUser")
    @Retry(maxRetries = 3, intervalMs = 10, maxIntervalMs = 50, backoffStrategy = BackoffStrategy.EXPONENTIAL,
            jitter = 0.2, retryRules = RetryRule.RESPONSE_STATUS_NOT_2XX)
    User getUserExponential(@Query("id") Long id);

    @GET("getUser")
    @Retry(maxRetries = 3, intervalMs = 5, retryStatusCodes = {503},
            retryRules = RetryRule.RESPONSE_STATUS_NOT_2XX)
    User getUserStatusCode503(@Query("id") Long id);

    @GET("getUser")
    @Retry(maxRetries = 2, intervalMs = 5, retryExceptionClasses = {SocketTimeoutException.class},
            retryRules = RetryRule.OCCUR_IO_EXCEPTION)
    User getUserOnlyTimeout(@Query("id") Long id);
}
