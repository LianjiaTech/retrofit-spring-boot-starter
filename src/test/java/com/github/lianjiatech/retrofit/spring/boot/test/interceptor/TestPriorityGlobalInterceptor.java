package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;

import okhttp3.Response;

/**
 * @author 陈添明
 * @since 2022/3/9 7:39 下午
 */
@Component
@Order(1)
public class TestPriorityGlobalInterceptor implements GlobalInterceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
