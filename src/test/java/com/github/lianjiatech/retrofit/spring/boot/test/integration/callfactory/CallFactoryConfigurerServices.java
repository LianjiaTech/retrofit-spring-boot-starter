package com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 用于 CallFactoryConfigurer SPI 测试的服务接口。
 */
public interface CallFactoryConfigurerServices {

    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface DefaultCallTimeoutService {
        @DynamicCallTimeout(ms = 500)
        @GET("getUser")
        User getUser(@Query("id") Long id);

        @GET("getUser")
        User getUserNoAnnotation(@Query("id") Long id);
    }

    @Timeout(callTimeoutMs = 500)
    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface ShortCallTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }
}