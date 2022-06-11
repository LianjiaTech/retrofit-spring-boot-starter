package com.github.lianjiatech.retrofit.spring.boot.test.reactive.completable;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import io.reactivex.rxjava3.core.Completable;
import retrofit2.http.GET;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface Rxjava3CompletableTestApi {

    @GET("ping")
    Completable ping();
}
