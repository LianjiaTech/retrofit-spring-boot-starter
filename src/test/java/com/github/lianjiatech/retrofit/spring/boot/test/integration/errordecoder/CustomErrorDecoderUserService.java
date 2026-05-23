package com.github.lianjiatech.retrofit.spring.boot.test.integration.errordecoder;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 使用自定义 ErrorDecoder 的 service。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", errorDecoder = CustomErrorDecoder.class)
public interface CustomErrorDecoderUserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
