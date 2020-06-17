package com.github.lianjiatech.retrofit.spring.boot.exception;


import com.github.lianjiatech.retrofit.spring.boot.core.RequestHolder;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseHolder;

/**
 * @author 陈添明
 */
public class FailResponseException extends RuntimeException {

    private static final long serialVersionUID = -7884534078962910000L;
    private final RequestHolder requestHolder;

    private final ResponseHolder responseHolder;

    public FailResponseException(RequestHolder requestHolder, ResponseHolder responseHolder) {
        this.requestHolder = requestHolder;
        this.responseHolder = responseHolder;
    }

    @Override
    public String getMessage() {
        StringBuilder stringBuilder = new StringBuilder("fail Response! ");
        if (requestHolder != null) {
            stringBuilder.append(requestHolder);
        }
        if (responseHolder != null) {
            stringBuilder.append(responseHolder);
        }

        return stringBuilder.toString();
    }
}
