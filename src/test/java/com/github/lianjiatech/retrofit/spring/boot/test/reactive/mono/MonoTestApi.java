package com.github.lianjiatech.retrofit.spring.boot.test.reactive.mono;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import reactor.core.publisher.Mono;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface MonoTestApi {

    @GET("person")
    Mono<Result<Person>> getPerson(@Query("id") Long id);

    @GET("person")
    Mono<Response<Result<Person>>> getPersonResponse(@Query("id") Long id);

    @GET("ping")
    Mono<Void> ping();
}
