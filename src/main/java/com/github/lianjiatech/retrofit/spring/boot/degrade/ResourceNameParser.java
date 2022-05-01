package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * @author 陈添明
 * @since 2022/4/30 3:55 下午
 */
public interface ResourceNameParser {

    String HTTP_OUT = "HTTP_OUT";

    Map<Method, String> RESOURCE_NAME_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 提取资源名称，支持缓存
     * @param method 方法
     * @return 资源名称
     */
    default String extractResourceNameCache(Method method) {
        String resourceName = RESOURCE_NAME_CACHE.get(method);
        if (resourceName != null) {
            return resourceName;
        }
        resourceName = extractResourceName(method);
        RESOURCE_NAME_CACHE.put(method, resourceName);
        return resourceName;
    }

    /**
     * 提取资源名称
     * @param method 方法
     * @return 资源名称
     */
    String extractResourceName(Method method);

    /**
     * 解析方法路径
     * @param method 方法
     * @return 方法路径
     */
    default HttpMethodPath parseHttpMethodPath(Method method) {

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
}
