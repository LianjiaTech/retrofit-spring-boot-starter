package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.sentinel;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:47 下午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = GlobalSentinelFallbackUserService.class)
public interface GlobalSentinelUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

}
