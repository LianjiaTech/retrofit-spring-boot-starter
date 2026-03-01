package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;

/**
 * 全局Sentinel降级配置
 * @author 陈添明
 * @since 2022/5/8 10:45 上午
 */
@Data
public class GlobalSentinelDegradeProperty {

    /**
     * 是否开启
     */
    private boolean enable = false;

    @NestedConfigurationProperty
    private SentinelDegradeRuleProperty[] rules = {};
}
