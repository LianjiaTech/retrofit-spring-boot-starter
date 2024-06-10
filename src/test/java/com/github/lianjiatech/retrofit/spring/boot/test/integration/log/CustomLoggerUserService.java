package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

/**
 * 测试自定义 logger 的客户端
 *
 * @author Hason
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
@Logging(logger = CustomLoggerUserService.LOGGER)
public interface CustomLoggerUserService {

    String LOGGER = "CustomLoggerUserService";

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

    /**
     * 根据id查询用户信息
     */
    @GET("getUser")
    @Logging(logStrategy = LogStrategy.BODY, logger = CustomLoggerUserService.LOGGER + ".getUser")
    User getUser(@Query("id") Long id);

}
