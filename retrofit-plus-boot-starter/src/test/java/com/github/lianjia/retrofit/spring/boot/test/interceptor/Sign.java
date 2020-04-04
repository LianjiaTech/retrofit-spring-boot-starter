package com.github.lianjia.retrofit.spring.boot.test.interceptor;

import com.github.lianjia.retrofit.plus.annotation.InterceptMark;
import com.github.lianjia.retrofit.plus.interceptor.BasePathMatchInterceptor;

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

    String accessKeyId();

    String accessKeySecret();

    String[] include() default {"/**"};

    String[] exclude() default {};

    Class<? extends BasePathMatchInterceptor> handler() default SignInterceptor.class;
}
