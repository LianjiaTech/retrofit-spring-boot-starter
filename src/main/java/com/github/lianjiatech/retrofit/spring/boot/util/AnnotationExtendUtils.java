package com.github.lianjiatech.retrofit.spring.boot.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

/**
 * @author 陈添明
 * @since 2022/4/30 3:02 下午
 */
public class AnnotationExtendUtils {

    /**
     * 查找方法上的Annotation，如果不存在，则查找类上的。
     * @param method 方法
     * @param annotationType 注解类型
     * @param <A> 注解泛型参数
     * @return 方法或者类上指定的注解。
     */
    public static <A extends Annotation> A findAnnotation(Method method, @Nullable Class<A> annotationType) {
        A annotation = method.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return method.getDeclaringClass().getAnnotation(annotationType);
    }

    /**
     * 判断某个类上指定的Annotation是否存在。如果类上不存在，则继续判断每个公有方法是否存在。
     * @param clazz 类
     * @param annotationType 注解类型
     * @param <A> 注解泛型参数
     * @return 某个类上指定的Annotation是否存在。类或公有方法存在，则返回true。
     */
    public static <A extends Annotation> boolean isAnnotationPresent(Class<?> clazz, Class<A> annotationType) {
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
