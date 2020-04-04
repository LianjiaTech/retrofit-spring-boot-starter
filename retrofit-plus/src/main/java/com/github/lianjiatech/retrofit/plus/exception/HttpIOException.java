package com.github.lianjiatech.retrofit.plus.exception;

import com.github.lianjiatech.retrofit.plus.core.RequestHolder;
import com.github.lianjiatech.retrofit.plus.core.ResponseHolder;

/**
 * @author 陈添明
 */
public class HttpIOException extends RuntimeException {

    private static final long serialVersionUID = 5204193158428317438L;
    private final RequestHolder requestHolder;

    private final ResponseHolder responseHolder;

    public HttpIOException(RequestHolder requestHolder, ResponseHolder responseHolder, Exception e) {
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
