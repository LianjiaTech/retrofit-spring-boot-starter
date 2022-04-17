package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @summary
 * @since 2022/1/21 4:19 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@SentinelDegrade(count = 100)
public interface DegradeApi {

    /**
     * 其他任意Java类型 <br>
     * 将响应体内容适配成一个对应的Java类型对象返回，如果http状态码不是2xx，直接抛错！<br>
     *
     * @param id id
     * @return 其他任意Java类型
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

}
