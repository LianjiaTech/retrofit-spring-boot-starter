package com.github.lianjiatech.retrofit.spring.boot.core;

import java.net.URI;

import com.github.lianjiatech.retrofit.spring.boot.exception.ServiceInstanceChooseException;

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

    class NoValidServiceInstanceChooser implements ServiceInstanceChooser {

        @Override
        public URI choose(String serviceId) {
            throw new ServiceInstanceChooseException(
                    "No valid service instance selector, Please configure it! serviceId=" + serviceId);
        }
    }

}
