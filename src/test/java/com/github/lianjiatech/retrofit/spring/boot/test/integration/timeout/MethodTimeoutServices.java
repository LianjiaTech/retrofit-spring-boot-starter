package com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 方法级 @Timeout 测试用的 service 接口。
 *
 * @author 陈添明
 */
public interface MethodTimeoutServices {

    /**
     * 类级 @Timeout(readTimeoutMs=1000)，所有方法继承此超时。
     */
    @Timeout(readTimeoutMs = 1000)
    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface ClassTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);

        @GET("getUser")
        User getUserSlow(@Query("id") Long id);
    }

    /**
     * 类级 @Timeout(readTimeoutMs=1000) + 方法级 @Timeout(readTimeoutMs=5000) 覆盖。
     */
    @Timeout(readTimeoutMs = 1000)
    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface MethodOverridesClassService {
        @GET("getUser")
        User getUser(@Query("id") Long id);

        @Timeout(readTimeoutMs = 5000)
        @GET("getUser")
        User getUserWithLongTimeout(@Query("id") Long id);
    }

    /**
     * 无类级 @Timeout，方法级 @Timeout(readTimeoutMs=500)。
     */
    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface OnlyMethodTimeoutService {
        @GET("getUser")
        User getUserDefault(@Query("id") Long id);

        @Timeout(readTimeoutMs = 500)
        @GET("getUser")
        User getUserShortTimeout(@Query("id") Long id);
    }

    /**
     * 无任何 @Timeout，使用全局配置。
     */
    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface NoTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }
}