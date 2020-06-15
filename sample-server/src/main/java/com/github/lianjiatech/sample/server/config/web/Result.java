package com.github.lianjiatech.sample.server.config.web;

import java.io.Serializable;

/**
 * @author 陈添明
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -504027247149928390L;

    private int code;
    private String msg;
    private String exceptionMsg;
    private T data;

    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public Result<T> setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public static <T> Result<T> ok(T body) {
        return new Result<T>()
                .setData(body)
                .setCode(BusinessCode.SUCCESS.code())
                .setMsg(BusinessCode.SUCCESS.message());
    }

    public static Result ok() {
        return new Result<>()
                .setCode(BusinessCode.SUCCESS.code())
                .setMsg(BusinessCode.SUCCESS.message());
    }

    public static Result fail(ReturnCode returnCode) {
        return new Result<>()
                .setCode(returnCode.code())
                .setMsg(returnCode.message());
    }

    public static Result fail(ReturnCode returnCode, String exceptionMsg) {
        return new Result<>()
                .setCode(returnCode.code())
                .setMsg(returnCode.message())
                .setExceptionMsg(exceptionMsg);
    }
}
