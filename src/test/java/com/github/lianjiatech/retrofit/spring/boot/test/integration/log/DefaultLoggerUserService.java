package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 测试默认 logger 的客户端
 *
 * @author Hason
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface DefaultLoggerUserService {

    /**
     * 根据id查询用户姓名
     */
    @POST("getName")
    String getName(@Query("id") Long id);

}
