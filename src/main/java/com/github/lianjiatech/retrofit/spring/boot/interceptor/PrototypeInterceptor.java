package com.github.lianjiatech.retrofit.spring.boot.interceptor;


import okhttp3.Interceptor;

/**
 * 拦截器标记接口
 * 该接口的实现类在spring容器中的scope会自动修改为prototype
 *
 * @author 陈添明
 */
public interface PrototypeInterceptor extends Interceptor {
}
