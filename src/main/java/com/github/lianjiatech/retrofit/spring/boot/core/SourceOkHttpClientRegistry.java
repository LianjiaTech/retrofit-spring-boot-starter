package com.github.lianjiatech.retrofit.spring.boot.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import okhttp3.OkHttpClient;

/**
 * SourceOkHttpClient注册中心
 * @author 陈添明
 * @since 2022/5/24 7:54 下午
 */
public class SourceOkHttpClientRegistry {

    private final Map<String, OkHttpClient> okHttpClientMap;

    private final List<SourceOkHttpClientRegistrar> registrars;

    public SourceOkHttpClientRegistry(List<SourceOkHttpClientRegistrar> registrars) {
        this.registrars = registrars;
        this.okHttpClientMap = new HashMap<>(4);
    }

    @PostConstruct
    public void init() {
        if (registrars == null) {
            return;
        }
        registrars.forEach(registrar -> registrar.register(this));
    }

    public void register(String name, OkHttpClient okHttpClient) {
        okHttpClientMap.put(name, okHttpClient);
    }

    public OkHttpClient get(String name) {
        OkHttpClient okHttpClient = okHttpClientMap.get(name);
        Assert.notNull(okHttpClient, "Specified OkHttpClient not found! name=" + name);
        return okHttpClient;
    }

}
