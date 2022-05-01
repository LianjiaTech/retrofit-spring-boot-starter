package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;

/**
 * @author 陈添明
 */
public class DefaultResourceNameParser implements ResourceNameParser, EnvironmentAware {

    protected Environment environment;

    @Override
    public String extractResourceName(Method method) {
        String resourceName;
        Class<?> declaringClass = method.getDeclaringClass();
        RetrofitClient retrofitClient = declaringClass.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, baseUrl, environment);
        HttpMethodPath httpMethodPath = parseHttpMethodPath(method);
        resourceName =
                String.format("%s:%s:%s", HTTP_OUT, httpMethodPath.getMethod(), baseUrl + httpMethodPath.getPath());
        return resourceName;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
