package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.annotation.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.config.PoolConfig;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.*;
import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.slf4j.event.Level;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈添明
 */
public class RetrofitFactoryBean<T> implements FactoryBean<T>, EnvironmentAware, InitializingBean, ApplicationContextAware {

    private Class<T> retrofitInterface;

    private Environment environment;

    private Map<String, ConnectionPool> poolRegistry = new ConcurrentHashMap<>(4);

    private RetrofitProperties retrofitProperties;

    private ApplicationContext applicationContext;

    public Class<T> getRetrofitInterface() {
        return retrofitInterface;
    }

    public void setRetrofitInterface(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    public RetrofitFactoryBean() {

    }

    public RetrofitFactoryBean(Class<T> retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {

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
        for (Method method : methods) {
            Class<?> returnType = method.getReturnType();
            Assert.isTrue(!void.class.isAssignableFrom(returnType),
                    "不支持使用void关键字做返回类型，请使用java.lang.Void! method=" + method);
            if (retrofitProperties.isDisableVoidReturnType()) {
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
    private synchronized OkHttpClient getOkHttpClient(Class<?> retrofitClientInterfaceClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
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

        // 日志打印拦截器
        if (retrofitProperties.isEnableLog()) {
            Class<? extends BaseLoggingInterceptor> loggingInterceptorClass = retrofitProperties.getLoggingInterceptorClass();
            Constructor<? extends BaseLoggingInterceptor> constructor = loggingInterceptorClass.getConstructor(Level.class, BaseLoggingInterceptor.LogStrategy.class);
            BaseLoggingInterceptor loggingInterceptor = constructor.newInstance(retrofitClient.logLevel(), retrofitClient.logStrategy());
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }

        // 报警信息拦截器
        Class<? extends BaseHttpExceptionMessageFormatter> httpExceptionMessageFormatterClass = retrofitProperties.getHttpExceptionMessageFormatterClass();
        BaseHttpExceptionMessageFormatter alarmFormatter = httpExceptionMessageFormatterClass.newInstance();
        HttpExceptionMessageFormatterInterceptor httpExceptionMessageFormatterInterceptor = new HttpExceptionMessageFormatterInterceptor(alarmFormatter);
        okHttpClientBuilder.addInterceptor(httpExceptionMessageFormatterInterceptor);

        return okHttpClientBuilder.build();
    }


    /**
     * 获取retrofitClient接口类上定义的拦截器集合
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return 拦截器实例集合
     */
    @SuppressWarnings("unchecked")
    private List<Interceptor> findInterceptorByAnnotation(Class<?> retrofitClientInterfaceClass) throws InstantiationException, IllegalAccessException {
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
            Map<String, Object> annotationResolveAttributes = new HashMap<>(8);
            // 占位符属性替换
            annotationAttributes.forEach((key, value) -> {
                if (value instanceof String) {
                    String newValue = environment.resolvePlaceholders((String) value);
                    annotationResolveAttributes.put(key, newValue);
                } else {
                    annotationResolveAttributes.put(key, value);
                }
            });
            // 动态设置属性值
            BeanExtendUtils.populate(interceptor, annotationResolveAttributes);
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
    private BasePathMatchInterceptor getInterceptorInstance(Class<? extends BasePathMatchInterceptor> interceptorClass) throws IllegalAccessException, InstantiationException {
        // spring bean
        try {
            return applicationContext.getBean(interceptorClass);
        } catch (BeansException e) {
            // spring容器获取失败，反射创建
            return interceptorClass.newInstance();
        }
    }


    /**
     * 获取Retrofit实例，一个retrofitClient接口对应一个Retrofit实例
     *
     * @param retrofitClientInterfaceClass retrofitClient接口类
     * @return Retrofit实例
     */
    private synchronized Retrofit getRetrofit(Class<?> retrofitClientInterfaceClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        RetrofitClient retrofitClient = retrofitClientInterfaceClass.getAnnotation(RetrofitClient.class);
        String baseUrl = retrofitClient.baseUrl();
        // 解析baseUrl占位符
        baseUrl = environment.resolveRequiredPlaceholders(baseUrl);
        OkHttpClient client = getOkHttpClient(retrofitClientInterfaceClass);
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client);
        // spring容器中的适配器工厂和转换器工厂
        Collection<CallAdapter.Factory> callAdapterFactories = getBeans(CallAdapter.Factory.class);
        if (!CollectionUtils.isEmpty(callAdapterFactories)) {
            // 添加CallAdapter.Factory
            callAdapterFactories.forEach(retrofitBuilder::addCallAdapterFactory);
        }
        Collection<Converter.Factory> converterFactories = getBeans(Converter.Factory.class);
        if (!CollectionUtils.isEmpty(converterFactories)) {
            // 添加Converter.Factory
            converterFactories.forEach(retrofitBuilder::addConverterFactory);
        }
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



    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void afterPropertiesSet() {
        // 初始化连接池
        Map<String, PoolConfig> pool = retrofitProperties.getPool();
        if (pool != null) {
            pool.forEach((poolName, poolConfig) -> {
                long keepAliveSecond = poolConfig.getKeepAliveSecond();
                int maxIdleConnections = poolConfig.getMaxIdleConnections();
                ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveSecond, TimeUnit.SECONDS);
                poolRegistry.put(poolName, connectionPool);
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        retrofitProperties = applicationContext.getBean(RetrofitProperties.class);
    }
}
