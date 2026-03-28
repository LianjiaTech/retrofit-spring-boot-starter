package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
public class ErrorDecoderInterceptor implements Interceptor, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    /** 缓存每个 Retrofit 接口对应的 ErrorDecoder 实例，避免每次请求都进行 Spring 容器查找和反射。 */
    private final ConcurrentHashMap<Class<?>, ErrorDecoder> errorDecoderCache = new ConcurrentHashMap<>(32);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        ErrorDecoder errorDecoder = errorDecoderCache.computeIfAbsent(invocation.service(), serviceClass -> {
            RetrofitClient retrofitClient =
                    AnnotatedElementUtils.findMergedAnnotation(serviceClass, RetrofitClient.class);
            return AppContextUtils.getBeanOrNew(applicationContext, retrofitClient.errorDecoder());
        });
        boolean decoded = false;
        try {
            Response response = chain.proceed(request);
            decoded = true;
            Exception exception = errorDecoder.invalidRespDecode(request, response);
            if (exception == null) {
                return response;
            }
            throw exception;
        } catch (IOException e) {
            if (decoded) {
                throw e;
            }
            throw errorDecoder.ioExceptionDecode(request, e);
        } catch (Exception e) {
            if (decoded && e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            throw errorDecoder.exceptionDecode(request, e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
