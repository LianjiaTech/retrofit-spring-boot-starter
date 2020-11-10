package com.github.lianjiatech.retrofit.spring.boot.core;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.util.Assert;

import java.net.URI;

/**
 * @author 陈添明
 */
public class SpringCloudServiceInstanceChooser implements ServiceInstanceChooser {

    private final LoadBalancerClient loadBalancerClient;

    public SpringCloudServiceInstanceChooser(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }

    /**
     * Chooses a ServiceInstance URI from the LoadBalancer for the specified service.
     *
     * @param serviceId The service ID to look up the LoadBalancer.
     * @return Return the uri of ServiceInstance
     */
    @Override
    public URI choose(String serviceId) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        Assert.notNull(serviceInstance, "can not found service instance! serviceId=" + serviceId);
        return serviceInstance.getUri();
    }
}
