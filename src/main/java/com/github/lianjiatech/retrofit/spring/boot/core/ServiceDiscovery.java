package com.github.lianjiatech.retrofit.spring.boot.core;

import java.net.URI;

/**
 * @author 陈添明
 */
public interface ServiceDiscovery {


    /**
     * Get the uri of an available instance according to serviceId. The implementer is responsible for service discovery and load balancing
     *
     * @param serviceId The name of the service.
     * @return Return the uri of an available instance in the service list
     */
    URI getUri(String serviceId);

}
