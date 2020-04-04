package com.github.lianjiatech.retrofit.plus.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接池参数配置
 *
 * @author 陈添明
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolConfig {
    /**
     * 最大空闲连接
     */
    private int maxIdleConnections = 5;

    /**
     * 保活时间，单位为秒
     */
    private long keepAliveSecond = 300;
}