package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.resilience4j;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;

import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:47 下午
 */
@Timeout(connectTimeoutMs = 1, readTimeoutMs = 1, writeTimeoutMs = 1)
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = GlobalResilience4jFallbackFactory.class)
public interface GlobalResilience4jUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

}
