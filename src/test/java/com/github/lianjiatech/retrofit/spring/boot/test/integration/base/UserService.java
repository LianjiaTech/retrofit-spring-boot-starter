package com.github.lianjiatech.retrofit.spring.boot.test.integration.base;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 包含User增删改查接口，底层通过http调用实现
 * @author 陈添明
 * @since 2023/12/16 9:47 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

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

    /**
     * 根据id异步查询用户信息
     */
    @GET("getUserAsync")
    CompletableFuture<User> getUserAsync(@Query("id") Long id);

    /**
     * 根据id查询用户信息，以Response返回
     */
    @GET("getUserReturnResponse")
    Response<User> getUserReturnResponse(@Query("id") Long id);

    /**
     * 根据id查询用户信息，以Call返回，此时还没真正发起调用，需要使用方主动调用。
     */
    @GET("getUserReturnCall")
    Call<User> getUserReturnCall(@Query("id") Long id);

    /**
     * 根据id查询用户是否是男性
     */
    @POST("isMale")
    Boolean isMale(@Query("id") Long id);

    /**
     * 保存用户
     */
    @POST("saveUser")
    Void saveUser(@Body User user);

    /**
     * 保存一组用户
     */
    @POST("saveUserList")
    Void saveUserList(@Body List<User> userList);

}
