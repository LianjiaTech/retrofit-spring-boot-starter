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

        // 替换默认的SourceOkHttpClient，可以用来修改全局OkhttpClient设置
        registry.register(Constants.DEFAULT_SOURCE_OK_HTTP_CLIENT, new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .writeTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .addInterceptor(chain -> {
                    log.info("============replace default SourceOkHttpClient=============");
                    return chain.proceed(chain.request());
                })
                .build());

        // 添加testSourceOkHttpClient
        registry.register("testSourceOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .writeTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(3))
                .addInterceptor(chain -> {
                    log.info("============use testSourceOkHttpClient=============");
                    return chain.proceed(chain.request());
                })
                .build());
    }
}
