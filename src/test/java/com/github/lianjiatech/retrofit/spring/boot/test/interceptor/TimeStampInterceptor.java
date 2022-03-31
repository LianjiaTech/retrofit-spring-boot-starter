package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TimeStampInterceptor extends BasePathMatchInterceptor {

    @Value("${test.baseUrl}")
    private String baseUrl;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        long timestamp = System.currentTimeMillis();
        HttpUrl newUrl = url.newBuilder()
                .addQueryParameter("timestamp", String.valueOf(timestamp))
                .build();
        Request newRequest = request.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newRequest);
    }
}
