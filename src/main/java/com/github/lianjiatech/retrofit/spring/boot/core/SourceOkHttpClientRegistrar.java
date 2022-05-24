package com.github.lianjiatech.retrofit.spring.boot.core;

/**
 * SourceOkHttpClientRegistry注册器
 * @author 陈添明
 * @since 2022/5/24 8:13 下午
 */
public interface SourceOkHttpClientRegistrar {

    /**
     * 向#{@link SourceOkHttpClientRegistry}注册数据
     * @param registry SourceOkHttpClientRegistry
     */
    void register(SourceOkHttpClientRegistry registry);
}
