package com.github.lianjia.retrofit.spring.boot.test.entity;

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
}
