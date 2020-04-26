package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import com.github.lianjiatech.retrofit.plus.interceptor.BaseGlobalInterceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陈添明
 */
@Component
public class PrintInterceptor extends BaseGlobalInterceptor {
    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println("=============test===========");
        return chain.proceed(request);
    }
}
