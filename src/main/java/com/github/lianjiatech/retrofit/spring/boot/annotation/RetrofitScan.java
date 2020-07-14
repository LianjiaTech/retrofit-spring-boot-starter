package com.github.lianjiatech.retrofit.spring.boot.annotation;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RetrofitClientRegistrar.class)
public @interface RetrofitScan {

    /**
     * <p>扫描包路径</p>
     * 与basePackages含义相同
     *
     * @return 扫描包路径
     */
    String[] value() default {};


    /**
     * <p>扫描包路径</p>
     * 与value含义相同
     *
     * @return 扫描包路径
     */
    String[] basePackages() default {};

    /**
     * 扫描的classes
     *
     * @return 扫描的classes
     */
    Class<?>[] basePackageClasses() default {};
}
