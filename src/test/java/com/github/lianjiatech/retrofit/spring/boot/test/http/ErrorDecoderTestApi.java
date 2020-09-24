package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.test.InvalidRespErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", logStrategy = LogStrategy.BODY, errorDecoder = InvalidRespErrorDecoder.class)
public interface ErrorDecoderTestApi {

    /**
     * .
     *
     * @param person .
     * @return .
     */
    @POST("error")
    Person error(@Body Person person);
}
