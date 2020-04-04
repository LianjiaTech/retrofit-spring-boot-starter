package com.github.lianjia.retrofit.plus.core;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.github.lianjia.retrofit.plus.annotation.InterceptMark;
import com.github.lianjia.retrofit.plus.annotation.RetrofitClient;
import com.github.lianjia.retrofit.plus.config.Config;
import com.github.lianjia.retrofit.plus.config.PoolConfig;
import com.github.lianjia.retrofit.plus.interceptor.BaseGlobalInterceptor;
import com.github.lianjia.retrofit.plus.interceptor.LogInterceptor;
import com.github.lianjia.retrofit.plus.interceptor.BasePathMatchInterceptor;
import com.github.lianjia.retrofit.plus.util.BeanExtendUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈添明
 */
@Slf4j
public class RetrofitFactoryBean<T> implements FactoryBean<T>, EnvironmentAware, InitializingBean, ApplicationContextAware {

    @Setter
    @Getter
    private Class<T> retrofitInterface;

    private ConfigurableEnvironment environment;

    private Map<String, ConnectionPool> poolRegistry = new ConcurrentHashMap<>(16);

    @Setter
    @Getter
    private RetrofitHelper retrofitHelper;

    private ApplicationContext applicationContext;

    private Retrofit2ConverterFactory retrofit2ConverterFactory;

    private BodyCallAdapterFactory bodyCallAdapterFactory;

    private ResponseCallAdapterFactory responseCallAdapterFactory;

    private static final String PROTOTYPE = "prototype";
    private static final String DEFAULT_KEY = "default";

