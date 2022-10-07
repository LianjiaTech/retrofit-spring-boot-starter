package com.github.lianjiatech.retrofit.spring.boot.test.degrade;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = DegradeSentinelApiFallback.class)
@Retry(enable = false)
public interface DegradeSentinelApi {

    /**
     * @param id .
     * @return .
     */
    @GET("degrade/person1")
    Result<Person> getPerson1(@Query("id") Long id);

    @GET("degrade/person2")
    @SentinelDegrade(enable = false)
    Result<Person> getPerson2(@Query("id") Long id);


}