package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import java.util.List;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:47 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface GlobalRetryUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    User getUser(@Query("id") Long id);

    /**
     * 查询所有用户信息
     */
    @GET("getAll")
    List<User> getAll();
}
