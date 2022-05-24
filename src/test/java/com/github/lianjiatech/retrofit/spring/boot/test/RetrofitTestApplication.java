package com.github.lianjiatech.retrofit.spring.boot.test;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jaxb.JaxbConverterFactory;

/**
 * @author 陈添明
 */
@MySpringBootApplication
@Slf4j
public class RetrofitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrofitTestApplication.class, args);
    }

    @Bean
    public GsonConverterFactory gsonConverterFactory() {
        return GsonConverterFactory.create();
    }

    @Bean
    public JaxbConverterFactory jaxbConverterFactory() {
        return JaxbConverterFactory.create();
    }

    @Bean
    public OkHttpClient defaultBaseOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    log.info("=======替换defaultBaseOkHttpClient构建OkHttpClient=====");
                    return chain.proceed(chain.request());
                })
                .build();
    }

    @Bean
    public OkHttpClient testOkHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(10);

        return new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(10, 10, TimeUnit.MINUTES))
                .addInterceptor(chain -> {
                    log.info("=======基于testOkHttpClient构建OkHttpClient=====");
                    return chain.proceed(chain.request());
                })
                .build();
    }

    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        log.info("===替换defaultCircuitBreakerConfig====");
        return CircuitBreakerConfig.ofDefaults();
    }

    @Bean
    public CircuitBreakerConfig testCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .failureRateThreshold(30)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
    }

    @Bean
    public InvalidRespErrorDecoder invalidRespErrorDecoder() {
        return new InvalidRespErrorDecoder();
    }
}
