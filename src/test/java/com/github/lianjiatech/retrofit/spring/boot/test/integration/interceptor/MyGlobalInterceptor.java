package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.GlobalInterceptor;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陈添明
 * @since 2023/12/17 10:54 上午
 */
@Component
public class MyGlobalInterceptor implements GlobalInterceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        // response的Header加上global
        return response.newBuilder().header("global", "true").build();
    }
}
