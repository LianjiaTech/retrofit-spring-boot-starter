package com.github.lianjiatech.retrofit.spring.boot.timeout;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.AnnotatedElementUtils;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Invocation;

/**
 * Call.Factory 包装器：为方法级 {@link Timeout} 注解预创建带超时覆盖的 OkHttpClient，
 * 运行时零 clone 开销。
 * <p>
 * 类级 {@code @Timeout} 已在 {@code createOkHttpClient()} 中处理（零运行时开销）；
 * 此包装器仅处理方法级 {@code @Timeout}——在初始化阶段为每个有方法级 {@code @Timeout} 的方法
 * clone delegate 并覆盖超时参数，存入 {@code methodCallFactoryMap}；
 * 运行时通过 Invocation tag 查找方法对应的预创建 client，直接委托。
 * <p>
 * 当 delegate 是 {@link OkHttpClient} 时，预创建 per-method client 保留 {@link com.github.lianjiatech.retrofit.spring.boot.core.CallFactoryConfigurer} 的修改；
 * 当 delegate 不是 {@link OkHttpClient}（如用户通过 {@code CallFactoryConfigurer} SPI
 * 返回的自定义 Call.Factory），方法级 {@code @Timeout} 不生效——用户应在自定义实现中自行处理。
 *
 * @author 陈添明
 */
public class TimeoutCallFactory implements Call.Factory {

    private final Call.Factory delegate;
    private final Map<Method, Call.Factory> methodCallFactoryMap;

    public TimeoutCallFactory(Call.Factory delegate, Class<?> retrofitInterface) {
        this.delegate = delegate;
        this.methodCallFactoryMap = buildMethodCallFactoryMap(delegate, retrofitInterface);
    }

    private static Map<Method, Call.Factory> buildMethodCallFactoryMap(
            Call.Factory delegate, Class<?> retrofitInterface) {
        if (!(delegate instanceof OkHttpClient)) {
            return new HashMap<>(0);
        }
        OkHttpClient baseClient = (OkHttpClient)delegate;
        Map<Method, Call.Factory> map = new HashMap<>(4);
        for (Method method : retrofitInterface.getMethods()) {
            Timeout timeout = AnnotatedElementUtils.findMergedAnnotation(method, Timeout.class);
            if (timeout == null || allInvalid(timeout)) {
                continue;
            }
            map.put(method, cloneWithTimeout(baseClient, timeout));
        }
        return map;
    }

    private static boolean allInvalid(Timeout timeout) {
        return timeout.connectTimeoutMs() == Constants.INVALID_VALUE
                && timeout.readTimeoutMs() == Constants.INVALID_VALUE
                && timeout.writeTimeoutMs() == Constants.INVALID_VALUE
                && timeout.callTimeoutMs() == Constants.INVALID_VALUE;
    }

    private static Call.Factory cloneWithTimeout(OkHttpClient baseClient, Timeout timeout) {
        OkHttpClient.Builder builder = baseClient.newBuilder();
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
        return builder.build();
    }

    @Override
    public Call newCall(Request request) {
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return delegate.newCall(request);
        }
        Call.Factory methodFactory = methodCallFactoryMap.get(invocation.method());
        return methodFactory != null
                ? methodFactory.newCall(request)
                : delegate.newCall(request);
    }
}
