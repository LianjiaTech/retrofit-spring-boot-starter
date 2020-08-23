package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.OkHttpClientBuilder;
import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import okhttp3.OkHttpClient;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

import java.util.concurrent.TimeUnit;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "http://ke.com")
public interface HttpApi3 {


    @OkHttpClientBuilder
    static OkHttpClient.Builder okhttpClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS);

    }

    /**
     * .
     *
     * @param url .
     * @param id  .
     * @return .
     */
    @GET
    Result<Person> getPerson(@Url String url, @Query("id") Long id);
}
