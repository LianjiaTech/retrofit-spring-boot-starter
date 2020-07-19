package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BaseGlobalInterceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SourceInterceptor extends BaseGlobalInterceptor {
    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("source", "test")
                .build();
        return chain.proceed(newReq);
    }
}