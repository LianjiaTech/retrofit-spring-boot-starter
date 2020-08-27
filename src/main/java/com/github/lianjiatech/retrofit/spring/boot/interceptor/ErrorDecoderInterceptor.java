package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class ErrorDecoderInterceptor implements Interceptor {

    private final ErrorDecoder errorDecoder;

    public ErrorDecoderInterceptor(ErrorDecoder errorDecoder) {
        this.errorDecoder = errorDecoder;
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        try {
            Response response = chain.proceed(request);
            if (errorDecoder == null) {
                return response;
            }
            Exception exception = errorDecoder.decode(request, response);
            if (exception == null) {
                return response;
            }
            throw exception;
        } catch (IOException e) {
            throw RetrofitException.errorExecuting(request, e);
        } catch (Exception e) {
            throw RetrofitException.errorUnknown(request, e);
        }
    }
}
