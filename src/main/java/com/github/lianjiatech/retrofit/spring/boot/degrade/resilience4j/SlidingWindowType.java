package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

/**
 * 全局Resilience4j降级配置
 * @author 陈添明
 * @since 2022/5/8 10:46 上午
 */
public enum SlidingWindowType {
    TIME_BASED, COUNT_BASED
}