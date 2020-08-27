package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.core.PrototypeInterceptorBdfProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈添明
 */
@Configuration
public class RetrofitProcessorAutoConfiguration {


    @Bean
    public PrototypeInterceptorBdfProcessor prototypeInterceptorBdfProcessor() {
        return new PrototypeInterceptorBdfProcessor();
    }
}
