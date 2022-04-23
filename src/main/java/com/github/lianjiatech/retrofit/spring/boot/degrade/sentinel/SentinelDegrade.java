package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.lang.annotation.*;

import com.github.lianjiatech.retrofit.spring.boot.degrade.Degrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeStrategy;

/**
 * 应仅采用异常比例模式来控制熔断，超时导致的报错应在okhttp这一层做
 * @author 陈添明 yukdawn@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Degrade(register = SentinelDegradeRuleRegister.class)
public @interface SentinelDegrade {

    /**
     * 异常比例
     */
    // FIXME double
    float count();

    /**
     * 时间窗口size，单位：秒
     */
    int timeWindow() default 5;

    /**
     * Degrade strategy (0: average RT, 1: exception ratio).
     */
    DegradeStrategy degradeStrategy() default DegradeStrategy.AVERAGE_RT;
}
