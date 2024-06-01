package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import retrofit2.Invocation;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class ErrorDecoderInterceptor implements Interceptor, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    @SneakyThrows
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(invocation.service(), RetrofitClient.class);
        ErrorDecoder errorDecoder =
                AppContextUtils.getBeanOrNew(applicationContext, retrofitClient.errorDecoder());
        boolean decoded = false;
        try {
            Response response = chain.proceed(request);
            if (errorDecoder == null) {
                return response;
            }
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
