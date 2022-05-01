package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.InvalidRespErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;

import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author 陈添明
 */
@Logging(logStrategy = LogStrategy.BODY)
@RetrofitClient(baseUrl = "${test.baseUrl}", errorDecoder = InvalidRespErrorDecoder.class)
public interface ErrorDecoderTestApi {

    @POST("error")
    Person error(@Body Person person);
}
