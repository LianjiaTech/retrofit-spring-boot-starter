package com.github.lianjiatech.retrofit.spring.boot.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author 陈添明
 */
public final class ApplicationContextUtils {

    private ApplicationContextUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


    public static <T> T getBean(ApplicationContext context, Class<T> clz) {
        try {
            T bean = context.getBean(clz);
            return bean;
        } catch (BeansException e) {
            return null;
        }
    }

    public static <T> T getTargetInstanceIfNecessary(T bean) {
        Object object = bean;
        while (AopUtils.isAopProxy(object)) {
            try {
                object = ((Advised)object).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new RuntimeException("get target bean failed", e);
            }
        }
        return (T)object;
    }
}
