package com.github.lianjia.retrofit.spring.test.http;

import com.github.lianjia.retrofit.plus.annotation.RetrofitClient;
import com.github.lianjia.retrofit.spring.test.entity.Result;
import com.github.lianjia.retrofit.spring.test.entity.Person;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "http://ke.com")
public interface HttpApi3 {

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
