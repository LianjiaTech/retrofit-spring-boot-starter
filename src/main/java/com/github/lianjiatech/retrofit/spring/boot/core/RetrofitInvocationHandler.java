package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.ApplicationContextUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈添明
 */
public class RetrofitInvocationHandler implements InvocationHandler {

    private final Object source;

    private final RetrofitProperties retrofitProperties;

    private Class<?> fallback;

    private Class<?> fallbackFactory;

    private ApplicationContext applicationContext;

    private static final Map<Method, Object> FALLBACK_OBJ_CACHE = new ConcurrentHashMap<>(128);

    public RetrofitInvocationHandler(Object source, Class<?> fallback, Class<?> fallbackFactory, RetrofitProperties retrofitProperties, ApplicationContext applicationContext) {
        this.source = source;
        this.retrofitProperties = retrofitProperties;
        this.fallback = fallback;
        this.fallbackFactory = fallbackFactory;
        this.applicationContext = applicationContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(source, args);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            Object fallbackObject = getFallbackObject(method);
            // 熔断逻辑
            if (cause instanceof RetrofitBlockException && retrofitProperties.isEnableDegrade() && EmptyObject.class.isAssignableFrom(fallbackObject.getClass())) {
                return method.invoke(fallbackObject, args);
            }
            throw cause;
        }
    }

    private Object getFallbackObject(Method method) throws IllegalAccessException, InstantiationException {
        Object fallbackObject = FALLBACK_OBJ_CACHE.get(method);
        if (fallbackObject != null) {
            return fallbackObject;
        }
        // fallback
        if (!void.class.isAssignableFrom(fallback)) {
            fallbackObject = ApplicationContextUtils.getBean(applicationContext, fallback);
            if (fallbackObject == null) {
                fallbackObject = fallback.newInstance();
            }
        }
        if (fallbackObject == null) {
            fallbackObject = EmptyObject.INSTANCE;
        }
        FALLBACK_OBJ_CACHE.put(method, fallbackObject);

        return fallbackObject;

    }

    public static class EmptyObject {

        public static final EmptyObject INSTANCE = new EmptyObject();
    }
}
