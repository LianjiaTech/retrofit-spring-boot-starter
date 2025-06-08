package com.github.lianjiatech.retrofit.spring.boot.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 陈添明
 */
@UtilityClass
@Slf4j
public final class AppContextUtils {

    /**
     * 优先从Spring容器获取实例，如果获取失败，调用无参方法创建，如果再失败，尝试调用无参create静态方法创建
     *
     * @param context spring context
     * @param clz 对象类型
     * @param <T> 泛型参数
     * @return spring context实例，或者反射创建的实例。
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanOrNew(ApplicationContext context, Class<T> clz) {
        try {
            return context.getBean(clz);
        } catch (Exception e1) {
            try {
                log.warn("Failed to get bean from applicationContext！");
                return clz.getDeclaredConstructor().newInstance();
            } catch (Exception e2) {
                log.warn("Failed to create instance by reflection.");
                try {
                    return (T)clz.getMethod("create").invoke(null);
                } catch (Exception e3) {
                    throw new RuntimeException("Failed to create instance through create static method.", e3);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTargetInstanceIfNecessary(T bean) {
        Object object = bean;
        while (AopUtils.isAopProxy(object)) {
            try {
                object = ((Advised)object).getTargetSource().getTarget();
            } catch (Exception e) {
                log.warn("Failed to get target source！", e);
            }
        }
        return (T)object;
    }
}
