package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import retrofit2.Invocation;

import java.io.IOException;
import java.net.URI;

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
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(invocation.service(), RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        if (StringUtils.hasText(baseUrl)) {
            return chain.proceed(request);
        }
        // serviceId服务发现
        String serviceId = retrofitClient.serviceId();
        URI uri = serviceInstanceChooser.choose(serviceId);

        HttpUrl url = request.url();
        int port = uri.getPort();
        String scheme = uri.getScheme();
        if (port <= 0 || port > 65535) {
            port = HttpUrl.defaultPort(scheme);
        }

        HttpUrl newUrl = url.newBuilder()
                .scheme(scheme)
                .host(uri.getHost())
                .port(port)
                .build();
        Request newReq = request.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newReq);
    }
}
