package com.github.lianjiatech.retrofit.plus.annotation;

import java.lang.annotation.*;

/**
 * <p>拦截标记注解<br>
 * 标记一个注解是拦截器
 *
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface InterceptMark {

}
