package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeType;

import lombok.Data;

/**
 * @author 陈添明
 */
@Data
public class DegradeProperty {

    /**
     * 启用熔断降级
     * enable degrade
     */
    private boolean enable = false;

    /**
     * 熔断降级类型
     * degrade type
     */
    private DegradeType degradeType = DegradeType.SENTINEL;
}
