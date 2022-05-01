package com.github.lianjiatech.retrofit.spring.boot.degrade.release4j;

import java.io.IOException;

import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.Response;

/**
 * @author 陈添明
 * @since 2022/5/1 8:02 下午
 */
public class Release4jRetrofitDegrade implements RetrofitDegrade {

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        return false;
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface) {
        CircuitBreakerRegistry registry = new CircuitBreakerRegistry.Builder()
                .build();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return null;
    }
}
