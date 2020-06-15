package com.github.lianjiatech.retrofit.plus.config;

import com.github.lianjiatech.retrofit.plus.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.plus.core.ResponseCallAdapterFactory;

import java.util.Map;

/**
 * @author 陈添明
 */
public class Config {

    public static final String PREFIX = "retrofit-plus";

    private Map<String, PoolConfig> pool;

    /**
     * 启用 #{@link BodyCallAdapterFactory} 调用适配器
     */
    private boolean enableBodyCallAdapter = true;

    /**
     * 启用 #{@link ResponseCallAdapterFactory} 调用适配器
     */
    private boolean enableResponseCallAdapter = true;

    /**
     * 启用日志打印
     */
    private boolean enableLog = true;
    /**
     * 禁用java.lang.Void返回类型
     */
    private boolean disableVoidReturnType = false;

    public Map<String, PoolConfig> getPool() {
        return pool;
    }

    public void setPool(Map<String, PoolConfig> pool) {
        this.pool = pool;
    }

    public boolean isEnableBodyCallAdapter() {
        return enableBodyCallAdapter;
    }

    public void setEnableBodyCallAdapter(boolean enableBodyCallAdapter) {
        this.enableBodyCallAdapter = enableBodyCallAdapter;
    }

    public boolean isEnableResponseCallAdapter() {
        return enableResponseCallAdapter;
    }

    public void setEnableResponseCallAdapter(boolean enableResponseCallAdapter) {
        this.enableResponseCallAdapter = enableResponseCallAdapter;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    public boolean isDisableVoidReturnType() {
        return disableVoidReturnType;
    }

    public void setDisableVoidReturnType(boolean disableVoidReturnType) {
        this.disableVoidReturnType = disableVoidReturnType;
    }
}
