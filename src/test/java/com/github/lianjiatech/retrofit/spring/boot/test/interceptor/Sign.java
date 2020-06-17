package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;


import com.github.lianjiatech.retrofit.spring.boot.annotation.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import java.lang.annotation.*;

/**
 * 自动将注解上的参数值赋值到handleInterceptor实例上
 *
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@InterceptMark
public @interface Sign {

    /**
     * 密钥key
     * 支持占位符形式配置。
     *
     * @return 密钥key
     */
    String accessKeyId();

    /**
     * 密钥
     * 支持占位符形式配置。
     *
     * @return 密钥
     */
    String accessKeySecret();


    /**
     * 拦截器匹配路径
     *
     * @return 拦截器匹配路径
     */
    String[] include() default {"/**"};

    /**
     * 拦截器排除匹配，排除指定路径拦截
     *
     * @return 排除指定路径拦截
     */
    String[] exclude() default {};

    /**
     * 处理该注解的拦截器类 <br>
     * 优先从spring容器获取对应的Bean，如果获取不到，则使用反射创建一个！ <br>
     * 如果以Bean的形式配置，scope必须是prototype <br>
     *
     * @return 处理该注解的拦截器类
     */
    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
