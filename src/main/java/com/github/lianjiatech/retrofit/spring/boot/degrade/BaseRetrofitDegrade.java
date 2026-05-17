package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;

/**
 * @author 陈添明
 * @since 2022/5/1 9:54 下午
 */
public abstract class BaseRetrofitDegrade implements RetrofitDegrade, ResourceNameParser, ApplicationContextAware {

    protected static final String HTTP_OUT = "HTTP_OUT";

    /**
     * 资源名缓存，作为实例字段，生命周期与当前 RetrofitDegrade Bean 一致。
     */
    protected final Map<Method, String> resourceNameCache = new ConcurrentHashMap<>(128);

    /**
     * 通过 ApplicationContext 懒查找 RetrofitConfigBean，以避免与 RetrofitDegrade Bean 在容器启动期形成循环依赖。
     */
    private ApplicationContext applicationContext;
    private volatile RetrofitConfigBean retrofitConfigBean;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 查找指定 Retrofit 接口当前注册的 baseUrl，由 {@link RetrofitConfigBean} 管理生命周期。
     */
    protected String lookupBaseUrl(Class<?> retrofitInterface) {
        RetrofitConfigBean cfg = retrofitConfigBean;
        if (cfg == null) {
            cfg = applicationContext.getBean(RetrofitConfigBean.class);
            retrofitConfigBean = cfg;
        }
        return cfg.getBaseUrl(retrofitInterface);
    }

    @Override
    public String parseResourceName(Method method, String baseUrl) {
        String resourceName = resourceNameCache.get(method);
        if (resourceName != null) {
            return resourceName;
        }
        HttpMethodPath httpMethodPath = parseHttpMethodPath(method);
        resourceName = formatResourceName(baseUrl, httpMethodPath);
        resourceNameCache.put(method, resourceName);
        return resourceName;
    }

    protected String formatResourceName(String baseUrl, HttpMethodPath httpMethodPath) {
        return HTTP_OUT + ":" + httpMethodPath.getMethod() + ":" + baseUrl + httpMethodPath.getPath();
    }

    protected boolean isDefaultOrStatic(Method method) {
        if (method.isDefault()) {
            return true;
        }
        return Modifier.isStatic(method.getModifiers());
    }
}
