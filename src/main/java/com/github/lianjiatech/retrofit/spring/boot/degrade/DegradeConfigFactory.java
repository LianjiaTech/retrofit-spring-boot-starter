package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.util.Map;

/**
 *
 * @param <T> 熔断器配置实例的类型
 * @author yukdawn@gmail.com 2022/4/23 21:55
 */
public interface DegradeConfigFactory<T> {

    /**
     * 此方法为获取熔断资源的默认配置，注意从此方法中获取的配置对象不可复用，一定要是新对象
     * 参数会自动注入
     * @param resourceName 资源名称
     * @param attrMap 集合内为从{@link Degrade}上继承出的注解上获取的属性集合
     * @return 熔断器配置实例
     */
    T newInstanceByDefault(String resourceName, Map<String, Object> attrMap);
}
