package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.annotation.*;

/**
 * 熔断注解，该注解尽量不要被直接使用
 * @author yukdawn@gmail.com 2022/4/23 21:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Degrade {

    /**
     * 根据类型从spring容器中获取bean
     * @return DegradeRuleRegister
     */
    Class<? extends DegradeRuleRegister<?>> register();
}
