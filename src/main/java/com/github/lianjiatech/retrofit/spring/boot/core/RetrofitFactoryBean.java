package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
import com.github.lianjiatech.retrofit.spring.boot.config.GlobalTimeoutProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.core.reactive.*;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProxy;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.*;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;
import com.github.lianjiatech.retrofit.spring.boot.timeout.TimeoutCallFactory;
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
     * 解析当前 {@code @RetrofitClient} 接口的完整配置元信息，供 Actuator Endpoint 等上层只读消费。
     *
     * <p>该方法<b>不触发 {@link #getObject()}</b>（即不创建 Retrofit 产品代理），仅读取注解与
     * {@link RetrofitConfigBean} 中已注册的状态，因此可安全地对所有声明的 client 调用。超时/连接池中
     * 值为 {@link Constants#INVALID_VALUE} 的字段会按与 {@link #createOkHttpClient} 完全一致的规则
     * 解析为全局兜底值，并记入 {@code inheritedFields}，保证 Endpoint 展示与真实构建行为一致。
     */
    public RetrofitClientResolution describe() {
        RetrofitConfigBean cfg = retrofitConfigBean();
        RetrofitProperties properties = cfg.getRetrofitProperties();
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        Objects.requireNonNull(retrofitClient,
                "@RetrofitClient annotation not found on " + retrofitInterface.getName());

        RetrofitClientResolution resolution = new RetrofitClientResolution();
        resolution.setInterfaceName(retrofitInterface.getName());
        resolution.setBaseUrl(emptyToNull(retrofitClient.baseUrl()));
        resolution.setResolvedBaseUrl(cfg.getBaseUrl(retrofitInterface));
        resolution.setServiceId(emptyToNull(retrofitClient.serviceId()));
        resolution.setPath(emptyToNull(retrofitClient.path()));
        resolution.setConverterFactories(classNames(retrofitClient.converterFactories()));
        resolution.setCallAdapterFactories(classNames(retrofitClient.callAdapterFactories()));
        resolution.setFallback(voidToNull(retrofitClient.fallback()));
        resolution.setFallbackFactory(voidToNull(retrofitClient.fallbackFactory()));
        resolution.setErrorDecoder(retrofitClient.errorDecoder().getName());
        resolution.setValidateEagerly(retrofitClient.validateEagerly());

        String sourceOkHttpClient = retrofitClient.sourceOkHttpClient();
        boolean useGlobalClient = Constants.NO_SOURCE_OK_HTTP_CLIENT.equals(sourceOkHttpClient);
        resolution.setSourceOkHttpClient(useGlobalClient ? null : sourceOkHttpClient);
        resolution.setTimeoutEffective(useGlobalClient);
        if (useGlobalClient) {
            resolution.setTimeout(resolveTimeout(retrofitClient, properties.getGlobalTimeout()));
            resolution.setPool(resolvePool(retrofitClient, properties.getGlobalConnectionPool()));
        }

        resolution.setLogging(resolveLogging());
        resolution.setRetry(resolveRetry());
        resolution.setDegrade(resolveDegrade());
        return resolution;
    }

    private RetrofitClientResolution.Timeout resolveTimeout(RetrofitClient retrofitClient, GlobalTimeoutProperty global) {
        RetrofitClientResolution.Timeout timeout = new RetrofitClientResolution.Timeout();
        List<String> inherited = new ArrayList<>(4);
        Timeout annotation = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, Timeout.class);
        int connectMs = annotation != null ? annotation.connectTimeoutMs() : Constants.INVALID_VALUE;
        int readMs = annotation != null ? annotation.readTimeoutMs() : Constants.INVALID_VALUE;
        int writeMs = annotation != null ? annotation.writeTimeoutMs() : Constants.INVALID_VALUE;
        int callMs = annotation != null ? annotation.callTimeoutMs() : Constants.INVALID_VALUE;
        timeout.setConnectMs(resolveTimeoutField(connectMs, global.getConnectTimeoutMs(),
                "connectMs", inherited));
        timeout.setReadMs(resolveTimeoutField(readMs, global.getReadTimeoutMs(),
                "readMs", inherited));
        timeout.setWriteMs(resolveTimeoutField(writeMs, global.getWriteTimeoutMs(),
                "writeMs", inherited));
        timeout.setCallMs(resolveTimeoutField(callMs, global.getCallTimeoutMs(),
                "callMs", inherited));
        timeout.setInheritedFields(inherited);
        return timeout;
    }

    private int resolveTimeoutField(int clientValue, int globalValue, String fieldName, List<String> inherited) {
        if (clientValue == Constants.INVALID_VALUE) {
            inherited.add(fieldName);
            return globalValue;
        }
        return clientValue;
    }

    private RetrofitClientResolution.Pool resolvePool(RetrofitClient retrofitClient, GlobalConnectionPoolProperty global) {
        RetrofitClientResolution.Pool pool = new RetrofitClientResolution.Pool();
        List<String> inherited = new ArrayList<>(2);
        if (retrofitClient.maxIdleConnections() == Constants.INVALID_VALUE) {
            pool.setMaxIdleConnections(global.getMaxIdleConnections());
            inherited.add("maxIdleConnections");
        } else {
            pool.setMaxIdleConnections(retrofitClient.maxIdleConnections());
        }
        if (retrofitClient.keepAliveDurationMs() == Constants.INVALID_VALUE) {
            pool.setKeepAliveDurationMs(global.getKeepAliveDurationMs());
            inherited.add("keepAliveDurationMs");
        } else {
            pool.setKeepAliveDurationMs(retrofitClient.keepAliveDurationMs());
        }
        pool.setInheritedFields(inherited);
        return pool;
    }

    private RetrofitClientResolution.Logging resolveLogging() {
        RetrofitClientResolution.Logging info = new RetrofitClientResolution.Logging();
        Logging logging = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, Logging.class);
        if (logging == null) {
            info.setSource("global");
            return info;
        }
        info.setSource("interface");
        info.setEnable(logging.enable());
        info.setLogLevel(logging.logLevel().name());
        info.setLogStrategy(logging.logStrategy().name());
        info.setAggregate(logging.aggregate());
        info.setLogName(emptyToNull(logging.logName()));
        info.setRedactHeaders(logging.redactHeaders().length == 0 ? null : Arrays.asList(logging.redactHeaders()));
        return info;
    }

    private RetrofitClientResolution.Retry resolveRetry() {
        RetrofitClientResolution.Retry info = new RetrofitClientResolution.Retry();
        Retry retry = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, Retry.class);
        if (retry == null) {
            info.setSource("global");
            return info;
        }
        info.setSource("interface");
        info.setEnable(retry.enable());
        info.setMaxRetries(retry.maxRetries());
        info.setIntervalMs(retry.intervalMs());
        List<String> rules = new ArrayList<>(retry.retryRules().length);
        for (RetryRule rule : retry.retryRules()) {
            rules.add(rule.name());
        }
        info.setRetryRules(rules);
        return info;
    }

    private RetrofitClientResolution.Degrade resolveDegrade() {
        RetrofitClientResolution.Degrade info = new RetrofitClientResolution.Degrade();
        RetrofitDegrade retrofitDegrade = retrofitConfigBean().getRetrofitDegrade();
        if (retrofitDegrade == null) {
            info.setEnabled(false);
            info.setType(RetrofitDegrade.NONE);
            return info;
        }
        info.setEnabled(retrofitDegrade.isEnableDegrade(retrofitInterface));
        info.setType(retrofitConfigBean().getRetrofitProperties().getDegrade().getDegradeType());
        return info;
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private static String voidToNull(Class<?> clazz) {
        return void.class.isAssignableFrom(clazz) ? null : clazz.getName();
    }

    private static List<String> classNames(Class<?>[] classes) {
        if (classes == null || classes.length == 0) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>(classes.length);
        for (Class<?> clazz : classes) {
            names.add(clazz.getName());
        }
        return names;
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

        // 类级 @Timeout 覆盖超时（对 baseClient 和 sourceOkHttpClient 均生效）
        Timeout classTimeout = AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, Timeout.class);
        if (classTimeout != null) {
            applyTimeoutOverrides(okHttpClientBuilder, classTimeout);
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
        // 指标拦截器位于重试之后、日志之前：每次 HTTP 尝试单独计入 timer，与日志看到的耗时一致。
        // 当 cfg.getMetricsInterceptor() 为 null（未引入 Micrometer 或显式禁用）时跳过。
        Interceptor metricsInterceptor = cfg.getMetricsInterceptor();
        if (metricsInterceptor != null) {
            okHttpClientBuilder.addInterceptor(metricsInterceptor);
        }
        okHttpClientBuilder.addInterceptor(cfg.getLoggingInterceptor());
        List<NetworkInterceptor> networkInterceptors = cfg.getNetworkInterceptors();
        if (!CollectionUtils.isEmpty(networkInterceptors)) {
            networkInterceptors.forEach(okHttpClientBuilder::addNetworkInterceptor);
        }
        return okHttpClientBuilder.build();
    }

    private boolean hasMethodLevelTimeout(Class<?> retrofitInterface) {
        for (Method method : retrofitInterface.getMethods()) {
            if (AnnotatedElementUtils.findMergedAnnotation(method, Timeout.class) != null) {
                return true;
            }
        }
        return false;
    }

    private void applyTimeoutOverrides(OkHttpClient.Builder builder, Timeout timeout) {
        if (timeout.connectTimeoutMs() != Constants.INVALID_VALUE) {
            builder.connectTimeout(timeout.connectTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        if (timeout.readTimeoutMs() != Constants.INVALID_VALUE) {
            builder.readTimeout(timeout.readTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        if (timeout.writeTimeoutMs() != Constants.INVALID_VALUE) {
            builder.writeTimeout(timeout.writeTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        if (timeout.callTimeoutMs() != Constants.INVALID_VALUE) {
            builder.callTimeout(timeout.callTimeoutMs(), TimeUnit.MILLISECONDS);
        }
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
        // 仅当存在方法级 @Timeout 时才引入 TimeoutCallFactory，否则无额外开销
        if (hasMethodLevelTimeout(retrofitInterface)) {
            callFactory = new TimeoutCallFactory(callFactory, retrofitInterface);
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