    public RetrofitFactoryBean() {
    }

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        if (log.isDebugEnabled()) {
            log.debug("当前retrofit接口为={}, global_config = {}", retrofitInterface, retrofitHelper.getConfig());
        }
        checkRetrofitInterface(retrofitInterface);
        Retrofit retrofit = getRetrofit(retrofitInterface);
        return retrofit.create(retrofitInterface);
    }

    /**
     * RetrofitInterface检查
     *
     * @param retrofitInterface
     */
    private void checkRetrofitInterface(Class<T> retrofitInterface) {
        // check class type
        Assert.isTrue(retrofitInterface.isInterface(), "@RetrofitClient只能作用在接口类型上！");
        Method[] methods = retrofitInterface.getMethods();
        // check void
        Config config = retrofitHelper.getConfig();

        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            Assert.isTrue(!void.class.isAssignableFrom(returnType),
                    "不支持使用void关键字做返回类型，请使用java.lang.Void! method=" + method);
            if (config.isDisableVoidReturnType()) {
                Assert.isTrue(!Void.class.isAssignableFrom(returnType),
                        "已配置禁用Void作为返回值，请指定其他返回类型！method=" + method);
            }
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


    /**
     * 获取okhttp3连接池
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return okhttp3连接池
     */
    @SneakyThrows
    private synchronized okhttp3.ConnectionPool getConnectionPool(Class<?> retrofitClientInterfaceClass) {
        RetrofitClient retrofitClient = retrofitClientInterfaceClass.getAnnotation(RetrofitClient.class);
        String poolName = retrofitClient.poolName();
        ConnectionPool connectionPool = poolRegistry.get(poolName);
        Assert.notNull(connectionPool, "当前poolName对应的连接池不存在！poolName = " + poolName);
        return connectionPool;
    }


    /**
     * 获取OkHttpClient实例，一个接口接口对应一个OkHttpClient
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return OkHttpClient实例
     */
    private synchronized OkHttpClient getOkHttpClient(Class<?> retrofitClientInterfaceClass) {
        okhttp3.ConnectionPool connectionPool = getConnectionPool(retrofitClientInterfaceClass);
        RetrofitClient retrofitClient = retrofitClientInterfaceClass.getAnnotation(RetrofitClient.class);
        // 构建一个OkHttpClient对象
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(retrofitClient.connectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(retrofitClient.readTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(retrofitClient.writeTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);
        // 添加接口上注解定义的拦截器
        List<Interceptor> interceptors = new ArrayList<>(findInterceptorByAnnotation(retrofitClientInterfaceClass));
        // 添加全局拦截器
        Collection<BaseGlobalInterceptor> globalInterceptors = getBeans(BaseGlobalInterceptor.class);
        if (globalInterceptors != null) {
            interceptors.addAll(globalInterceptors);
        }
        interceptors.forEach(okHttpClientBuilder::addInterceptor);
        // 添加日志拦截器
        Config config = retrofitHelper.getConfig();
        if (config.isEnableLog()) {
            LogInterceptor.Logger logger = LogInterceptor.innerLogger(retrofitClient.logLevel(), log);
            okHttpClientBuilder.addInterceptor(new LogInterceptor(logger, retrofitClient.logStrategy()));
        }
        return okHttpClientBuilder.build();
    }


    /**
     * 获取retrofitClient接口类上定义的拦截器集合
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return 拦截器实例集合
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private List<Interceptor> findInterceptorByAnnotation(Class<?> retrofitClientInterfaceClass) {
        Annotation[] classAnnotations = retrofitClientInterfaceClass.getAnnotations();
        List<Interceptor> interceptors = new ArrayList<>();
        // 找出被@InterceptMark标记的注解
        List<Annotation> interceptAnnotations = new ArrayList<>();
        for (Annotation classAnnotation : classAnnotations) {
            Class<? extends Annotation> annotationType = classAnnotation.annotationType();
            if (annotationType.isAnnotationPresent(InterceptMark.class)) {
                interceptAnnotations.add(classAnnotation);
            }
        }
        for (Annotation interceptAnnotation : interceptAnnotations) {
            // 获取注解属性数据
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(interceptAnnotation);
            Object handler = annotationAttributes.get("handler");
            Assert.notNull(handler, "@InterceptMark标记的注解必须配置: Class<? extends BasePathMatchInterceptor> handler()");
            Assert.notNull(annotationAttributes.get("include"), "@InterceptMark标记的注解必须配置: String[] include()");
            Assert.notNull(annotationAttributes.get("exclude"), "@InterceptMark标记的注解必须配置: String[] exclude()");
            Class<? extends BasePathMatchInterceptor> interceptorClass = (Class<? extends BasePathMatchInterceptor>) handler;
            BasePathMatchInterceptor interceptor = getInterceptorInstance(interceptorClass);
            // 动态设置属性值
            BeanExtendUtils.populate(interceptor, annotationAttributes);
            interceptor.setEnvironment(environment);
            interceptors.add(interceptor);
        }
        return interceptors;
    }

    /**
     * 获取路径拦截器实例，优先从spring容器中取。如果spring容器中不存在，则无参构造器实例化一个
     *
     * @param interceptorClass 路径拦截器类的子类，参见@{@link BasePathMatchInterceptor}
     * @return 路径拦截器实例
     */
    @SneakyThrows
    private BasePathMatchInterceptor getInterceptorInstance(Class<? extends BasePathMatchInterceptor> interceptorClass) {
        // spring bean
        if (isComponent(interceptorClass)) {
            // spring容器获取bean，必须是原型模式
            Scope scope = interceptorClass.getAnnotation(Scope.class);
            Assert.notNull(scope, interceptorClass.getName() + ": scope必须配置为prototype");
            Assert.isTrue(PROTOTYPE.equalsIgnoreCase(scope.value()), interceptorClass.getName() + ": scope必须配置为prototype");
            return applicationContext.getBean(interceptorClass);
        } else {
            // spring容器获取失败，反射创建
            return interceptorClass.newInstance();
        }
    }


    private boolean isComponent(Class<? extends BasePathMatchInterceptor> interceptorClass) {

        if (interceptorClass.isAnnotationPresent(Component.class)) {
            return true;
        }

        if (interceptorClass.isAnnotationPresent(Service.class)) {
            return true;
        }

        if (interceptorClass.isAnnotationPresent(Controller.class)) {
            return true;
        }

        return interceptorClass.isAnnotationPresent(Repository.class);
    }

    /**
     * 获取Retrofit实例，一个retrofitClient接口对应一个Retrofit实例
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return Retrofit实例
     */
    public synchronized Retrofit getRetrofit(Class<?> retrofitClientInterfaceClass) {
        RetrofitClient retrofitClient = retrofitClientInterfaceClass.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        baseUrl = formatBaseUrl(baseUrl);
        OkHttpClient client = getOkHttpClient(retrofitClientInterfaceClass);
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client);
        Config config = retrofitHelper.getConfig();
        // 配置的适配器工厂和转换器工厂
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        List<Converter.Factory> converterFactories = new ArrayList<>();
        // spring容器中的适配器工厂和转换器工厂
        Collection<CallAdapter.Factory> factories = getBeans(CallAdapter.Factory.class);
        if (!CollectionUtils.isEmpty(factories)) {
            callAdapterFactories.addAll(factories);
        }
        Collection<Converter.Factory> factoryCollection = getBeans(Converter.Factory.class);
        if (!CollectionUtils.isEmpty(factoryCollection)) {
            converterFactories.addAll(factoryCollection);
        }
        // 添加配置中选择启用的适配器工厂和转换器工厂
        if (config.isEnableBodyCallAdapter()) {
            bodyCallAdapterFactory = Optional.ofNullable(this.bodyCallAdapterFactory).orElse(new BodyCallAdapterFactory());
            callAdapterFactories.add(bodyCallAdapterFactory);
        }
        if (config.isEnableResponseCallAdapter()) {
            responseCallAdapterFactory = Optional.ofNullable(responseCallAdapterFactory).orElse(new ResponseCallAdapterFactory());
            callAdapterFactories.add(responseCallAdapterFactory);
        }
        if (config.isEnableFastJsonConverter()) {
            retrofit2ConverterFactory = Optional.ofNullable(retrofit2ConverterFactory).orElse(new Retrofit2ConverterFactory());
            converterFactories.add(retrofit2ConverterFactory);
        }
        // 添加CallAdapter.Factory
        callAdapterFactories.forEach(retrofitBuilder::addCallAdapterFactory);
        // 添加Converter.Factory
        converterFactories.forEach(retrofitBuilder::addConverterFactory);
        return retrofitBuilder.build();
    }

    private <U> Collection<U> getBeans(Class<U> clz) {
        try {
            Map<String, U> beanMap = applicationContext.getBeansOfType(clz);
            return beanMap.values();
        } catch (BeansException e) {
            // do nothing
        }
        return null;
    }


    private String formatBaseUrl(String baseUrl) {
        return this.environment.resolveRequiredPlaceholders(baseUrl);
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }


    @Override
    public void afterPropertiesSet() {
        MutablePropertySources propertySources = this.environment.getPropertySources();
        // 加载retrofitHelper的Properties属性到environment
        Properties properties = retrofitHelper.getProperties();
        if (properties != null) {
            PropertiesPropertySource propertySource = new PropertiesPropertySource("retrofitHelperProperties", properties);
            propertySources.addFirst(propertySource);
        }
        // 初始化连接池
        Map<String, PoolConfig> pool = retrofitHelper.getConfig().getPool();
        if (pool != null) {
            pool.forEach((poolName, poolConfig) -> {
                long keepAliveSecond = poolConfig.getKeepAliveSecond();
                int maxIdleConnections = poolConfig.getMaxIdleConnections();
                ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveSecond, TimeUnit.SECONDS);
                poolRegistry.put(poolName, connectionPool);
            });
        }
        // 默认连接池
        if (!poolRegistry.containsKey(DEFAULT_KEY)) {
            poolRegistry.put(DEFAULT_KEY, new ConnectionPool());
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
