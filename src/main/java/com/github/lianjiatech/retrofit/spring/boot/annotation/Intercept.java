package com.github.lianjiatech.retrofit.spring.boot.annotation;


import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import java.lang.annotation.*;

/**
 * 自动将注解上的参数值赋值到handleInterceptor实例上
 * Automatically assign the parameter value on the annotation to the handleInterceptor instance
 *
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@InterceptMark
@Repeatable(Intercepts.class)
public @interface Intercept {
    /**
     * 拦截器匹配路径pattern
     * Interceptor matching path pattern
     *
     * @return 拦截器匹配路径pattern Interceptor matching path pattern
     */
    String[] include() default {"/**"};

    /**
     * 拦截器排除匹配，排除指定路径拦截
     * Interceptor excludes matching, excludes specified path interception
     *
     * @return 排除指定路径拦截pattern Exclude specified path interception pattern
     */
    String[] exclude() default {};

    /**
     * Interceptor handler
     * 优先从spring容器获取对应的Bean，如果获取不到，则使用反射创建一个！
     * First obtain the corresponding Bean from the spring container, if not, use reflection to create one!
     *
     * @return 拦截器处理器 Interceptor handler
     */
    Class<? extends BasePathMatchInterceptor> handler();
}
