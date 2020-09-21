package com.github.lianjiatech.retrofit.spring.boot.core;

import java.net.URI;

/**
 * @author 陈添明
 */
@FunctionalInterface
public interface ServiceInstanceChooser {


    /**
     * Chooses a ServiceInstance URI from the LoadBalancer for the specified service.
     *
     * @param serviceId The service ID to look up the LoadBalancer.
     * @return Return the uri of ServiceInstance
     */
    URI choose(String serviceId);

}
