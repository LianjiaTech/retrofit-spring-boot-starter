package com.github.lianjiatech.retrofit.spring.boot.test.reactive.single;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface Rxjava2SingleTestApi {

    @GET("person")
    Single<Result<Person>> getPerson(@Query("id") Long id);

    @GET("person")
    Single<Response<Result<Person>>> getPersonResponse(@Query("id") Long id);
}
