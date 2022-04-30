package com.github.lianjiatech.retrofit.spring.boot.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import lombok.experimental.UtilityClass;

/**
 * @author 陈添明
 */
@UtilityClass
public final class ApplicationContextUtils {

    public static <T> T getBeanOrNull(ApplicationContext context, Class<T> clz) {
        try {
            return context.getBean(clz);
        } catch (BeansException e) {
            return null;
        }
    }

    public static <T> T getBeanOrNew(ApplicationContext context, Class<T> clz)
            throws InstantiationException, IllegalAccessException {
        try {
            return context.getBean(clz);
        } catch (BeansException e) {
            return clz.newInstance();
        }
    }

    @SuppressWarnings("unchecked")
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
