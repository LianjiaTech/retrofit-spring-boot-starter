package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 陈添明
 * @since 2022/3/30 11:01 下午
 */
public class GlobalAndNetworkInterceptorFinder {

    @Autowired(required = false)
    private List<GlobalInterceptor> globalInterceptors;

    @Autowired(required = false)
    private List<NetworkInterceptor> networkInterceptors;

    public List<GlobalInterceptor> getGlobalInterceptors() {
        if (globalInterceptors == null) {
            return Collections.emptyList();
        }
        return globalInterceptors;
    }

    public List<NetworkInterceptor> getNetworkInterceptors() {
        if (networkInterceptors == null) {
            return Collections.emptyList();
        }
        return networkInterceptors;
    }
}
