package com.github.lianjiatech.retrofit.spring.boot.annotation;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {

    Intercept[] value();
}
