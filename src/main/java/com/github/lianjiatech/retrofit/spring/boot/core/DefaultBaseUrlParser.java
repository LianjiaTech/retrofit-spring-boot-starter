package com.github.lianjiatech.retrofit.spring.boot.core;

import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author chentianming
 * @since 2025/3/29
 */
public class DefaultBaseUrlParser implements BaseUrlParser {

    String SUFFIX = "/";
    String HTTP_PREFIX = "http://";

    @Override
    public String parse(RetrofitClient retrofitClient, Environment environment) {
        String baseUrl = Objects.requireNonNull(retrofitClient).baseUrl();
        if (StringUtils.hasText(baseUrl)) {
            baseUrl = environment.resolveRequiredPlaceholders(baseUrl);
            // 解析baseUrl占位符
            if (!baseUrl.endsWith(SUFFIX)) {
                baseUrl += SUFFIX;
            }
        } else {
            String serviceId = retrofitClient.serviceId();
            String path = retrofitClient.path();
            if (!path.endsWith(SUFFIX)) {
                path += SUFFIX;
            }
            baseUrl = HTTP_PREFIX + (serviceId + SUFFIX + path).replaceAll("/+", SUFFIX);
            baseUrl = environment.resolveRequiredPlaceholders(baseUrl);
        }
        return baseUrl;
    }
}
