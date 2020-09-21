package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 */
public class ServiceInstanceChooseException extends RuntimeException {

    public ServiceInstanceChooseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceInstanceChooseException(String message) {
        super(message);
    }

    public ServiceInstanceChooseException(Throwable cause) {
        super(cause);
    }
}
