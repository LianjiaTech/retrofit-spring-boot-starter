package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class AlarmInterceptor implements Interceptor {

    private final BaseAlarmFormatter alarmFormatter;

    public AlarmInterceptor(BaseAlarmFormatter alarmFormatter) {
        this.alarmFormatter = alarmFormatter;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = null;
        Response response = null;
        try {
            request = chain.request();
            response = chain.proceed(request);
        } catch (IOException e) {
            String alarmFormat = alarmFormatter.alarmFormat(request, response);
            String message = e.getMessage() + "\ndetail: " + alarmFormat;
            throw new IOException(message, e);
        } catch (Exception e) {
            String alarmFormat = alarmFormatter.alarmFormat(request, response);
            String message = e.getMessage() + "\ndetail: " + alarmFormat;
            throw new RuntimeException(message, e);
        }
        return response;
    }
}
