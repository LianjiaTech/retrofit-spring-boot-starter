package com.github.lianjiatech.retrofit.spring.boot.exception;

/**
 * @author 陈添明
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class RetrofitIOException extends RetrofitException { // NOPMD class naming: public API, rename breaks
                                                             // compatibility

    public RetrofitIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetrofitIOException(String message) {
        super(message);
    }

}
