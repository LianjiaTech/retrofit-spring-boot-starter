package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RetrofitClientScannerRegistrar.class)
@Inherited
public @interface RetrofitScan {

    /**
     * Scan package path
     * Same meaning as basePackages
     *
     * @return basePackages
     */
    String[] value() default {};

    /**
     * Scan package path
     *
     * @return basePackages
     */
    String[] basePackages() default {};

    /**
     * Scan package classes
     *
     * @return Scan package classes
     */
    Class<?>[] basePackageClasses() default {};
}
