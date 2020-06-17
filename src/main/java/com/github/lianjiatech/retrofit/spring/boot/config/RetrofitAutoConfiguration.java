package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.RetrofitPlusInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.RetrofitPlusInterceptorBdfRegistryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈添明
 */
@Configuration
@EnableConfigurationProperties(RetrofitProperties.class)
public class RetrofitAutoConfiguration {

    /**
     * 配置 {@link RetrofitPlusInterceptorBdfRegistryPostProcessor}
     * 动态修改 {@link RetrofitPlusInterceptor} 的`BeanDefinition`中的scope为`prototype`
     *
     * @return RetrofitPlusInterceptorBdfRegistryPostProcessor instance
     */
    @Bean
    public RetrofitPlusInterceptorBdfRegistryPostProcessor retrofitPlusInterceptorBdfRegistryPostProcessor() {
        return new RetrofitPlusInterceptorBdfRegistryPostProcessor();
    }
}
