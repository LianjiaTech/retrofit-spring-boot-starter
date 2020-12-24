package com.github.lianjiatech.retrofit.spring.boot.config;

import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DefaultResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeType;

/**
 * @author 陈添明
 */
public class DegradeProperty {


    /**
     * 启用熔断降级
     * enable degrade
     */
    private boolean enable = false;

    /**
     * 熔断降级类型，暂时只支持SENTINEL
     * degrade type, Only SENTINEL is currently supported
     */
    private DegradeType degradeType = DegradeType.SENTINEL;

    /**
     * 资源名称解析器
     * resource name parser
     */
    private Class<? extends BaseResourceNameParser> resourceNameParser = DefaultResourceNameParser.class;


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public DegradeType getDegradeType() {
        return degradeType;
    }

    public void setDegradeType(DegradeType degradeType) {
        this.degradeType = degradeType;
    }

    public Class<? extends BaseResourceNameParser> getResourceNameParser() {
        return resourceNameParser;
    }

    public void setResourceNameParser(Class<? extends BaseResourceNameParser> resourceNameParser) {
        this.resourceNameParser = resourceNameParser;
    }
}
