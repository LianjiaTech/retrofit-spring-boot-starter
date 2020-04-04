package com.github.lianjiatech.sample.server.config.web;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 陈添明
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -504027247149928390L;

    private int code;
    private String msg;
    private String exceptionMsg;
    private T data;

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
