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
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.annotation.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.annotation.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.annotation.Intercepts;
import com.github.lianjiatech.retrofit.spring.boot.annotation.OkHttpClientBuilder;
import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.config.DegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProxy;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeType;
import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 */
public class RetrofitFactoryBean<T> implements FactoryBean<T>, EnvironmentAware, ApplicationContextAware {

    private static final Map<Class<? extends CallAdapter.Factory>, CallAdapter.Factory> CALL_ADAPTER_FACTORIES_CACHE =
            new HashMap<>(4);

    private Class<T> retrofitInterface;

    private Environment environment;

    private RetrofitProperties retrofitProperties;

    private RetrofitConfigBean retrofitConfigBean;

    private ApplicationContext applicationContext;

    private static final Map<Class<? extends Converter.Factory>, Converter.Factory> CONVERTER_FACTORIES_CACHE =
            new HashMap<>(4);

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    public T getObject() throws Exception {
        checkRetrofitInterface();
        // source
        T source = createRetrofit().create(retrofitInterface);
        if (!isEnableSentinelDegrade(retrofitProperties.getDegrade(), retrofitInterface)) {
            return source;
        }
        // 启用代理
        loadDegradeRules();
        return DegradeProxy.create(source, retrofitInterface, applicationContext);
    }

    public boolean isEnableSentinelDegrade(DegradeProperty degradeProperty, Class<?> retrofitInterface) {
        if (!degradeProperty.isEnable()) {
            return false;
        }
        return AnnotationExtendUtils.isAnnotationPresent(retrofitInterface, SentinelDegrade.class);
    }

    private void loadDegradeRules() {
        if (retrofitProperties.getDegrade().getDegradeType() == DegradeType.SENTINEL) {
            loadSentinelDegradeRules();
        }
    }

    private void loadSentinelDegradeRules() {
        // 读取熔断配置
        Method[] methods = retrofitInterface.getMethods();
        List<DegradeRule> rules = new ArrayList<>();
        for (Method method : methods) {
            if (method.isDefault()) {
                continue;
            }
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            // 获取熔断配置
            SentinelDegrade sentinelDegrade = AnnotationExtendUtils.findAnnotation(method, SentinelDegrade.class);
            if (sentinelDegrade == null) {
                continue;
            }

            DegradeRule degradeRule = new DegradeRule()
                    .setCount(sentinelDegrade.count())
                    .setTimeWindow(sentinelDegrade.timeWindow())
                    .setGrade(sentinelDegrade.grade());
            degradeRule.setResource(retrofitConfigBean.getResourceNameParser().extractResourceName(method));
            rules.add(degradeRule);
        }
        DegradeRuleManager.loadRules(rules);
    }

    private void checkRetrofitInterface() {
        // check class type
        Assert.isTrue(retrofitInterface.isInterface(), "@RetrofitClient can only be marked on the interface type!");
        Method[] methods = retrofitInterface.getMethods();

        RetrofitClient retrofitClient = retrofitInterface.getAnnotation(RetrofitClient.class);
        Assert.isTrue(StringUtils.hasText(retrofitClient.baseUrl()) || StringUtils.hasText(retrofitClient.serviceId()),
                "@RetrofitClient's baseUrl and serviceId must be configured with one！");

        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            if (method.isAnnotationPresent(OkHttpClientBuilder.class)) {
                Assert.isTrue(returnType.equals(OkHttpClient.Builder.class),
                        "For methods annotated by @OkHttpClientBuilder, the return value must be OkHttpClient.Builder！");
                Assert.isTrue(Modifier.isStatic(method.getModifiers()),
                        "only static method can annotated by @OkHttpClientBuilder!");
                continue;
            }

            Assert.isTrue(!void.class.isAssignableFrom(returnType),
                    "The void keyword is not supported as the return type, please use java.lang.Void！ method="
                            + method);
            if (retrofitProperties.isDisableVoidReturnType()) {
                Assert.isTrue(!Void.class.isAssignableFrom(returnType),
                        "Configured to disable Void as the return value, please specify another return type!method="
                                + method);
            }
        }

        Class<?> fallbackClass = retrofitClient.fallback();
        if (!void.class.isAssignableFrom(fallbackClass)) {
            Assert.isTrue(retrofitInterface.isAssignableFrom(fallbackClass),
                    "The fallback type must implement the current interface！the fallback type is " + fallbackClass);
            Object fallback = AppContextUtils.getBeanOrNull(applicationContext, fallbackClass);
            Assert.notNull(fallback, "fallback  must be a valid spring bean! the fallback class is " + fallbackClass);
        }

