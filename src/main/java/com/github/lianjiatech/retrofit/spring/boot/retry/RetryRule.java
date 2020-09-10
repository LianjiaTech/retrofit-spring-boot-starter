package com.github.lianjiatech.retrofit.spring.boot.retry;

/**
 * 触发重试的规则
 * Rules that trigger retry
 * @author 陈添明
 */
public enum  RetryRule {

    /**
     * 响应状态码不是2xx
     * The response status code is not 2xx
     */
    RESPONSE_STATUS_NOT_2XX,

    /**
     * 发生任意异常
     * Any exception occurred
     */
    OCCUR_EXCEPTION,

    /**
     * 发生IO异常
     * IO exception occurred
     */
    OCCUR_IO_EXCEPTION,

}
