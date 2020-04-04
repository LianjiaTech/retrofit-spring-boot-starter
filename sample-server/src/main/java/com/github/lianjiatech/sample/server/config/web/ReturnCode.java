package com.github.lianjiatech.sample.server.config.web;

/**
 * 如果需要自定义错误编码，需要继承ReturnCode接口
 *
 * @author 陈添明
 */
public interface ReturnCode {

    /**
     * 获取返回编码
     *
     * @return code
     */
    int code();

    /**
     * 获取返回描述信息
     *
     * @return message
     */
    String message();

}
