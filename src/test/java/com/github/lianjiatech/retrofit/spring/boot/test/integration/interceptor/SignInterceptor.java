package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import lombok.Setter;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 加签拦截器，取加签值，放到header中
 *
 * @author 陈添明
 */
@Component
@Setter
public class SignInterceptor extends BasePathMatchInterceptor {

    private String accessKeyId;

    private String accessKeySecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newReq = request.newBuilder()
                .addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret)
                .build();
        Response response = chain.proceed(newReq);
        return response.newBuilder().addHeader("accessKeyId", accessKeyId)
                .addHeader("accessKeySecret", accessKeySecret).build();
    }
}
