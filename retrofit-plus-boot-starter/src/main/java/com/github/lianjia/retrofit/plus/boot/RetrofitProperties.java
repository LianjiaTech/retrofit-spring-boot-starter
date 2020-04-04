package com.github.lianjia.retrofit.plus.boot;


import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.github.lianjia.retrofit.plus.core.BodyCallAdapterFactory;
import com.github.lianjia.retrofit.plus.core.ResponseCallAdapterFactory;
import com.github.lianjia.retrofit.plus.config.Config;
import com.github.lianjia.retrofit.plus.config.PoolConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 这个类存在的主要目的是方便 IDE 自动提示开头的配置
 *
 * @author 陈添明
 */
@Data
@ConfigurationProperties(prefix = Config.PREFIX)
public class RetrofitProperties {

    /**
     * 连接池配置
     */
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
     * 启用 #{@link Retrofit2ConverterFactory} 数据转换器
     */
    private boolean enableFastJsonConverter = true;

    /**
     * 启用日志打印
     */
    private boolean enableLog = true;

    /**
     * 禁用Void返回类型
     */
    private boolean disableVoidReturnType = false;
}
