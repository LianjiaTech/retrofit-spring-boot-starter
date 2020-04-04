package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import com.github.lianjiatech.retrofit.plus.interceptor.BasePathMatchInterceptor;
import lombok.Setter;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * 加签拦截器
 * 取加签值，放到header中
 *
 * @author 陈添明
 */
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", resolvePlaceholders(accessKeyId))
                .addHeader("accessKeySecret", resolvePlaceholders(accessKeySecret))
                .build();
        return chain.proceed(newReq);
    }
}
