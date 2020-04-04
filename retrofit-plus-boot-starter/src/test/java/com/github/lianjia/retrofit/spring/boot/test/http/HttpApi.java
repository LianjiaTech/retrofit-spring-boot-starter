package com.github.lianjia.retrofit.spring.boot.test.http;

import com.github.lianjia.retrofit.plus.annotation.Intercept;
import com.github.lianjia.retrofit.plus.annotation.RetrofitClient;
import com.github.lianjia.retrofit.spring.boot.test.entity.Person;
import com.github.lianjia.retrofit.spring.boot.test.entity.Result;
import com.github.lianjia.retrofit.spring.boot.test.interceptor.Sign;
import com.github.lianjia.retrofit.spring.boot.test.interceptor.TimeStampInterceptor;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", exclude = {"/api/test/person"})
@Intercept(handler = TimeStampInterceptor.class, include = {"/api/**"}, exclude = "/api/test/savePerson")
public interface HttpApi {

    /**
     * .
     *
     * @param id .
     * @return .
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    /**
     * .
     *
     * @param person .
     * @return .
     */
    @POST("savePerson")
    Result<Person> savePerson(@Body Person person);
}
