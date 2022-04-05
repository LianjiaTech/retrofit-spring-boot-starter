package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.util.List;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:14
 */
public interface DegradeRuleRegister {

    void register(RetrofitDegradeRule retrofitDegradeRule);

    void batchRegister(List<RetrofitDegradeRule> retrofitDegradeRuleList);

    <T> T exec(String resourceName, DegradeProxyMethod<T> func) throws IOException;

    @FunctionalInterface
    interface DegradeProxyMethod<R>{
        R get() throws IOException;
    }
}
