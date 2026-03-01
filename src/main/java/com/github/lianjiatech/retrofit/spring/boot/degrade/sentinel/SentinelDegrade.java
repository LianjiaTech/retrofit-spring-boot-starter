package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface SentinelDegrade {

    /**
     * 是否开启
     *
     * @return 是否开启
     */
    boolean enable() default true;

    /**
     * 降级规则
     *
     * @return 降级规则
     */
    SentinelDegradeRule[] rules() default {};
}
