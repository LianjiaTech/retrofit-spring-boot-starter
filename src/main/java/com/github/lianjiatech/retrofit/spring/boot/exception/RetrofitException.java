package com.github.lianjiatech.retrofit.spring.boot.exception;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class RetrofitException extends RuntimeException {


    protected RetrofitException(String message, Throwable cause) {
        super(message, cause);
    }

    protected RetrofitException(String message) {
        super(message);
    }


    public static RetrofitException errorStatus(Request request, Response response) {
        String msg = String.format("invalid Response! request=%s, response=%s", request, response);
        ResponseBody body = response.body();
        if (body != null) {
            try {
                String bodyString = body.string();
                msg += ", body=" + bodyString;
            } catch (IOException e) {
                // do nothing
            }
        }
        return new RetrofitException(msg);
    }

    public static RetrofitException errorExecuting(Request request, IOException cause) {
        return new RetrofitException(cause.getMessage() + ", request=" + request, cause);
    }

    public static RetrofitException errorUnknown(Request request, Exception cause) {
        if (cause instanceof RetrofitException) {
            return (RetrofitException) cause;
        }
        return new RetrofitException(cause.getMessage() + ", request=" + request, cause);
    }
}
