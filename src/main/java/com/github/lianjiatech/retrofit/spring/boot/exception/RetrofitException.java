package com.github.lianjiatech.retrofit.spring.boot.exception;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;

import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
public class RetrofitException extends RuntimeException {

    public RetrofitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetrofitException(String message) {
        super(message);
    }

    public static RetrofitException errorStatus(Request request, Response response) {
        String msg = "invalid Response! request=" + request + ", response=" + response;
        try {
            String responseBody = RetrofitUtils.readResponseBody(response);
            if (StringUtils.hasText(responseBody)) {
                msg += ", body=" + responseBody;
            }
        } catch (ReadResponseBodyException e) {
            throw new RetrofitException(
                    "read ResponseBody error! request=" + request + ", response=" + response, e);
        } finally {
            response.close();
        }
        return new RetrofitException(msg);
    }

    public static RetrofitException errorExecuting(Request request, IOException cause) {
        return new RetrofitIOException(cause.getMessage() + ", request=" + request, cause);
    }

    public static RetrofitException errorUnknown(Request request, Exception cause) {
        if (cause instanceof RetrofitException) {
            return (RetrofitException)cause;
        }
        return new RetrofitException(cause.getMessage() + ", request=" + request, cause);
    }
}
