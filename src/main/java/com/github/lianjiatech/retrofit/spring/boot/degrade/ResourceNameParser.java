package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;

/**
 * 资源名称解析器
 * @author yukdawn@gmail.com 2022/5/1 0:32
 */
public interface ResourceNameParser {

    /**
     * 根据方法名提取资源名称
     * @param method 需要熔断器控制的method
     * @return resourceName
     */
    String parseResourceName(Method method);
}
