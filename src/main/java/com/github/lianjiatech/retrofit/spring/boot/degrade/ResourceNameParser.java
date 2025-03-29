package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;

import retrofit2.http.*;

/**
 * @author 陈添明
 * @since 2022/4/30 3:55 下午
 */
public interface ResourceNameParser {

    /**
     * 解析资源名称
     *
     * @param method  方法
     * @param baseUrl HTTP接口的baseUrl
     * @return 资源名称
     */
    String parseResourceName(Method method, String baseUrl);

    /**
     * 解析方法路径
     *
     * @param method 方法
     * @return 方法路径
     */
    default HttpMethodPath parseHttpMethodPath(Method method) {

        HTTP http = method.getAnnotation(HTTP.class);
        if (http != null) {
            return new HttpMethodPath(http.method(), http.path());
        }

        GET get = method.getAnnotation(GET.class);
        if (get != null) {
            return new HttpMethodPath("GET", get.value());
        }

        POST post = method.getAnnotation(POST.class);
        if (post != null) {
            return new HttpMethodPath("POST", post.value());
        }

        PUT put = method.getAnnotation(PUT.class);
        if (put != null) {
            return new HttpMethodPath("PUT", put.value());
        }

        DELETE delete = method.getAnnotation(DELETE.class);
        if (delete != null) {
            return new HttpMethodPath("DELETE", delete.value());
        }

        HEAD head = method.getAnnotation(HEAD.class);
        if (head != null) {
            return new HttpMethodPath("HEAD", head.value());
        }

        PATCH patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
            return new HttpMethodPath("PATCH", patch.value());
        }
        throw new UnsupportedOperationException("unsupported method!" + method);
    }
}
