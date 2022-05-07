package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProxy;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercepts;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 */
public class RetrofitFactoryBean<T> implements FactoryBean<T>, EnvironmentAware, ApplicationContextAware {

    private final Class<T> retrofitInterface;

    private Environment environment;

    private RetrofitProperties retrofitProperties;

    private RetrofitConfigBean retrofitConfigBean;

    private ApplicationContext applicationContext;

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    public T getObject() throws Exception {
        T source = createRetrofit().create(retrofitInterface);
        if (!isEnableDegrade(retrofitInterface)) {
            return source;
        }
        retrofitConfigBean.getRetrofitDegrade().loadDegradeRules(retrofitInterface);
        return DegradeProxy.create(source, retrofitInterface, applicationContext);
    }

    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        RetrofitDegrade retrofitDegrade = retrofitConfigBean.getRetrofitDegrade();
        if (retrofitDegrade == null) {
            return false;
        }
        return retrofitDegrade.isEnableDegrade(retrofitInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.retrofitInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private okhttp3.ConnectionPool parseConnectionPool() {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        String poolName = retrofitClient.poolName();
        Map<String, ConnectionPool> poolRegistry = retrofitConfigBean.getPoolRegistry();
        Assert.notNull(poolRegistry, "poolRegistry does not exist! Please set retrofitConfigBean.poolRegistry!");
        ConnectionPool connectionPool = poolRegistry.get(poolName);
        Assert.notNull(connectionPool,
                "The connection pool corresponding to the current poolName does not exist! poolName = " + poolName);
        return connectionPool;
    }

    private OkHttpClient createOkHttpClient() throws IllegalAccessException, InvocationTargetException {
        OkHttpClient.Builder okHttpClientBuilder = createOkHttpClientBuilder();
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        if (isEnableDegrade(retrofitInterface)) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getRetrofitDegrade());
        }
        if (StringUtils.hasText(retrofitClient.serviceId())) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getServiceChooseInterceptor());
        }
        okHttpClientBuilder.addInterceptor(retrofitConfigBean.getErrorDecoderInterceptor());
        findInterceptorByAnnotation().forEach(okHttpClientBuilder::addInterceptor);
        retrofitConfigBean.getGlobalInterceptors().forEach(okHttpClientBuilder::addInterceptor);
        okHttpClientBuilder.addInterceptor(retrofitConfigBean.getRetryInterceptor());
        okHttpClientBuilder.addInterceptor(retrofitConfigBean.getLoggingInterceptor());
        retrofitConfigBean.getNetworkInterceptors().forEach(okHttpClientBuilder::addInterceptor);
        return okHttpClientBuilder.build();
    }

    private OkHttpClient.Builder createOkHttpClientBuilder() throws InvocationTargetException, IllegalAccessException {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        Method method = findOkHttpClientBuilderMethod();
        if (method != null) {
            return (OkHttpClient.Builder)method.invoke(null);
        }
        okhttp3.ConnectionPool connectionPool = parseConnectionPool();
        final int connectTimeoutMs = retrofitClient.connectTimeoutMs() == -1
                ? retrofitProperties.getGlobalConnectTimeoutMs() : retrofitClient.connectTimeoutMs();
        final int readTimeoutMs = retrofitClient.readTimeoutMs() == -1 ? retrofitProperties.getGlobalReadTimeoutMs()
                : retrofitClient.readTimeoutMs();
        final int writeTimeoutMs = retrofitClient.writeTimeoutMs() == -1
                ? retrofitProperties.getGlobalWriteTimeoutMs() : retrofitClient.writeTimeoutMs();
        final int callTimeoutMs = retrofitClient.callTimeoutMs() == -1 ? retrofitProperties.getGlobalCallTimeoutMs()
                : retrofitClient.callTimeoutMs();

        // Construct an OkHttpClient object
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(callTimeoutMs, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(retrofitClient.retryOnConnectionFailure())
                .followRedirects(retrofitClient.followRedirects())
                .followSslRedirects(retrofitClient.followSslRedirects())
                .pingInterval(retrofitClient.pingIntervalMs(), TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);
    }

    private Method findOkHttpClientBuilderMethod() {
        Method[] methods = retrofitInterface.getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())
                    && method.isAnnotationPresent(OkHttpClientBuilder.class)
                    && method.getReturnType().equals(OkHttpClient.Builder.class)) {
                return method;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Interceptor> findInterceptorByAnnotation() {
        Annotation[] classAnnotations = AnnotationUtils.getAnnotations(retrofitInterface);
        List<Interceptor> interceptors = new ArrayList<>();
        // 找出被@InterceptMark标记的注解。Find the annotation marked by @InterceptMark
        List<Annotation> interceptAnnotations = new ArrayList<>();
        for (Annotation classAnnotation : Objects.requireNonNull(classAnnotations)) {
            Class<? extends Annotation> annotationType = classAnnotation.annotationType();
            if (annotationType.isAnnotationPresent(InterceptMark.class)) {
                interceptAnnotations.add(classAnnotation);
            }
            if (classAnnotation instanceof Intercepts) {
                Intercept[] value = ((Intercepts)classAnnotation).value();
                interceptAnnotations.addAll(Arrays.asList(value));
            }
        }
        for (Annotation interceptAnnotation : interceptAnnotations) {
            // 获取注解属性数据。Get annotation attribute data
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(interceptAnnotation);
            Object handler = annotationAttributes.get("handler");
            Assert.notNull(handler,
                    "@InterceptMark annotations must be configured: Class<? extends BasePathMatchInterceptor> handler()");
            Assert.notNull(annotationAttributes.get("include"),
                    "@InterceptMark annotations must be configured: String[] include()");
            Assert.notNull(annotationAttributes.get("exclude"),
                    "@InterceptMark annotations must be configured: String[] exclude()");
            Class<? extends BasePathMatchInterceptor> interceptorClass =
                    (Class<? extends BasePathMatchInterceptor>)handler;
            BasePathMatchInterceptor interceptor =
                    AppContextUtils.getTargetInstanceIfNecessary(
                            AppContextUtils.getBeanOrNew(applicationContext, interceptorClass));
            Map<String, Object> annotationResolveAttributes = new HashMap<>(8);
            // 占位符属性替换。Placeholder attribute replacement
            annotationAttributes.forEach((key, value) -> {
                if (value instanceof String) {
                    String newValue = environment.resolvePlaceholders((String)value);
                    annotationResolveAttributes.put(key, newValue);
                } else {
                    annotationResolveAttributes.put(key, value);
                }
            });
            // 动态设置属性值。Set property value dynamically
            BeanExtendUtils.populate(interceptor, annotationResolveAttributes);
            interceptors.add(interceptor);
        }
        return interceptors;
    }

    private Retrofit createRetrofit() throws IllegalAccessException, InvocationTargetException {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        String baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, retrofitClient.baseUrl(), environment);

        OkHttpClient client = createOkHttpClient();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .validateEagerly(retrofitClient.validateEagerly())
                .client(client);

        combineAndCreate(retrofitClient.callAdapterFactories(), retrofitConfigBean.getGlobalCallAdapterFactoryClasses())
                .forEach(retrofitBuilder::addCallAdapterFactory);

        combineAndCreate(retrofitClient.converterFactories(), retrofitConfigBean.getGlobalConverterFactoryClasses())
                .forEach(retrofitBuilder::addConverterFactory);
        return retrofitBuilder.build();
    }

    private <E> List<E> combineAndCreate(Class<? extends E>[] clz, Class<? extends E>[] globalClz) {
        if (clz.length == 0 && globalClz.length == 0) {
            return Collections.emptyList();
        }
        List<Class<? extends E>> combineClz = new ArrayList<>(clz.length + globalClz.length);
        combineClz.addAll(Arrays.asList(clz));
        combineClz.addAll(Arrays.asList(globalClz));

        List<E> result = new ArrayList<>(combineClz.size());
        for (Class<? extends E> aClass : combineClz) {
            result.add(AppContextUtils.getBeanOrNew(applicationContext, aClass));
        }
        return result;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.retrofitConfigBean = applicationContext.getBean(RetrofitConfigBean.class);
        this.retrofitProperties = retrofitConfigBean.getRetrofitProperties();
    }
}
