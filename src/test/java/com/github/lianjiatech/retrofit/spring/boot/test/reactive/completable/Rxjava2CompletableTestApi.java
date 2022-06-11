package com.github.lianjiatech.retrofit.spring.boot.test.reactive.completable;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import io.reactivex.Completable;
import retrofit2.http.GET;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface Rxjava2CompletableTestApi {

    @GET("ping")
    Completable ping();
}
