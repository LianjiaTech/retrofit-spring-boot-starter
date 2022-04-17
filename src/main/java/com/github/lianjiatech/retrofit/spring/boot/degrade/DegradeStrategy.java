package com.github.lianjiatech.retrofit.spring.boot.degrade;

/**
 * @author 陈添明
 */
public enum DegradeStrategy {

    /**
     * average RT
     */
    AVERAGE_RT,

    /**
     * exception ratio
     */
    EXCEPTION_RATIO,
}
