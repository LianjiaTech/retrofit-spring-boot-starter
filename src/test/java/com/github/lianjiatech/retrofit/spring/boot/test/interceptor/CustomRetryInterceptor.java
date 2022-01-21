package com.github.lianjiatech.retrofit.spring.boot.test.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.retry.BaseRetryInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;
import com.github.lianjiatech.retrofit.spring.boot.test.service.TestService;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陈添明
 * @summary
 * @since 2022/1/21 4:52 下午
 */
@Component
public class CustomRetryInterceptor extends BaseRetryInterceptor {

    @Autowired
    private TestService testService;

    @Override
    protected Response retryIntercept(int maxRetries, int intervalMs, RetryRule[] retryRules, Chain chain) {
        System.out.println("=============执行重试=============");
        testService.test();
        try {
            return chain.proceed(chain.request());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
