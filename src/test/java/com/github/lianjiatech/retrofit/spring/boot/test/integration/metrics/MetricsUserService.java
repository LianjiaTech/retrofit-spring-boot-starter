package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 用于 metrics 集成测试的 Retrofit 接口。{@code uri} 标签应取注解上的路径模板（含 {@code {id}}）。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface MetricsUserService {

    @GET("user/{id}")
    User getUser(@Path("id") long id);

    @POST("create")
    String create(@Query("name") String name);
}
