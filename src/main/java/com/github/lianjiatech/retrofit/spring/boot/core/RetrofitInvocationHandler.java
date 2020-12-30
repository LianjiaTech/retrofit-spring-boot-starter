package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.config.DegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author 陈添明
 */
public class RetrofitInvocationHandler implements InvocationHandler {

    private final Object source;

    private final DegradeProperty degradeProperty;

    private Object fallback;

    private FallbackFactory<?> fallbackFactory;


    public RetrofitInvocationHandler(Object source, Object fallback, FallbackFactory<?> fallbackFactory, RetrofitProperties retrofitProperties) {
        this.source = source;
        this.degradeProperty = retrofitProperties.getDegrade();
        this.fallback = fallback;
        this.fallbackFactory = fallbackFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(source, args);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            Object fallbackObject = getFallbackObject(cause);
            // 熔断逻辑
            if (cause instanceof RetrofitBlockException && degradeProperty.isEnable() && fallbackObject != null) {
                return method.invoke(fallbackObject, args);
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
