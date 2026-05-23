package com.github.lianjiatech.retrofit.spring.boot.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;

/**
 * SourceOkHttpClient注册中心
 *
 * @author 陈添明
 * @since 2022/5/24 7:54 下午
 */
public class SourceOkHttpClientRegistry {

    /**
     * 使用 {@link ConcurrentHashMap} 而非 {@link java.util.HashMap}：register/get 是 public API，
     * 用户可能在 {@link SourceOkHttpClientRegistrar#register(SourceOkHttpClientRegistry)} 之外的时机
     * 调用 register，与请求路径上的 get 形成并发读写。HashMap 在并发修改下行为未定义
     * （rehash 死循环、size 错乱），ConcurrentHashMap 提供安全保证。
     */
    private final Map<String, OkHttpClient> okHttpClientMap;

    private final List<SourceOkHttpClientRegistrar> registrars;

    public SourceOkHttpClientRegistry(List<SourceOkHttpClientRegistrar> registrars) {
        this.registrars = registrars;
        this.okHttpClientMap = new ConcurrentHashMap<>(4);
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
