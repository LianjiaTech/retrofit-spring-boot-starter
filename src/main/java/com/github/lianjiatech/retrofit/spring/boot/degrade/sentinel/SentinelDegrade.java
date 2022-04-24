package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.lang.annotation.*;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.github.lianjiatech.retrofit.spring.boot.degrade.Degrade;

/**
 * Sentinel熔断器配置
 * @author 陈添明 yukdawn@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Degrade(register = SentinelDegradeRuleRegister.class)
public @interface SentinelDegrade {

    /**
     * 熔断策略
     */
    int grade() default DEFAULT_GRADE;
    /**
     * 异常比例
     */
    double count() default DEFAULT_COUNT;
    /**
     * 时间窗口size，单位：秒
     */
    int timeWindow() default SentinelDegrade.DEFAULT_TIME_WINDOW;

    int DEFAULT_GRADE = RuleConstant.DEGRADE_GRADE_RT;
    double DEFAULT_COUNT = 0.5D;
    int DEFAULT_TIME_WINDOW = 5;
}