        Class<?> fallbackFactoryClass = retrofitClient.fallbackFactory();
        if (!void.class.isAssignableFrom(fallbackFactoryClass)) {
            Assert.isTrue(FallbackFactory.class.isAssignableFrom(fallbackFactoryClass),
                    "The fallback factory type must implement FallbackFactory！the fallback factory is "
                            + fallbackFactoryClass);
            Object fallbackFactory = AppContextUtils.getBeanOrNull(applicationContext, fallbackFactoryClass);
            Assert.notNull(fallbackFactory,
                    "fallback factory  must be a valid spring bean! the fallback factory class is "
                            + fallbackFactoryClass);
        }
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
        RetrofitClient retrofitClient = retrofitInterface.getAnnotation(RetrofitClient.class);
        String poolName = retrofitClient.poolName();
        Map<String, ConnectionPool> poolRegistry = retrofitConfigBean.getPoolRegistry();
        Assert.notNull(poolRegistry, "poolRegistry does not exist! Please set retrofitConfigBean.poolRegistry!");
        ConnectionPool connectionPool = poolRegistry.get(poolName);
        Assert.notNull(connectionPool,
                "The connection pool corresponding to the current poolName does not exist! poolName = " + poolName);
        return connectionPool;
    }

    private OkHttpClient createOkHttpClient()
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        OkHttpClient.Builder okHttpClientBuilder = createOkHttpClientBuilder();
        RetrofitClient retrofitClient = retrofitInterface.getAnnotation(RetrofitClient.class);
        if (isEnableSentinelDegrade(retrofitProperties.getDegrade(), retrofitInterface)) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getDegradeInterceptor());
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
        RetrofitClient retrofitClient = retrofitInterface.getAnnotation(RetrofitClient.class);
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
    private List<Interceptor> findInterceptorByAnnotation()
            throws InstantiationException, IllegalAccessException {
        Annotation[] classAnnotations = retrofitInterface.getAnnotations();
        List<Interceptor> interceptors = new ArrayList<>();
        // 找出被@InterceptMark标记的注解。Find the annotation marked by @InterceptMark
        List<Annotation> interceptAnnotations = new ArrayList<>();
        for (Annotation classAnnotation : classAnnotations) {
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

    private Retrofit createRetrofit()
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        RetrofitClient retrofitClient = retrofitInterface.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();

        baseUrl = RetrofitUtils.convertBaseUrl(retrofitClient, baseUrl, environment);

        OkHttpClient client = createOkHttpClient();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .validateEagerly(retrofitClient.validateEagerly())
                .client(client);

        // 添加CallAdapter.Factory
        Class<? extends CallAdapter.Factory>[] callAdapterFactoryClasses = retrofitClient.callAdapterFactories();
        Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses =
                retrofitConfigBean.getGlobalCallAdapterFactoryClasses();
        List<CallAdapter.Factory> callAdapterFactories =
                getCallAdapterFactories(callAdapterFactoryClasses, globalCallAdapterFactoryClasses);
        if (!CollectionUtils.isEmpty(callAdapterFactories)) {
            callAdapterFactories.forEach(retrofitBuilder::addCallAdapterFactory);
        }
        // 添加Converter.Factory
        Class<? extends Converter.Factory>[] converterFactoryClasses = retrofitClient.converterFactories();
        Class<? extends Converter.Factory>[] globalConverterFactoryClasses =
                retrofitConfigBean.getGlobalConverterFactoryClasses();

        List<Converter.Factory> converterFactories =
                getConverterFactories(converterFactoryClasses, globalConverterFactoryClasses);
        if (!CollectionUtils.isEmpty(converterFactories)) {
            converterFactories.forEach(retrofitBuilder::addConverterFactory);
        }
        return retrofitBuilder.build();
    }

    private List<CallAdapter.Factory> getCallAdapterFactories(
            Class<? extends CallAdapter.Factory>[] callAdapterFactoryClasses,
            Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses)
            throws IllegalAccessException, InstantiationException {
        List<Class<? extends CallAdapter.Factory>> combineCallAdapterFactoryClasses = new ArrayList<>();

        if (callAdapterFactoryClasses != null && callAdapterFactoryClasses.length != 0) {
            combineCallAdapterFactoryClasses.addAll(Arrays.asList(callAdapterFactoryClasses));
        }

        if (globalCallAdapterFactoryClasses != null && globalCallAdapterFactoryClasses.length != 0) {
            combineCallAdapterFactoryClasses.addAll(Arrays.asList(globalCallAdapterFactoryClasses));
        }

        if (combineCallAdapterFactoryClasses.isEmpty()) {
            return Collections.emptyList();
        }

        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();

        for (Class<? extends CallAdapter.Factory> callAdapterFactoryClass : combineCallAdapterFactoryClasses) {
            CallAdapter.Factory callAdapterFactory = CALL_ADAPTER_FACTORIES_CACHE.get(callAdapterFactoryClass);
            if (callAdapterFactory == null) {
                callAdapterFactory = AppContextUtils.getBeanOrNew(applicationContext, callAdapterFactoryClass);
                CALL_ADAPTER_FACTORIES_CACHE.put(callAdapterFactoryClass, callAdapterFactory);
            }
            callAdapterFactories.add(callAdapterFactory);
        }
        return callAdapterFactories;
    }

    private List<Converter.Factory> getConverterFactories(Class<? extends Converter.Factory>[] converterFactoryClasses,
            Class<? extends Converter.Factory>[] globalConverterFactoryClasses)
            throws IllegalAccessException, InstantiationException {
        List<Class<? extends Converter.Factory>> combineConverterFactoryClasses = new ArrayList<>();

        if (converterFactoryClasses != null && converterFactoryClasses.length != 0) {
            combineConverterFactoryClasses.addAll(Arrays.asList(converterFactoryClasses));
        }

        if (globalConverterFactoryClasses != null && globalConverterFactoryClasses.length != 0) {
            combineConverterFactoryClasses.addAll(Arrays.asList(globalConverterFactoryClasses));
        }

        if (combineConverterFactoryClasses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Converter.Factory> converterFactories = new ArrayList<>();

        for (Class<? extends Converter.Factory> converterFactoryClass : combineConverterFactoryClasses) {
            Converter.Factory converterFactory = CONVERTER_FACTORIES_CACHE.get(converterFactoryClass);
            if (converterFactory == null) {
                converterFactory = AppContextUtils.getBeanOrNew(applicationContext, converterFactoryClass);
                CONVERTER_FACTORIES_CACHE.put(converterFactoryClass, converterFactory);
            }
            converterFactories.add(converterFactory);
        }
        return converterFactories;
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
