package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Annotation;
import java.util.*;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.github.lianjiatech.retrofit.spring.boot.config.GlobalConnectionPoolProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.core.reactive.*;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProxy;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.*;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;

import okhttp3.Call;
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

    private final Class<T> retrofitInterface;

    private Environment environment;

    /**
     * RetrofitConfigBean 通过 {@link #retrofitConfigBean()} 懒解析。
     * <p>
     * setApplicationContext 阶段不能直接 {@code getBean(RetrofitConfigBean.class)}：
     * 该 Bean 与 RetrofitFactoryBean 没有显式依赖，二者初始化顺序由 Spring 决定，
     * 用户若自定义 RetrofitConfigBean 并在其依赖链中引入更多 Bean，eager 查找
     * 有引发 BeanCurrentlyInCreationException 的风险。
     */
    private volatile RetrofitConfigBean retrofitConfigBean;

    private ApplicationContext applicationContext;

    /**
     * 类加载时一次性检测依赖是否存在，避免每次创建 Retrofit 实例都抛出 ClassNotFoundException。
     */
    private static final boolean RXJAVA3_PRESENT =
            ClassUtils.isPresent("io.reactivex.rxjava3.core.Single", RetrofitFactoryBean.class.getClassLoader());
    private static final boolean RXJAVA2_PRESENT =
            ClassUtils.isPresent("io.reactivex.Single", RetrofitFactoryBean.class.getClassLoader());
    private static final boolean REACTOR3_PRESENT =
            ClassUtils.isPresent("reactor.core.publisher.Mono", RetrofitFactoryBean.class.getClassLoader());

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    public T getObject() {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        Objects.requireNonNull(retrofitClient, "@RetrofitClient annotation not found on " + retrofitInterface.getName());
        BaseUrlParser baseUrlParser = AppContextUtils.getBeanOrNew(applicationContext, retrofitClient.baseUrlParser());
        String baseUrl = baseUrlParser.parse(retrofitClient, environment);
        retrofitConfigBean().registerBaseUrl(retrofitInterface, baseUrl);
        T source = createRetrofit(retrofitClient, baseUrl).create(retrofitInterface);
        if (!isEnableDegrade(retrofitInterface)) {
            return source;
        }
        retrofitConfigBean().getRetrofitDegrade().loadDegradeRules(retrofitInterface, baseUrl);
        return DegradeProxy.create(source, retrofitInterface, applicationContext);
    }

    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        RetrofitDegrade retrofitDegrade = retrofitConfigBean().getRetrofitDegrade();
        if (retrofitDegrade == null) {
            return false;
        }
        return retrofitDegrade.isEnableDegrade(retrofitInterface);
    }

    /**
     * 懒解析 RetrofitConfigBean。
     * <p>
     * 此处用 volatile + 单检的"benign race"模式即可，无需 synchronized：
     * <ul>
     *     <li>{@code FactoryBean.isSingleton()=true} 让 Spring 仅调用一次 {@code getObject()}，
     *     <li>{@code ApplicationContext.getBean} 本身线程安全，
     *     <li>{@code RetrofitConfigBean} 是 singleton — 多次 getBean 拿到同一引用，
     *         即使被多个线程同时写入 {@link #retrofitConfigBean}，结果完全一致；
     *     <li>{@code volatile} 保证写入对其它线程立即可见。
     * </ul>
     * 与 {@code BaseRetrofitDegrade.lookupBaseUrl} 的写法保持一致。
     * @return RetrofitConfigBean 实例
     */
    private RetrofitConfigBean retrofitConfigBean() {
        if (this.retrofitConfigBean == null) {
            this.retrofitConfigBean = applicationContext.getBean(RetrofitConfigBean.class);
        }
        return this.retrofitConfigBean;
    }

    @Override
    public Class<T> getObjectType() {
        return this.retrofitInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private OkHttpClient createOkHttpClient(RetrofitClient retrofitClient) {
        RetrofitConfigBean cfg = retrofitConfigBean();
        OkHttpClient.Builder okHttpClientBuilder;
        RetrofitProperties retrofitProperties = cfg.getRetrofitProperties();
        if (Constants.NO_SOURCE_OK_HTTP_CLIENT.equals(Objects.requireNonNull(retrofitClient).sourceOkHttpClient())) {
            // 基于共享的 baseOkHttpClient 派生，复用 connectionPool 与 dispatcher
            OkHttpClient baseClient = cfg.getBaseOkHttpClient();
            Objects.requireNonNull(baseClient, "baseOkHttpClient must not be null");
            okHttpClientBuilder = baseClient.newBuilder();

            // 仅在 @RetrofitClient 显式覆盖时覆盖超时；否则继承 base
            if (retrofitClient.connectTimeoutMs() != Constants.INVALID_VALUE) {
                okHttpClientBuilder.connectTimeout(retrofitClient.connectTimeoutMs(), TimeUnit.MILLISECONDS);
            }
            if (retrofitClient.readTimeoutMs() != Constants.INVALID_VALUE) {
                okHttpClientBuilder.readTimeout(retrofitClient.readTimeoutMs(), TimeUnit.MILLISECONDS);
            }
            if (retrofitClient.writeTimeoutMs() != Constants.INVALID_VALUE) {
                okHttpClientBuilder.writeTimeout(retrofitClient.writeTimeoutMs(), TimeUnit.MILLISECONDS);
            }
            if (retrofitClient.callTimeoutMs() != Constants.INVALID_VALUE) {
                okHttpClientBuilder.callTimeout(retrofitClient.callTimeoutMs(), TimeUnit.MILLISECONDS);
            }

            // 仅在显式覆盖连接池参数时才隔离一份新 ConnectionPool；否则共享 base 的连接池
            boolean overrideMaxIdle = retrofitClient.maxIdleConnections() != Constants.INVALID_VALUE;
            boolean overrideKeepAlive = retrofitClient.keepAliveDurationMs() != Constants.INVALID_VALUE;
            if (overrideMaxIdle || overrideKeepAlive) {
                GlobalConnectionPoolProperty globalConnectionPool = retrofitProperties.getGlobalConnectionPool();
                int maxIdleConnections = overrideMaxIdle
                        ? retrofitClient.maxIdleConnections() : globalConnectionPool.getMaxIdleConnections();
                long keepAliveDurationMs = overrideKeepAlive
                        ? retrofitClient.keepAliveDurationMs() : globalConnectionPool.getKeepAliveDurationMs();
                okHttpClientBuilder.connectionPool(
                        new ConnectionPool(maxIdleConnections, keepAliveDurationMs, TimeUnit.MILLISECONDS));
            }
        } else {
            OkHttpClient sourceOkHttpClient = cfg.getSourceOkHttpClientRegistry()
                    .get(retrofitClient.sourceOkHttpClient());
            okHttpClientBuilder = sourceOkHttpClient.newBuilder();
        }

        if (isEnableDegrade(retrofitInterface)) {
            okHttpClientBuilder.addInterceptor(cfg.getRetrofitDegrade());
        }
        if (StringUtils.hasText(retrofitClient.serviceId())) {
            okHttpClientBuilder.addInterceptor(cfg.getServiceChooseInterceptor());
        }
        if (retrofitProperties.isEnableErrorDecoder()) {
            okHttpClientBuilder.addInterceptor(cfg.getErrorDecoderInterceptor());
        }
        findInterceptorByAnnotation().forEach(okHttpClientBuilder::addInterceptor);
        List<GlobalInterceptor> globalInterceptors = cfg.getGlobalInterceptors();
        if (!CollectionUtils.isEmpty(globalInterceptors)) {
            globalInterceptors.forEach(okHttpClientBuilder::addInterceptor);
        }
        okHttpClientBuilder.addInterceptor(cfg.getRetryInterceptor());
        okHttpClientBuilder.addInterceptor(cfg.getLoggingInterceptor());
        List<NetworkInterceptor> networkInterceptors = cfg.getNetworkInterceptors();
        if (!CollectionUtils.isEmpty(networkInterceptors)) {
            networkInterceptors.forEach(okHttpClientBuilder::addNetworkInterceptor);
        }
        return okHttpClientBuilder.build();
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

    private Retrofit createRetrofit(RetrofitClient retrofitClient, String baseUrl) {

        OkHttpClient client = createOkHttpClient(retrofitClient);
        RetrofitConfigBean cfg = retrofitConfigBean();

        Call.Factory callFactory = client;
        CallFactoryConfigurer configurer = cfg.getCallFactoryConfigurer();
        if (configurer != null) {
            callFactory = configurer.configure(retrofitInterface, client);
        }

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .validateEagerly(retrofitClient.validateEagerly())
                .callFactory(callFactory);

        // 添加配置或者指定的CallAdapterFactory
        List<Class<? extends CallAdapter.Factory>> callAdapterFactories = new ArrayList<>(2);
        Class<? extends CallAdapter.Factory>[] retrofitCallAdapterFactories = retrofitClient.callAdapterFactories();
        if (retrofitCallAdapterFactories != null) {
            callAdapterFactories.addAll(Arrays.asList(retrofitCallAdapterFactories));
        }
        Class<? extends CallAdapter.Factory>[] globalCallAdapterFactoryClasses =
                cfg.getGlobalCallAdapterFactoryClasses();
        if (globalCallAdapterFactoryClasses != null) {
            callAdapterFactories.addAll(Arrays.asList(globalCallAdapterFactoryClasses));
        }

        callAdapterFactories.stream()
                // 过滤掉内置的CallAdapterFactory，因为后续会指定add
                .filter(adapterFactoryClass -> !InternalCallAdapterFactory.class.isAssignableFrom(adapterFactoryClass))
                .forEach(adapterFactoryClass -> retrofitBuilder
                        .addCallAdapterFactory(AppContextUtils.getBeanOrNew(applicationContext, adapterFactoryClass)));

        addReactiveCallAdapterFactory(retrofitBuilder);
        retrofitBuilder.addCallAdapterFactory(ResponseCallAdapterFactory.INSTANCE);
        retrofitBuilder.addCallAdapterFactory(BodyCallAdapterFactory.INSTANCE);

        // 添加配置或者指定的ConverterFactory
        List<Class<? extends Converter.Factory>> converterFactories = new ArrayList<>(4);
        Class<? extends Converter.Factory>[] retrofitConverterFactories = retrofitClient.converterFactories();
        if (retrofitConverterFactories != null) {
            converterFactories.addAll(Arrays.asList(retrofitConverterFactories));
        }
        Class<? extends Converter.Factory>[] globalConverterFactoryClasses =
                cfg.getGlobalConverterFactoryClasses();
        if (globalConverterFactoryClasses != null) {
            converterFactories.addAll(Arrays.asList(globalConverterFactoryClasses));
        }
        converterFactories.forEach(converterFactoryClass -> retrofitBuilder
                .addConverterFactory(AppContextUtils.getBeanOrNew(applicationContext, converterFactoryClass)));

        return retrofitBuilder.build();
    }

    private void addReactiveCallAdapterFactory(Retrofit.Builder retrofitBuilder) {
        if (REACTOR3_PRESENT) {
            retrofitBuilder.addCallAdapterFactory(MonoCallAdapterFactory.INSTANCE);
        }
        if (RXJAVA2_PRESENT) {
            retrofitBuilder.addCallAdapterFactory(Rxjava2SingleCallAdapterFactory.INSTANCE);
            retrofitBuilder.addCallAdapterFactory(Rxjava2CompletableCallAdapterFactory.INSTANCE);
        }
        if (RXJAVA3_PRESENT) {
            retrofitBuilder.addCallAdapterFactory(Rxjava3SingleCallAdapterFactory.INSTANCE);
            retrofitBuilder.addCallAdapterFactory(Rxjava3CompletableCallAdapterFactory.INSTANCE);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // RetrofitConfigBean 改为懒解析（见 retrofitConfigBean()），不在此处 eager getBean，
        // 避免与用户自定义 Bean 的初始化顺序冲突。
    }
}
