package com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:40 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", readTimeoutMs = 1000, writeTimeoutMs = 1000)
public interface TimeoutUserService {

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
