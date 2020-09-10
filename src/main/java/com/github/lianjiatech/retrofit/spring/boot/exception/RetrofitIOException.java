package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 */
public class RetrofitIOException extends RetrofitException {


    public RetrofitIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetrofitIOException(String message) {
        super(message);
    }

}
