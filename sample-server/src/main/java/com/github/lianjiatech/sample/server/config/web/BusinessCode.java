package com.github.lianjiatech.sample.server.config.web;

/**
 * @author 陈添明
 */
public enum BusinessCode implements ReturnCode {

    /**
     * 成功
     */
    SUCCESS(0, "success");

    /**
     * 业务编号
     */
    private int code;

    /**
     * 业务值
     */
    private String message;

    BusinessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取返回编码
     *
     * @return code
     */
    @Override
    public int code() {
        return code;
    }

    /**
     * 获取返回描述信息
     *
     * @return message
     */
    @Override
    public String message() {
        return message;
    }
}
