package com.github.lianjiatech.retrofit.spring.boot.degrade;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.GlobalResilience4jDegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.GlobalSentinelDegradeProperty;

import lombok.Data;

/**
 * @author 陈添明
 */
@Data
public class DegradeProperty {

    /**
     * 熔断降级类型。默认none，表示不启用熔断降级
     */
    private String degradeType = RetrofitDegrade.NONE;

    /**
     * 全局Sentinel降级配置
     */
    @NestedConfigurationProperty
    private GlobalSentinelDegradeProperty globalSentinelDegrade = new GlobalSentinelDegradeProperty();

    /**
     * 全局Resilience4j降级配置
     */
    @NestedConfigurationProperty
    private GlobalResilience4jDegradeProperty globalResilience4jDegrade = new GlobalResilience4jDegradeProperty();
}
