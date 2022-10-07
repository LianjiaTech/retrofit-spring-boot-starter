package com.github.lianjiatech.retrofit.spring.boot.test.timeout;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.InvalidRespErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;

import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@Logging(logStrategy = LogStrategy.BODY)
@RetrofitClient(baseUrl = "${test.baseUrl}", readTimeoutMs = 2000)
public interface TimeoutTestApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
