package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import okhttp3.Response;

/**
 * @author 陈添明
 * @since 2023/12/17 10:23 上午
 */
@Component
public class PathMatchInterceptor extends BasePathMatchInterceptor {
    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response的Header加上path.match
        return response.newBuilder().header("path.match", "true").build();
    }
}
