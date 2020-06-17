package com.github.lianjiatech.retrofit.spring.boot.exception;


import com.github.lianjiatech.retrofit.spring.boot.core.RequestHolder;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseHolder;

/**
 * @author 陈添明
 */
public class HttpExecuteException extends RuntimeException {

    private static final long serialVersionUID = -3167932922036752098L;
    private final RequestHolder requestHolder;

    private final ResponseHolder responseHolder;

    public HttpExecuteException(RequestHolder requestHolder, ResponseHolder responseHolder, Exception e) {
        super(e);
        this.requestHolder = requestHolder;
        this.responseHolder = responseHolder;
    }

    @Override
    public String getMessage() {
        StringBuilder stringBuilder = new StringBuilder(super.getMessage())
                .append(": ");
        if (requestHolder != null) {
            stringBuilder.append(requestHolder);
        }
        if (responseHolder != null) {
            stringBuilder.append(responseHolder);
        }

        return stringBuilder.toString();
    }
}
