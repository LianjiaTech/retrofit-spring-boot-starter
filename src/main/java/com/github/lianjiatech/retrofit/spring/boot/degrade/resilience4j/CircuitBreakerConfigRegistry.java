package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import com.github.lianjiatech.retrofit.spring.boot.core.Constants;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陈添明
 * @since 2022/5/24 9:37 下午
 */
public class CircuitBreakerConfigRegistry {

    private final Map<String, CircuitBreakerConfig> circuitBreakerConfigMap;

    private final List<CircuitBreakerConfigRegistrar> registrars;

    public CircuitBreakerConfigRegistry(List<CircuitBreakerConfigRegistrar> registrars) {
        this.circuitBreakerConfigMap = new HashMap<>(8);
        circuitBreakerConfigMap.put(Constants.DEFAULT_CIRCUIT_BREAKER_CONFIG, CircuitBreakerConfig.ofDefaults());
        this.registrars = registrars;
    }

    @PostConstruct
    public void init() {
        if (registrars == null) {
            return;
        }
        registrars.forEach(registrar -> registrar.register(this));
    }

    public void register(String name, CircuitBreakerConfig circuitBreakerConfig) {
        circuitBreakerConfigMap.put(name, circuitBreakerConfig);
    }

    public CircuitBreakerConfig get(String name) {
        CircuitBreakerConfig circuitBreakerConfig = circuitBreakerConfigMap.get(name);
        Assert.notNull(circuitBreakerConfig, "Specified CircuitBreakerConfig not found! name=" + name);
        return circuitBreakerConfig;
    }
}
