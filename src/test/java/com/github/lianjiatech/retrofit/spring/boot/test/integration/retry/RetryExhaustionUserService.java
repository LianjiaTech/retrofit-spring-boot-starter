package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 穷尽场景下使用的 service：
 * <ul>
 *     <li>{@link #getUserStatusRetry}：仅 RESPONSE_STATUS_NOT_2XX 规则</li>
 *     <li>{@link #getUserIoRetry}：仅 OCCUR_IO_EXCEPTION 规则</li>
 * </ul>
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", connectTimeoutMs = 200, readTimeoutMs = 200, writeTimeoutMs = 200)
public interface RetryExhaustionUserService {

    @GET("getUser")
    @Retry(intervalMs = 5, maxRetries = 2, retryRules = RetryRule.RESPONSE_STATUS_NOT_2XX)
    User getUserStatusRetry(@Query("id") Long id);

    @GET("getUser")
    @Retry(intervalMs = 5, maxRetries = 2, retryRules = RetryRule.OCCUR_IO_EXCEPTION)
    User getUserIoRetry(@Query("id") Long id);
}
