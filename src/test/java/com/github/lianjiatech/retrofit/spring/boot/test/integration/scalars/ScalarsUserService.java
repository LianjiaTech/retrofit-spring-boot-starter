package com.github.lianjiatech.retrofit.spring.boot.test.integration.scalars;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;

/**
 * 验证默认最前置内置 ScalarsConverterFactory：String / 基本类型返回值按响应体原始文本返回，
 * POJO 仍走 JSON Converter。baseUrl 指向 MockWebServer。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface ScalarsUserService {

    /** 返回纯文本（非合法 JSON），验证 Scalars 原样返回而非报错。 */
    @GET("string/plain")
    String plainText();

    /** 返回带引号的 JSON 字符串字面量，验证 Scalars 保留引号（区别于 Jackson 的去引号行为）。 */
    @GET("string/quoted")
    String quotedString();

    @GET("int")
    Integer intValue();

    @GET("long")
    Long longValue();

    @GET("boolean")
    Boolean booleanValue();

    @GET("double")
    Double doubleValue();

    /** POJO 回归：仍由 Jackson 正常反序列化，证明前置 Scalars 不影响对象类型。 */
    @GET("user")
    User user();
}
