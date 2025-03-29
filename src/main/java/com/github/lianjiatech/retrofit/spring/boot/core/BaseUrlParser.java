package com.github.lianjiatech.retrofit.spring.boot.core;

import org.springframework.core.env.Environment;

/**
 * baseUrl解析器，用于将`@Retrofit`上的信息解析成发起HTTP请求的BaseUrl，默认DefaultBaseUrlParser
 * @author chentianming
 * @since 2025/3/29
 */
public interface BaseUrlParser {

    /**
     * 对BaseUrl进行转换处理，得到符合业务需求的任意格式
     * @param retrofitClient  HTTP API上的Retrofit注解，用于拿到当前注解内容
     * @param environment Spring环境变量，辅助解析BaseUrl
     * @return 当前接口解析之后的BaseUrl，用于发起HTTP请求
     */
    String parse(RetrofitClient retrofitClient, Environment environment);
}
