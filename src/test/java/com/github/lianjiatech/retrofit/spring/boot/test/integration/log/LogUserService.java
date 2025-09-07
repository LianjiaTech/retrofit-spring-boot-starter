package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import java.util.List;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:47 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(logStrategy = LogStrategy.HEADERS)
public interface LogUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    @Logging(logStrategy = LogStrategy.BODY)
    User getUser(@Query("id") Long id);

    /**
     * 查询所有用户信息
     */
    @GET("getAll")
    @Logging(logStrategy = LogStrategy.BODY, aggregate = false)
    List<User> getAll();

    /**
     * 根据id查询用户姓名
     */
    @POST("getNameWithHeader")
    @Logging(logStrategy = LogStrategy.BODY, redactHeaders = {"Token"})
    String getNameWithHeader(@Query("id") Long id, @Header("Authorization") String authorizationHeader,
            @Header("Token") String tokenHeader);
}
