package com.github.lianjiatech.retrofit.spring.boot.test.integration.actuate;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 不带任何横切注解、且不覆盖超时/连接池的 client，用于验证 Actuator Endpoint 对
 * {@code source="global"} 与超时/连接池完全继承全局的解析。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface PlainActuateService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
