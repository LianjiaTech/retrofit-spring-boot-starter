package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author 陈添明
 */
public class RetrofitInvocationHandler implements InvocationHandler {

    private final Object source;

    private final RetrofitProperties retrofitProperties;

    private Object fallbackObject;

    public RetrofitInvocationHandler(Object source, Class<?> fallback, RetrofitProperties retrofitProperties) throws IllegalAccessException, InstantiationException {
        this.source = source;
        this.retrofitProperties = retrofitProperties;
        if (!void.class.isAssignableFrom(fallback)) {
            fallbackObject = fallback.newInstance();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(source, args);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            // 熔断逻辑
            if (cause instanceof RetrofitBlockException && retrofitProperties.isEnableDegrade() && fallbackObject != null) {
                return method.invoke(fallbackObject, args);
            }
            throw cause;
        }
    }
}
