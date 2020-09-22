package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 陈添明
 */
public class ErrorDecoderInterceptor implements Interceptor {

    private final ErrorDecoder errorDecoder;

    private static Map<ErrorDecoder, ErrorDecoderInterceptor> cache = new HashMap<>(4);

    private ErrorDecoderInterceptor(ErrorDecoder errorDecoder) {
        this.errorDecoder = errorDecoder;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
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
                throw (RuntimeException) e;
            }
            throw errorDecoder.exceptionDecode(request, e);
        }
    }


    public static ErrorDecoderInterceptor create(ErrorDecoder errorDecoder) {
        ErrorDecoderInterceptor interceptor = cache.get(errorDecoder);
        if (interceptor != null) {
            return interceptor;
        }
        interceptor = new ErrorDecoderInterceptor(errorDecoder);
        cache.put(errorDecoder, interceptor);
        return interceptor;
    }
}
