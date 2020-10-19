package com.github.lianjiatech.retrofit.spring.boot.degrade;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.env.Environment;
import retrofit2.Invocation;
import retrofit2.http.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈添明
 */
public abstract class BaseDegradeInterceptor implements Interceptor {

    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private static Map<Method, String> METHOD_URL_MAPPING = new ConcurrentHashMap<>(128);

    private static Map<Method, String> METHOD_PATH_MAPPING = new ConcurrentHashMap<>(128);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        assert invocation != null;
        Method method = invocation.method();
        String url = METHOD_URL_MAPPING.get(method);
        if (url != null) {
            return degradeIntercept(url, chain);
        }
        Class<?> declaringClass = method.getDeclaringClass();
        RetrofitClient retrofitClient = declaringClass.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, baseUrl, environment);
        String methodPath = parseMethodPath(method, request);
        url = baseUrl + methodPath;
        METHOD_URL_MAPPING.put(method, url);
        return degradeIntercept(url, chain);
    }

    private String parseMethodPath(Method method, Request request) {

        String methodPath = METHOD_PATH_MAPPING.get(method);
        if (methodPath != null) {
            return methodPath;
        }

        if (method.isAnnotationPresent(HTTP.class)) {
            HTTP http = method.getAnnotation(HTTP.class);
            methodPath = http.path();
            METHOD_PATH_MAPPING.put(method, methodPath);
            return methodPath;
        }

        String reqMethod = request.method();

        switch (reqMethod) {
            case "GET": {
                GET get = method.getAnnotation(GET.class);
                methodPath = get.value();
                break;
            }
            case "POST": {
                POST post = method.getAnnotation(POST.class);
                methodPath = post.value();
                break;
            }
            case "PUT": {
                PUT put = method.getAnnotation(PUT.class);
                methodPath = put.value();
                break;
            }
            case "DELETE": {
                DELETE delete = method.getAnnotation(DELETE.class);
                methodPath = delete.value();
                break;
            }
            case "HEAD": {
                HEAD head = method.getAnnotation(HEAD.class);
                methodPath = head.value();
                break;
            }
            default: {
                throw new IllegalArgumentException("Request method is illegal, method=" + method.getName());
            }
        }
        METHOD_PATH_MAPPING.put(method, methodPath);
        return methodPath;
    }

    /**
     * 熔断拦截处理
     *
     * @param url   请求url，支持RESTFul风格接口
     * @param chain 请求执行链
     * @return 请求响应
     * @throws RetrofitBlockException 如果触发熔断，抛出RetrofitBlockException异常！
     */
    protected abstract Response degradeIntercept(String url, Chain chain) throws RetrofitBlockException, IOException;
}
