package com.github.lianjiatech.retrofit.spring.boot.test.integration.custom.okhttp;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistrar;
import com.github.lianjiatech.retrofit.spring.boot.core.SourceOkHttpClientRegistry;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

/**
 * @author 陈添明
 * @since 2022/5/24 9:29 下午
 */
@Component
public class CustomOkHttpClientRegistrar implements SourceOkHttpClientRegistrar {

    @Override
    public void register(SourceOkHttpClientRegistry registry) {
        // 注册customOkHttpClient，超时时间设置为1s
        registry.register("customOkHttpClient", new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .writeTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .addInterceptor(chain -> chain.proceed(chain.request()))
                .build());
    }
}
