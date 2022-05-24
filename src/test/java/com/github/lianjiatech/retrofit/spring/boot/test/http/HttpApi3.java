package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "http://ke.com")
public interface HttpApi3 {

    @GET
    Result<Person> getPerson(@Url String url, @Query("id") Long id);
}
