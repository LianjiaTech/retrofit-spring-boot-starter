package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * @author 陈添明
 * @since 2023/12/17 10:40 上午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class, include = {"/api/user/**"}, exclude = "/api/user/getUser")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", include = "/api/user/getAll")
public interface InterceptorUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    Response<String> getName(@Query("id") Long id);

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);

    /**
     * 查询所有用户信息
     */
    @GET("getAll")
    Response<List<User>> getAll();

}
