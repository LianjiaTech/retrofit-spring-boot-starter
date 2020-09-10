package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 */
public class ReadResponseBodyException extends Exception {

    public ReadResponseBodyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadResponseBodyException(String message) {
        super(message);
    }

    public ReadResponseBodyException(Throwable cause) {
        super(cause);
    }
}
