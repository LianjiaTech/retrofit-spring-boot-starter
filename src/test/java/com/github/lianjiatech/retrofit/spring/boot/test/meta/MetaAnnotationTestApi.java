package com.github.lianjiatech.retrofit.spring.boot.test.meta;

import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@MyRetrofitClient(converterFactories = GsonConverterFactory.class, logStrategy = LogStrategy.HEADERS)
public interface MetaAnnotationTestApi {

    @GET("person")
    @Headers({
        "X-Foo: Bar",
        "X-Ping: Pong"
    })
    Result<Person> getPerson(@Query("id") Long id);
}
