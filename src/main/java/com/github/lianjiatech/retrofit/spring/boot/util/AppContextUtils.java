package com.github.lianjiatech.retrofit.spring.boot.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
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
     * 优先从 Spring 容器获取实例；容器中不存在时再回退到反射创建。
     * <p>
     * 反射回退顺序：公开无参构造 → 公开静态 {@code create()} 方法（兼容 Retrofit 的
     * {@link retrofit2.Converter.Factory} 等静态工厂模式）。
     * <p>
     * 仅捕获 {@link NoSuchBeanDefinitionException}：Bean 构造失败、依赖缺失等
     * Spring 抛出的其他异常会向上传递，避免出现"静默退化为非托管实例"导致 AOP / 依赖注入失效。
     * <p>
     * <b>注意：反射创建的实例完全脱离 Spring 管理</b> — {@code @Autowired}、{@code @PostConstruct}、
     * {@code @Value} 等都不会生效。当用户写带这些注解的 ErrorDecoder / FallbackFactory /
     * Converter.Factory 时，常常会在使用其依赖时遭遇 NPE。该方法在走反射路径时打 WARN，提示用户
     * 把对应类声明为 Spring Bean。
     *
     * @param context spring context
     * @param clz 对象类型
     * @param <T> 泛型参数
     * @return spring context实例，或者反射创建的实例。
     */
    public static <T> T getBeanOrNew(ApplicationContext context, Class<T> clz) {
        try {
            return context.getBean(clz);
        } catch (NoUniqueBeanDefinitionException e) {
            // 容器中存在多个候选 Bean，属于配置错误，应直接抛出而非反射创建
            throw e;
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("No bean of type {} in Spring context; falling back to reflective instantiation. "
                    + "@Autowired / @PostConstruct / @Value on this class will NOT take effect — "
                    + "register it as a Spring bean if you rely on container features.", clz.getName());
            return createInstance(clz);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(Class<T> clz) {
        try {
            return clz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException noPublicCtor) {
            // 没有公开无参构造，尝试静态 create() 方法
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                    "Constructor of " + clz.getName() + " threw an exception", e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to instantiate " + clz.getName() + " via no-arg constructor", e);
        }
        try {
            Method create = clz.getMethod("create");
            return (T) create.invoke(null);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Cannot instantiate " + clz.getName()
                            + ": no public no-arg constructor and no static create() method",
                    e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                    "Static create() of " + clz.getName() + " threw an exception", e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to invoke static create() on " + clz.getName(), e);
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
