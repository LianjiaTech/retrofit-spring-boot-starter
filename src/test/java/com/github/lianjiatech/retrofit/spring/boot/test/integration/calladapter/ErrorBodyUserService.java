package com.github.lianjiatech.retrofit.spring.boot.test.integration.calladapter;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 在禁用 ErrorDecoder 的子上下文中使用，验证 BodyCallAdapter 在
 * 非 2xx 响应下走 errorBody → converter 的反序列化路径。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface ErrorBodyUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
