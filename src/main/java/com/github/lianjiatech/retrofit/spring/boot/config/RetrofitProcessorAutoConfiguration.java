package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.core.PrototypeInterceptorBdfProcessor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.PrototypeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈添明
 */
@Configuration
public class RetrofitProcessorAutoConfiguration {

    /**
     * 配置 {@link PrototypeInterceptorBdfProcessor}
     * 动态修改 {@link PrototypeInterceptor} 的`BeanDefinition`中的scope为`prototype`
     *
     * @return PrototypeInterceptorBdfProcessor instance
     */
    @Bean
    public PrototypeInterceptorBdfProcessor prototypeInterceptorBdfProcessor() {
        return new PrototypeInterceptorBdfProcessor();
    }
}
