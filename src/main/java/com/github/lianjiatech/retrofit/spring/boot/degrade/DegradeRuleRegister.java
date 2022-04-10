package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:14
 */
public interface DegradeRuleRegister {

    /**
     * 批量注册规则
     * @param retrofitDegradeRuleList 规则描述对象集合
     */
    void batchRegister(List<RetrofitDegradeRule> retrofitDegradeRuleList);

    /**
     * 使用规则代理执行目标方法
     * @param resourceName 资源名称
     * @param func 目标方法
     * @return okhttp响应
     * @throws IOException IOException
     */
    Response exec(String resourceName, DegradeProxyMethod<Response> func) throws IOException;

    @FunctionalInterface
    interface DegradeProxyMethod<R>{
        R get() throws IOException;
    }
}
