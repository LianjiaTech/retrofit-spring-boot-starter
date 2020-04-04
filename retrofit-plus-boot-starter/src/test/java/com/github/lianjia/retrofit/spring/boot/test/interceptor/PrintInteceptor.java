package com.github.lianjia.retrofit.spring.boot.test.interceptor;

import com.github.lianjia.retrofit.plus.interceptor.BaseGlobalInterceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陈添明
 */
@Component
public class PrintInteceptor extends BaseGlobalInterceptor {
    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        System.out.println("=============test===========");
        return chain.proceed(request);
    }
}
