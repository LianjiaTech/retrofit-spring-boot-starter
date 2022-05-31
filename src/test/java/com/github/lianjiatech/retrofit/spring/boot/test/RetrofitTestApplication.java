package com.github.lianjiatech.retrofit.spring.boot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.log.AggregateLoggingInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.log.LoggingInterceptor;

import lombok.extern.slf4j.Slf4j;
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
    public InvalidRespErrorDecoder invalidRespErrorDecoder() {
        return new InvalidRespErrorDecoder();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public LoggingInterceptor loggingInterceptor(RetrofitProperties retrofitProperties) {
        return new AggregateLoggingInterceptor(retrofitProperties.getGlobalLog());
    }
}
