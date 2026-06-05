package com.github.lianjiatech.retrofit.spring.boot.test.integration.actuate;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogLevel;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 带接口级 {@code @Logging}/{@code @Retry} 注解、并显式覆盖超时的 client，用于验证 Actuator Endpoint
 * 对 {@code source="interface"} 与超时覆盖/继承的解析。
 *
 * @author 陈添明
 */
@Timeout(connectTimeoutMs = 3000, readTimeoutMs = 3000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(enable = true, logLevel = LogLevel.DEBUG, logStrategy = LogStrategy.BODY)
@Retry(enable = true, maxRetries = 5, intervalMs = 200, retryRules = {RetryRule.OCCUR_EXCEPTION})
public interface AnnotatedActuateService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
