package com.github.lianjiatech.retrofit.spring.boot.test.integration.appcontext;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 使用一个"未注册为 Spring Bean"的自定义 ErrorDecoder，触发
 * {@code AppContextUtils.getBeanOrNew} 的反射回退路径以验证 WARN 日志。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}",
        errorDecoder = ReflectiveOnlyErrorDecoder.class)
public interface ReflectiveDecoderUserService {

    @GET("getName")
    String getName(@Query("id") Long id);
}
