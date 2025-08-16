package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

import com.github.lianjiatech.retrofit.spring.boot.config.GlobalTimeoutProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.core.reactive.*;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProxy;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercepts;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;

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

    private RetrofitConfigBean retrofitConfigBean;

    private ApplicationContext applicationContext;

    public static final ConcurrentHashMap<Class<?>, String> BASE_URL_MAP = new ConcurrentHashMap<>();

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    public T getObject() {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        BaseUrlParser baseUrlParser = AppContextUtils.getBeanOrNew(applicationContext, retrofitClient.baseUrlParser());
        String baseUrl = baseUrlParser.parse(retrofitClient, environment);
        BASE_URL_MAP.put(retrofitInterface, baseUrl);
        T source = createRetrofit(retrofitClient, baseUrl).create(retrofitInterface);
        if (!isEnableDegrade(retrofitInterface)) {
            return source;
        }
        retrofitConfigBean.getRetrofitDegrade().loadDegradeRules(retrofitInterface, baseUrl);
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

    private OkHttpClient createOkHttpClient() {
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);

        OkHttpClient.Builder okHttpClientBuilder;
        if (Constants.NO_SOURCE_OK_HTTP_CLIENT.equals(Objects.requireNonNull(retrofitClient).sourceOkHttpClient())) {
            // 使用默认超时时间创建OkHttpClient
            GlobalTimeoutProperty globalTimeout = retrofitConfigBean.getRetrofitProperties().getGlobalTimeout();

            int connectTimeoutMs = retrofitClient.connectTimeoutMs() == Constants.INVALID_TIMEOUT_VALUE
                    ? globalTimeout.getConnectTimeoutMs() : retrofitClient.connectTimeoutMs();
            int readTimeoutMs = retrofitClient.readTimeoutMs() == Constants.INVALID_TIMEOUT_VALUE
                    ? globalTimeout.getReadTimeoutMs() : retrofitClient.readTimeoutMs();
            int writeTimeoutMs = retrofitClient.writeTimeoutMs() == Constants.INVALID_TIMEOUT_VALUE
                    ? globalTimeout.getWriteTimeoutMs() : retrofitClient.writeTimeoutMs();
            int callTimeoutMs = retrofitClient.callTimeoutMs() == Constants.INVALID_TIMEOUT_VALUE
                    ? globalTimeout.getCallTimeoutMs() : retrofitClient.callTimeoutMs();

            okHttpClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
                    .callTimeout(callTimeoutMs, TimeUnit.MILLISECONDS);
        } else {
            OkHttpClient sourceOkHttpClient = retrofitConfigBean.getSourceOkHttpClientRegistry()
                    .get(retrofitClient.sourceOkHttpClient());
            okHttpClientBuilder = sourceOkHttpClient.newBuilder();
        }

        if (isEnableDegrade(retrofitInterface)) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getRetrofitDegrade());
        }
        if (StringUtils.hasText(retrofitClient.serviceId())) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getServiceChooseInterceptor());
        }
        if (retrofitConfigBean.getRetrofitProperties().isEnableErrorDecoder()) {
            okHttpClientBuilder.addInterceptor(retrofitConfigBean.getErrorDecoderInterceptor());
        }
        findInterceptorByAnnotation().forEach(okHttpClientBuilder::addInterceptor);
        retrofitConfigBean.getGlobalInterceptors().forEach(okHttpClientBuilder::addInterceptor);
        okHttpClientBuilder.addInterceptor(retrofitConfigBean.getRetryInterceptor());
        okHttpClientBuilder.addInterceptor(retrofitConfigBean.getLoggingInterceptor());
        retrofitConfigBean.getNetworkInterceptors().forEach(okHttpClientBuilder::addNetworkInterceptor);
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

        OkHttpClient client = createOkHttpClient();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .validateEagerly(retrofitClient.validateEagerly())
                .client(client);

        // 添加配置或者指定的CallAdapterFactory
        List<Class<? extends CallAdapter.Factory>> callAdapterFactories = new ArrayList<>(2);
        callAdapterFactories.addAll(Arrays.asList(retrofitClient.callAdapterFactories()));
        callAdapterFactories.addAll(Arrays.asList(retrofitConfigBean.getGlobalCallAdapterFactoryClasses()));
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
        converterFactories.addAll(Arrays.asList(retrofitClient.converterFactories()));
        converterFactories.addAll(Arrays.asList(retrofitConfigBean.getGlobalConverterFactoryClasses()));
        converterFactories.forEach(converterFactoryClass -> retrofitBuilder
                .addConverterFactory(AppContextUtils.getBeanOrNew(applicationContext, converterFactoryClass)));

        return retrofitBuilder.build();
    }

    private void addReactiveCallAdapterFactory(Retrofit.Builder retrofitBuilder) {
        if (reactor3ClassExist()) {
            retrofitBuilder.addCallAdapterFactory(MonoCallAdapterFactory.INSTANCE);
        }
        if (rxjava2ClassExist()) {
            retrofitBuilder.addCallAdapterFactory(Rxjava2SingleCallAdapterFactory.INSTANCE);
            retrofitBuilder.addCallAdapterFactory(Rxjava2CompletableCallAdapterFactory.INSTANCE);
        }
        if (rxjava3ClassExist()) {
            retrofitBuilder.addCallAdapterFactory(Rxjava3SingleCallAdapterFactory.INSTANCE);
            retrofitBuilder.addCallAdapterFactory(Rxjava3CompletableCallAdapterFactory.INSTANCE);
        }
    }

    private boolean rxjava3ClassExist() {
        try {
            Class.forName("io.reactivex.rxjava3.core.Single");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean rxjava2ClassExist() {
        try {
            Class.forName("io.reactivex.Single");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean reactor3ClassExist() {
        try {
            Class.forName("reactor.core.publisher.Mono");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.retrofitConfigBean = applicationContext.getBean(RetrofitConfigBean.class);
    }
}
