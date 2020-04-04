package com.github.lianjia.retrofit.spring.test.http;

import com.github.lianjia.retrofit.plus.annotation.Intercept;
import com.github.lianjia.retrofit.plus.annotation.RetrofitClient;
import com.github.lianjia.retrofit.plus.interceptor.LogStrategy;
import com.github.lianjia.retrofit.spring.test.entity.Person;
import com.github.lianjia.retrofit.spring.test.entity.Result;
import com.github.lianjia.retrofit.spring.test.interceptor.TimeStampInterceptor;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", logStrategy = LogStrategy.BODY)
@Intercept(handler = TimeStampInterceptor.class, include = "/a/b", exclude = "/c/d")
public interface HttpApi2 {

    /**
     * .
     *
     * @param id .
     * @return .
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
