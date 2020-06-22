package com.github.lianjiatech.retrofit.spring.boot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.PrototypeInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.PrototypeInterceptorBdfProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
public class RetrofitAutoConfiguration {

    /**
     * 配置 {@link PrototypeInterceptorBdfProcessor}
     * 动态修改 {@link PrototypeInterceptor} 的`BeanDefinition`中的scope为`prototype`
     *
     * @return RetrofitPlusInterceptorBdfRegistryPostProcessor instance
     */
    @Bean
    public PrototypeInterceptorBdfProcessor retrofitPlusInterceptorBdfRegistryPostProcessor() {
        return new PrototypeInterceptorBdfProcessor();
    }

    @Bean
    @ConditionalOnProperty(name = "retrofit.enable-body-call-adapter", havingValue = "true")
    public BodyCallAdapterFactory bodyCallAdapterFactory() {
        return new BodyCallAdapterFactory();
    }


    @Bean
    @ConditionalOnProperty(name = "retrofit.enable-response-call-adapter", havingValue = "true")
    public ResponseCallAdapterFactory responseCallAdapterFactory() {
        return new ResponseCallAdapterFactory();
    }

    @Bean("defaultJacksonConverterFactory")
    @ConditionalOnClass(JacksonConverterFactory.class)
    public JacksonConverterFactory jacksonConverterFactory() {
        return JacksonConverterFactory.create(new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
    }

}
