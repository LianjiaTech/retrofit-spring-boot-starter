package com.github.lianjiatech.retrofit.spring.boot.test.http;

import java.util.Map;

import com.github.lianjiatech.retrofit.spring.boot.annotation.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.EnumIntercept;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.EnvEnum;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.TimeStampInterceptor;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Intercept(handler = TimeStampInterceptor.class, include = "/a/b", exclude = "/c/d")
@EnumIntercept(envEnum = EnvEnum.test)
public interface HttpApi2 {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    @GET("testMap")
    Map<String, Map<String, String>> testMap();

    @HTTP(method = "get", path = "/getPersonBody", hasBody = true)
    Result<Person> getPersonBody(@Body Person person);

}
