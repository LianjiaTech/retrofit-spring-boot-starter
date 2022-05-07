package com.github.lianjiatech.retrofit.spring.boot.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitScan;

/**
 * @author 陈添明
 * @since 2022/5/7 8:48 下午
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@RetrofitScan("com.github.lianjiatech.retrofit.spring.boot")
@SpringBootApplication
public @interface MySpringBootApplication {}
