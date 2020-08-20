package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * http异常信息格式化
 *
 * @author 陈添明
 */
public class HttpExceptionMessageFormatterInterceptor implements Interceptor {

    private final BaseHttpExceptionMessageFormatter httpExceptionMessageFormatter;

    public HttpExceptionMessageFormatterInterceptor(BaseHttpExceptionMessageFormatter httpExceptionMessageFormatter) {
        this.httpExceptionMessageFormatter = httpExceptionMessageFormatter;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = null;
        Response response = null;
        try {
            request = chain.request();
            response = chain.proceed(request);
        } catch (IOException e) {
            String alarmFormat = httpExceptionMessageFormatter.alarmFormat(request, response);
            String message = e.getMessage() + "\n" + alarmFormat;
            throw new IOException(message, e);
        } catch (Exception e) {
            String alarmFormat = httpExceptionMessageFormatter.alarmFormat(request, response);
            String message = e.getMessage() + "\n" + alarmFormat;
            throw new RuntimeException(message, e);
        }
        return response;
    }
}
