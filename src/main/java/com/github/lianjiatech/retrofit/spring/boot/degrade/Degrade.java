package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.annotation.*;

/**
 * 应仅采用异常比例模式来控制熔断，超时导致的报错应在okhttp这一层做
 * @author 陈添明 yukdawn@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface Degrade {

    /**
     * 异常比例
     */
    float count();

    /**
     * 时间窗口size，单位：秒
     */
    int timeWindow() default 5;
}
