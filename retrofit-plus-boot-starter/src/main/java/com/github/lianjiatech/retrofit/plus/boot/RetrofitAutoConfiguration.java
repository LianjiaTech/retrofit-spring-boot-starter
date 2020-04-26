package com.github.lianjiatech.retrofit.plus.boot;

import com.github.lianjiatech.retrofit.plus.interceptor.RetrofitPlusInterceptor;
import com.github.lianjiatech.retrofit.plus.interceptor.RetrofitPlusInterceptorBdfRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈添明
 */
@Configuration
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
