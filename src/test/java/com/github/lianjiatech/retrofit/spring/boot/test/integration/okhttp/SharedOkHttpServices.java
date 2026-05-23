package com.github.lianjiatech.retrofit.spring.boot.test.integration.okhttp;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 用于验证：
 * <ul>
 *     <li>{@link DefaultClient}：未覆盖连接池 → 共享 base 的 ConnectionPool/Dispatcher</li>
 *     <li>{@link IsolatedPoolClient}：显式覆盖 maxIdleConnections → 隔离一份独立 ConnectionPool</li>
 * </ul>
 *
 * @author 陈添明
 */
public interface SharedOkHttpServices {

    @RetrofitClient(baseUrl = "${test.baseUrl}")
    interface DefaultClient {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }

    @RetrofitClient(baseUrl = "${test.baseUrl}", maxIdleConnections = 1, keepAliveDurationMs = 60_000L)
    interface IsolatedPoolClient {
        @GET("getUser")
        User getUser(@Query("id") Long id);
    }
}
