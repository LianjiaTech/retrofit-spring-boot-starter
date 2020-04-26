package com.github.lianjiatech.retrofit.spring.test.config;

import com.github.lianjiatech.retrofit.plus.annotation.RetrofitScan;
import com.github.lianjiatech.retrofit.plus.config.Config;
import com.github.lianjiatech.retrofit.plus.config.PoolConfig;
import com.github.lianjiatech.retrofit.plus.core.RetrofitHelper;
import com.github.lianjiatech.retrofit.plus.interceptor.RetrofitPlusInterceptor;
import com.github.lianjiatech.retrofit.plus.interceptor.RetrofitPlusInterceptorBdfRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用 retrofitHelperRef bean 的方式进行配置
 *
 * @author 陈添明
 */
@RetrofitScan(value = "com.github.lianjiatech.retrofit.spring.test.http", retrofitHelperRef = "retrofitHelper")
@Configuration
@ComponentScan("com.github.lianjiatech.retrofit.spring.test.interceptor")
public class RetrofitRefBeanConfig {
    /**
     * 使用RetrofitHelper进行配置
     *
     * @return RetrofitHelper实例
     */
    @Bean
    public RetrofitHelper retrofitHelper() {
        // 连接池配置
        PoolConfig test1 = new PoolConfig(5, 300);
        PoolConfig test2 = new PoolConfig(2, 100);
        Map<String, PoolConfig> pool = new HashMap<>(16);
        pool.put("test1", test1);
        pool.put("test2", test2);
        // 配置对象
        Config config = new Config();
        config.setPool(pool);
        // 是否启用 BodyCallAdapter适配器
        config.setEnableBodyCallAdapter(true);
        // 是否启用 ResponseCallAdapter适配器
        config.setEnableResponseCallAdapter(true);
        // 是否启用 Retrofit2Converter转码器
        config.setEnableFastJsonConverter(true);
        // 启用日志打印
        config.setEnableLog(true);
        // 禁用java.lang.Void作为返回值
//        config.setDisableVoidReturnType(true);
        // retrofitHelper bean
        RetrofitHelper retrofitHelper = new RetrofitHelper(config);

        // 配置其他属性 这些配置也可以配在spring其他的配置文件中
        retrofitHelper.addProperty("test.baseUrl", "http://localhost:8080/api/test/");
        retrofitHelper.addProperty("test.accessKeyId", "2523453463456");
        retrofitHelper.addProperty("test.accessKeySecret", "sdjfsdfasdfdg");
        return retrofitHelper;
    }


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
