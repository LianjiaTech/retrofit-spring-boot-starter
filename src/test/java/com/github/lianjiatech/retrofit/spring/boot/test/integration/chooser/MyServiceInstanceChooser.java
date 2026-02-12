package com.github.lianjiatech.retrofit.spring.boot.test.integration.chooser;

import com.github.lianjiatech.retrofit.spring.boot.core.ServiceInstanceChooser;
import okhttp3.Request;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author 陈添明
 * @since 2023/12/17 6:51 下午
 */
@Component
public class MyServiceInstanceChooser implements ServiceInstanceChooser {

    @Override
    public URI choose(String serviceId, Request request) {
        if (serviceId.equals("user")) {
            return URI.create("http://localhost:8080");
        }
        throw new IllegalStateException("illegal serviceId: " + serviceId);
    }
}
