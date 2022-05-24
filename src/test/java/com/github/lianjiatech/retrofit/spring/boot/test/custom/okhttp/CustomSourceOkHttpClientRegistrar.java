package com.github.lianjiatech.retrofit.spring.boot.test.custom.okhttp;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;
import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistry;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * @author 陈添明
 * @since 2022/5/24 9:29 下午
 */
@Slf4j
@Component
public class CustomSourceOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {

        // 替换默认的SourceOkHttpClient
        registry.register(Constants.DEFAULT_SOURCE_OK_HTTP_CLIENT, new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .writeTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .addInterceptor(chain -> {
                    log.info("============替换默认的SourceOkHttpClient=============");
                    return chain.proceed(chain.request());
                })
                .build());

        // 添加新的SourceOkHttpClient
        registry.register("testSourceOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .writeTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .addInterceptor(chain -> {
                    log.info("============使用testSourceOkHttpClient=============");
                    return chain.proceed(chain.request());
                })
                .build());
    }
}
