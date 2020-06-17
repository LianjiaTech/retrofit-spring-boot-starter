package com.github.lianjiatech.retrofit.spring.boot.config;

/**
 * 连接池参数配置
 *
 * @author 陈添明
 */
public class PoolConfig {
    /**
     * 最大空闲连接
     */
    private int maxIdleConnections;

    /**
     * 保活时间，单位为秒
     */
    private long keepAliveSecond;

    public PoolConfig(int maxIdleConnections, long keepAliveSecond) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveSecond = keepAliveSecond;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public long getKeepAliveSecond() {
        return keepAliveSecond;
    }
}