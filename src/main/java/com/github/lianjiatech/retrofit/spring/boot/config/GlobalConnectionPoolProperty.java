package com.github.lianjiatech.retrofit.spring.boot.config;

import lombok.Data;

/**
 * @author 陈添明
 * @since 2026/2/12 14:21 
 */
@Data
public class GlobalConnectionPoolProperty {

    /**
     * 最大空闲连接数
     */
    private int maxIdleConnections = 5;

    /**
     * 保活时间，单位毫秒
     */
    private long keepAliveDurationMs = 300_000;
}
