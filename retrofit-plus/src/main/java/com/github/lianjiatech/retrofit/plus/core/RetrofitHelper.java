package com.github.lianjiatech.retrofit.plus.core;

import com.github.lianjiatech.retrofit.plus.config.Config;
import lombok.Data;

import java.util.Properties;


/**
 * @author 陈添明
 */
@Data
public class RetrofitHelper {

    /**
     * retrofit配置
     */
    private Config config;


    /**
     * 其他配置属性 - 基于配置属性
     */
    private Properties properties;


    public RetrofitHelper addProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        properties.put(key, value);
        return this;
    }


    public RetrofitHelper(Config config) {
        this.config = config;
    }

    public RetrofitHelper() {
    }
}
