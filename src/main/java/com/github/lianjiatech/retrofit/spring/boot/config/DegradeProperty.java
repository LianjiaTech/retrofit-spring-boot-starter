package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitDegrade;

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
}
