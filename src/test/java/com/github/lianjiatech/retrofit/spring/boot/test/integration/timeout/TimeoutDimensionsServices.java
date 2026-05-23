package com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 各超时维度的 service：
 * <ul>
 *     <li>{@link ReadTimeoutService}：仅 readTimeout 极小</li>
 *     <li>{@link CallTimeoutService}：callTimeout（整体调用上限）</li>
 *     <li>{@link DefaultTimeoutService}：不覆盖，用全局 application.yml 配置</li>
 * </ul>
 *
 * @author 陈添明
 */
public interface TimeoutDimensionsServices {

    @RetrofitClient(baseUrl = "${test.baseUrl}", readTimeoutMs = 500)
    interface ReadTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }

    @RetrofitClient(baseUrl = "${test.baseUrl}", callTimeoutMs = 500)
    interface CallTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }

    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface DefaultTimeoutService {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }
}
