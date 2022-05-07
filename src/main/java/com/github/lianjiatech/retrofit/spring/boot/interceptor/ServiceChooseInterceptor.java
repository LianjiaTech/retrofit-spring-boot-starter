package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Objects;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
public class ServiceChooseInterceptor implements Interceptor {

    protected final ServiceInstanceChooser serviceInstanceChooser;

    public ServiceChooseInterceptor(ServiceInstanceChooser serviceDiscovery) {
        this.serviceInstanceChooser = serviceDiscovery;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        Class<?> declaringClass = method.getDeclaringClass();
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(declaringClass, RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        if (StringUtils.hasText(baseUrl)) {
            return chain.proceed(request);
        }
        // serviceId服务发现
        String serviceId = retrofitClient.serviceId();
        URI uri = serviceInstanceChooser.choose(serviceId);
        HttpUrl url = request.url();
        HttpUrl newUrl = url.newBuilder()
                .scheme(uri.getScheme())
                .host(uri.getHost())
                .port(uri.getPort())
                .build();
        Request newReq = request.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newReq);
    }
}
