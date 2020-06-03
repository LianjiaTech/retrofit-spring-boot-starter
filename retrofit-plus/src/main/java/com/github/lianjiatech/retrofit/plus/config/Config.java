package com.github.lianjiatech.retrofit.plus.config;

import com.github.lianjiatech.retrofit.plus.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.plus.core.ResponseCallAdapterFactory;
import lombok.Data;

import java.util.Map;

/**
 * @author 陈添明
 */
@Data
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
}
