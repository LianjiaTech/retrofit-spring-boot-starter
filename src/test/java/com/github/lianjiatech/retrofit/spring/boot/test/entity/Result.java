package com.github.lianjiatech.retrofit.spring.boot.test.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

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
}
