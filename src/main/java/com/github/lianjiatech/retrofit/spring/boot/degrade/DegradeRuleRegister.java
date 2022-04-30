package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import okhttp3.Response;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:14
 */
public interface DegradeRuleRegister<T> {

    /**
     * 注册规则
     * @param resourceName 资源名称
     * @param rule 规则描述
     */
    void register(String resourceName, T rule);

    /**
     * 此方法为获取熔断资源的默认配置，注意从此方法中获取的配置对象不可复用，一定要是新对象
     * 方法参数会自动注入
     * @param attrMap 集合内为从{@link Degrade}上继承出的注解上获取的属性集合
     * @return 熔断器配置实例
     */
    T newInstanceByDefault(Map<String, Object> attrMap);

    default void registerByNewConfig(String resourceName, Map<String, Object> attrMap){
        this.register(resourceName, this.newInstanceByDefault(attrMap));
    }

    /**
     * 使用规则代理执行目标方法
     * @param resourceName 资源名称
     * @param func 目标方法
     * @return okhttp响应
     * @throws IOException IOException
     */
    Response exec(String resourceName, DegradeProxyMethod<Response> func) throws IOException;

    /**
     * 检查value是否为空，不为空则转换值类型，为空则返回默认值
     * @param clazz 值类型
     * @param value 值
     * @param defaultValue 默认值
     * @param <V> 要转换到的值类型
     * @return 值
     */
    default <V> V convertOrDefault(Class<V> clazz, Object value, V defaultValue) {
        return Optional.ofNullable(value).map(clazz::cast).orElse(defaultValue);
    }

    @FunctionalInterface
    interface DegradeProxyMethod<R>{
        R get() throws IOException;
    }
}
