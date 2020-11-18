package com.github.lianjiatech.retrofit.spring.boot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jaxb.JaxbConverterFactory;

/**
 * @author 陈添明
 */
@SpringBootApplication
public class RetrofitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrofitTestApplication.class, args);
    }


    @Bean
    GsonConverterFactory gsonConverterFactory() {
        return GsonConverterFactory.create();
    }

    @Bean
    JaxbConverterFactory jaxbConverterFactory() {
        return JaxbConverterFactory.create();
    }
}
