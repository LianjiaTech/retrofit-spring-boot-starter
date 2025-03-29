package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈添明
 * @since 2022/5/1 9:54 下午
 */
public abstract class BaseRetrofitDegrade implements RetrofitDegrade, ResourceNameParser {

    protected static final String HTTP_OUT = "HTTP_OUT";

    protected static final Map<Method, String> RESOURCE_NAME_CACHE = new ConcurrentHashMap<>(128);

    @Override
    public String parseResourceName(Method method, String baseUrl) {
        String resourceName = RESOURCE_NAME_CACHE.get(method);
        if (resourceName != null) {
            return resourceName;
        }
        HttpMethodPath httpMethodPath = parseHttpMethodPath(method);
        resourceName = formatResourceName(baseUrl, httpMethodPath);
        RESOURCE_NAME_CACHE.put(method, resourceName);
        return resourceName;
    }

    protected String formatResourceName(String baseUrl, HttpMethodPath httpMethodPath) {
        return String.format("%s:%s:%s", HTTP_OUT, httpMethodPath.getMethod(), baseUrl + httpMethodPath.getPath());
    }

    protected boolean isDefaultOrStatic(Method method) {
        if (method.isDefault()) {
            return true;
        }
        return Modifier.isStatic(method.getModifiers());
    }
}
