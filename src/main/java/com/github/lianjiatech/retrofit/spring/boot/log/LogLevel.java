package com.github.lianjiatech.retrofit.spring.boot.log;

/**
 * @author 陈添明
 */
public enum LogLevel {

    /** 仅记录错误信息 */
    ERROR,

    /** 记录警告及以上级别 */
    WARN,

    /** 记录一般信息 */
    INFO,

    /** 记录调试详细信息 */
    DEBUG,

    /** 记录全部请求与响应细节 */
    TRACE,
}
