package com.github.lianjiatech.retrofit.spring.boot.core;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * SPI：允许用户自定义 Retrofit 实例使用的 {@link Call.Factory}。
 * <p>
 * 将实现类注册为 Spring Bean 即生效；未注册时组件行为完全不变。
 * <p>
 * 框架会将为当前 {@code @RetrofitClient} 接口配置好的 OkHttpClient（含全部拦截器、超时、连接池等）
 * 作为参数传入。用户可以：
 * <ul>
 *     <li>基于 baseClient 做 newBuilder 派生（如动态 callTimeout）</li>
 *     <li>包装 baseClient 增加自定义逻辑</li>
 *     <li>完全忽略 baseClient，返回自己的 Call.Factory</li>
 *     <li>直接返回 baseClient（等价默认行为）</li>
 * </ul>
 *
 * @author 陈添明
 */
public interface CallFactoryConfigurer {

    /**
     * 为指定 {@code @RetrofitClient} 接口配置 {@link Call.Factory}。
     * <p>
     * 每个接口初始化时调用一次，返回的 Call.Factory 将作为该接口所有调用的 Call 创建器。
     *
     * @param retrofitInterface {@code @RetrofitClient} 注解的接口类
     * @param baseClient 框架配置的 OkHttpClient（含全部拦截器）
     * @return Retrofit 实例使用的 Call.Factory
     */
    Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient);
}
