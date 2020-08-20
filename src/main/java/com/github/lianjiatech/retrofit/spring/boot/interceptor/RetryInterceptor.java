package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.annotation.Retry;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 请求重试拦截器
 *
 * @author 陈添明
 */
public class RetryInterceptor implements Interceptor {


    private static final int LIMIT_RETRIES = 10;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        assert invocation != null;
        Method method = invocation.method();
        // 获取重试配置
        Retry retry;
        if (method.isAnnotationPresent(Retry.class)) {
            retry = method.getAnnotation(Retry.class);
        } else {
            Class<?> declaringClass = method.getDeclaringClass();
            retry = declaringClass.getAnnotation(Retry.class);
        }
        if (retry == null) {
            // 不用重试
            return chain.proceed(request);
        }
        // 重试
        int maxRetries = retry.maxRetries();
        int intervalMs = retry.intervalMs();
        // 最多重试10次
        maxRetries = maxRetries > LIMIT_RETRIES ? LIMIT_RETRIES : maxRetries;
        return proceed(maxRetries, intervalMs, chain);
    }


    /**
     * 执行可重试请求
     * 这里访问级别设置为protected，可方便业务个性化扩展
     *
     * @param maxRetries 最大重试次数
     * @param intervalMs 重试时间间隔
     * @param chain      执行链
     * @return 请求响应
     */
    protected Response proceed(int maxRetries, int intervalMs, Chain chain) throws IOException {
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
