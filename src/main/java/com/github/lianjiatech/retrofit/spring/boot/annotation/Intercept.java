package com.github.lianjiatech.retrofit.spring.boot.annotation;


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
public @interface Intercept {
    /**
     * 拦截器匹配路径pattern
     *
     * @return 拦截器匹配路径pattern
     */
    String[] include() default {"/**"};

    /**
     * 拦截器排除匹配，排除指定路径拦截
     *
     * @return 排除指定路径拦截pattern
     */
    String[] exclude() default {};

    /**
     * 拦截器处理器
     * 优先从spring容器获取对应的Bean，如果获取不到，则使用反射创建一个！
     * 如果以Bean的形式配置，scope必须是prototype <br>
     *
     * @return 拦截器处理器
     */
    Class<? extends BasePathMatchInterceptor> handler();
}
