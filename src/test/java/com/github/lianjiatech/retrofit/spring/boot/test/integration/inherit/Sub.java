package com.github.lianjiatech.retrofit.spring.boot.test.integration.inherit;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import retrofit2.http.GET;
import retrofit2.http.Query;

@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface Sub extends Super {

    /**
     * 根据id查询用户姓名
     */
    @GET("getNameSub")
    String getNameSub(@Query("id") Long id);
}
