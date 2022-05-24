package com.github.lianjiatech.retrofit.spring.boot.test.custom.okhttp;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", baseOkHttpClientBeanName = "testOkHttpClient")
public interface CustomOkHttpTestApi {

    @GET("person")
    @Headers({
        "X-Foo: Bar",
        "X-Ping: Pong"
    })
    Result<Person> getPerson(@Query("id") Long id);
}
