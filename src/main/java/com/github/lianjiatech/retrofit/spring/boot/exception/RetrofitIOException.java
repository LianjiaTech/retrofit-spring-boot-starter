package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 */
public class RetrofitIOException extends RetrofitException {


    protected RetrofitIOException(String message, Throwable cause) {
        super(message, cause);
    }

    protected RetrofitIOException(String message) {
        super(message);
    }

}
