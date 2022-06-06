package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 * @since 2022/6/6 9:59 上午
 */
public class RetryFailedException extends RuntimeException {

    public RetryFailedException(String message) {
        super(message);
    }

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
