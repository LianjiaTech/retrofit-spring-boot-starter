package com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory;

import java.util.concurrent.TimeUnit;

import com.github.lianjiatech.retrofit.spring.boot.core.CallFactoryConfigurer;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Invocation;

/**
 * 测试用 CallFactoryConfigurer：对 DefaultCallTimeoutService 接口动态覆盖 callTimeout 为极短值（500ms），
 * 对其它接口返回 baseClient 不做覆盖。
 * <p>
 * 同时验证 Invocation tag 可在 configure 返回的 Call.Factory 中使用。
 */
public class TestCallFactoryConfigurer implements CallFactoryConfigurer {

    @Override
    public Call.Factory configure(Class<?> retrofitInterface, OkHttpClient baseClient) {
        if (retrofitInterface == CallFactoryConfigurerServices.DefaultCallTimeoutService.class) {
            return new DynamicCallTimeoutCallFactory(baseClient);
        }
        return baseClient;
    }

    /**
     * 包装 OkHttpClient，根据 Invocation tag 上的方法注解动态覆盖 callTimeout。
     */
    static class DynamicCallTimeoutCallFactory implements Call.Factory {

        private final OkHttpClient baseClient;

        DynamicCallTimeoutCallFactory(OkHttpClient baseClient) {
            this.baseClient = baseClient;
        }

        @Override
        public Call newCall(Request request) {
            Invocation invocation = request.tag(Invocation.class);
            if (invocation != null) {
                DynamicCallTimeout ann = invocation.method().getAnnotation(DynamicCallTimeout.class);
                if (ann != null) {
                    return baseClient.newBuilder()
                            .callTimeout(ann.ms(), TimeUnit.MILLISECONDS)
                            .build()
                            .newCall(request);
                }
            }
            return baseClient.newCall(request);
        }
    }
}
