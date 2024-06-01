package com.github.lianjiatech.retrofit.spring.boot.test.integration.inherit;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Super {

    /**
     * 根据id查询用户姓名
     */
    @GET("getNameSuper")
    String getNameSuper(@Query("id") Long id);
}
