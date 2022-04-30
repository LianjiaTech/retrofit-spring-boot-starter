package com.github.lianjiatech.retrofit.spring.boot.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈添明
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolConfig {

    private int maxIdleConnections;

    private long keepAliveSecond;
}