package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.test.service.TestService;

import okhttp3.Request;
import okhttp3.Response;

//@Component
@Order(2)
public class SourceGlobalInterceptor implements GlobalInterceptor {

    @Autowired
    private TestService testService;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("source", "test")
                .build();
        System.out.println("===========执行全局重试===========");
        testService.test();
        return chain.proceed(newReq);
    }
}
