package com.github.lianjiatech.retrofit.spring.boot.test.integration.chooser;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 9:53 上午
 */
@RetrofitClient(serviceId = "user", path = "/api/user")
public interface ChooserOkHttpUserService {

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    User getUser(@Query("id") Long id);
}
