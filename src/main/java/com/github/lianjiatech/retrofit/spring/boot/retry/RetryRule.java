package com.github.lianjiatech.retrofit.spring.boot.retry;

/**
 * 触发重试的规则
 * @author 陈添明
 */
public enum  RetryRule {

    /**
     * 响应状态码不是2xx
     */
    RESPONSE_STATUS_NOT_2XX,

    /**
     * 发生任意异常
     */
    OCCUR_EXCEPTION,

    /**
     * 发生IO异常
     */
    OCCUR_IO_EXCEPTION,

}
