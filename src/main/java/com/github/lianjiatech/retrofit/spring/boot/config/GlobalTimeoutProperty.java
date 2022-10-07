package com.github.lianjiatech.retrofit.spring.boot.config;

import lombok.Data;

/**
 * 只有在@RetrofitClient.sourceOkHttpClient为NO_SOURCE_OK_HTTP_CLIENT时才有效
 * @author 陈添明
 * @since 2022/10/7 4:23 下午
 */
@Data
public class GlobalTimeoutProperty {

    /**
     * 全局连接超时时间
     */
    private int connectTimeoutMs = 10_000;

    /**
     * 全局读取超时时间
     */
    private int readTimeoutMs = 10_000;

    /**
     * 全局写入超时时间
     */
    private int writeTimeoutMs = 10_000;

    /**
     * 全局完整调用超时时间
     */
    private int callTimeoutMs = 0;
}
