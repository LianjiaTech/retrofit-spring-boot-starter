package com.github.lianjiatech.retrofit.plus.interceptor;

import okhttp3.Response;

import java.io.IOException;

/**
 * okhttp全局拦截器
 * 使用抽象类，方便后面进行功能升级
 *
 * @author 陈添明
 */
public abstract class BaseGlobalInterceptor implements RetrofitPlusInterceptor {


    @Override
    public final Response intercept(Chain chain) throws IOException {
        return doIntercept(chain);
    }

    /**
     * 执行拦截
     *
     * @param chain 拦截器链
     * @return http Response
     * @throws IOException 可能因为网络IO问题，抛出IOException
     */
    protected abstract Response doIntercept(Chain chain) throws IOException;
}
