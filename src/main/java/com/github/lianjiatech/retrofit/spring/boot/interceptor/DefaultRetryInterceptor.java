package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class DefaultRetryInterceptor extends BaseRetryInterceptor {

    @Override
    protected Response retryIntercept(int maxRetries, int intervalMs, Chain chain) throws IOException {
        while (true) {
            try {
                Request request = chain.request();
                Response response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                }
                // 执行重试
                maxRetries--;
                if (maxRetries < 0) {
                    // 最后一次还没成功，返回最后一次response
                    return response;
                }
                response.close();
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                try {
                    maxRetries--;
                    if (maxRetries < 0) {
                        // 最后一次还没成功，抛出最后一次访问的异常
                        throw e;
                    }
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }
}
