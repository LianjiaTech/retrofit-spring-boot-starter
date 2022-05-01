package com.github.lianjiatech.retrofit.spring.boot.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.experimental.UtilityClass;

/**
 * @author 陈添明
 * @since 2022/4/30 3:02 下午
 */
@UtilityClass
public class AnnotationExtendUtils {

    /**
     * 查找方法及其类上的指定注解，优先返回方法上的。
     * @param <A> 注解泛型参数
     * @param method 方法
     * @param annotationType 注解类型
     * @return 方法及其类上的指定注解。
     */
    public static <A extends Annotation> A findAnnotationIncludeClass(Method method, Class<A> annotationType) {
        A annotation = method.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return method.getDeclaringClass().getAnnotation(annotationType);
    }

    /**
     * 判断某个类及其公有方法上是否存在指定注解。
     * @param <A> 注解泛型参数
     * @param clazz 类
     * @param annotationType 注解类型
     * @return 判断某个类及其公有方法上是否存在指定注解。
     */
    public static <A extends Annotation> boolean isAnnotationPresentIncludeMethod(Class<?> clazz,
            Class<A> annotationType) {
        if (clazz.isAnnotationPresent(annotationType)) {
            return true;
        }
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }

}
