package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 验证 path-match 拦截器的 include/exclude 优先级：当同一路径同时命中 include 与 exclude，
 * exclude 应优先 — 不执行 doIntercept，没有 path.match 头。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = PathMatchInterceptor.class,
        include = {"/api/user/**"},
        exclude = {"/api/user/getUser"})
public interface PathMatchPrecedenceUserService {

    /** 同时命中 include 和 exclude，exclude 优先 → 没有 path.match 头 */
    @GET("getUser")
    Response<User> getUser(@Query("id") Long id);

    /** 命中 include，未被 exclude → 有 path.match 头 */
    @GET("getName")
    Response<String> getName(@Query("id") Long id);
}
