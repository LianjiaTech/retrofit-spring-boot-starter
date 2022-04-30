package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import retrofit2.http.*;

/**
 * @author 陈添明
 */
public class DefaultResourceNameParser implements ResourceNameParser, EnvironmentAware {

    private static final String PREFIX = "HTTP_OUT";
    private static final Map<Method, String> RESOURCE_NAME_CACHE = new ConcurrentHashMap<>(128);

    private Environment env;

    @Override
    public String parseResourceName(Method method) {
        String resourceName = RESOURCE_NAME_CACHE.get(method);
        if (resourceName != null) {
            return resourceName;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        RetrofitClient retrofitClient = declaringClass.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, baseUrl, env);
        HttpMethodPath httpMethodPath = parseHttpMethodPath(method);
        resourceName = defineResourceName(baseUrl, httpMethodPath);
        RESOURCE_NAME_CACHE.put(method, resourceName);
        return resourceName;
    }

    protected String defineResourceName(String baseUrl, HttpMethodPath httpMethodPath) {
        return String.format("%s:%s:%s", PREFIX, httpMethodPath.getMethod(), baseUrl + httpMethodPath.getPath());
    }

    protected HttpMethodPath parseHttpMethodPath(Method method) {
        if (method.isAnnotationPresent(HTTP.class)) {
            HTTP http = method.getAnnotation(HTTP.class);
            return new HttpMethodPath(http.method(), http.path());
        }

        if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            return new HttpMethodPath("GET", get.value());
        }

        if (method.isAnnotationPresent(POST.class)) {
            POST post = method.getAnnotation(POST.class);
            return new HttpMethodPath("POST", post.value());
        }

        if (method.isAnnotationPresent(PUT.class)) {
            PUT put = method.getAnnotation(PUT.class);
            return new HttpMethodPath("PUT", put.value());
        }

        if (method.isAnnotationPresent(DELETE.class)) {
            DELETE delete = method.getAnnotation(DELETE.class);
            return new HttpMethodPath("DELETE", delete.value());
        }

        if (method.isAnnotationPresent(HEAD.class)) {
            HEAD head = method.getAnnotation(HEAD.class);
            return new HttpMethodPath("HEAD", head.value());
        }

        if (method.isAnnotationPresent(PATCH.class)) {
            PATCH patch = method.getAnnotation(PATCH.class);
            return new HttpMethodPath("PATCH", patch.value());
        }
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
