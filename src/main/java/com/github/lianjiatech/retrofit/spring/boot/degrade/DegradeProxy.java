package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

/**
 * @author 陈添明
 * @since 2022/4/30 2:27 下午
 */
@Slf4j
public class DegradeProxy implements InvocationHandler {

    private final Object source;

    private final Object fallback;

    private final FallbackFactory<?> fallbackFactory;

    @SuppressWarnings("unchecked")
    public static <T> T create(Object source, Class<T> retrofitInterface, ApplicationContext applicationContext) {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        Class<?> fallbackClass = retrofitClient.fallback();
        Object fallback = null;
        if (!void.class.isAssignableFrom(fallbackClass)) {
            fallback = AppContextUtils.getBeanOrNew(applicationContext, fallbackClass);
        }
        Class<?> fallbackFactoryClass = retrofitClient.fallbackFactory();
        FallbackFactory<?> fallbackFactory = null;
        if (!void.class.isAssignableFrom(fallbackFactoryClass)) {
            fallbackFactory =
                    (FallbackFactory<?>)AppContextUtils.getBeanOrNew(applicationContext, fallbackFactoryClass);
        }
        DegradeProxy degradeProxy = new DegradeProxy(source, fallback, fallbackFactory);
        return (T)Proxy.newProxyInstance(retrofitInterface.getClassLoader(),
                new Class<?>[] {retrofitInterface}, degradeProxy);
    }

    public DegradeProxy(Object source, Object fallback, FallbackFactory<?> fallbackFactory) {
        this.source = source;
        this.fallback = fallback;
        this.fallbackFactory = fallbackFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(source, args);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            // 熔断逻辑
            if (cause instanceof RetrofitBlockException) {
                Object fallbackObject = getFallbackObject(cause);
                if (fallbackObject != null) {
                    log.error("call fallback! method={}, args={}", method, args, cause);
                    return method.invoke(fallbackObject, args);
                }
            }
            throw cause;
        }
    }

    private Object getFallbackObject(Throwable cause) {
        if (fallback != null) {
            return fallback;
        }

        if (fallbackFactory != null) {
            return fallbackFactory.create(cause);
        }
        return null;
    }
}
