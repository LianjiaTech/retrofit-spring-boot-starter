package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.sentinel;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:47 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = SentinelFallbackUserService.class, connectTimeoutMs = 1,
        readTimeoutMs = 1, writeTimeoutMs = 1)
@SentinelDegrade(grade = 1, count = 0.01, timeWindow = 3)
public interface SentinelUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    @SentinelDegrade(grade = 2, count = 1, timeWindow = 4)
    User getUser(@Query("id") Long id);

}
