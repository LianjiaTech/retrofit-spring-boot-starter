package com.github.lianjiatech.retrofit.spring.boot.exception;

import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.StringUtils;

import java.io.IOException;

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
        String msg = String.format("invalid Response! request=%s, response=%s", request, response);
        try {
            String responseBody = RetrofitUtils.readResponseBody(response);
            if (StringUtils.hasText(responseBody)) {
                msg += ", body=" + responseBody;
            }
        } catch (ReadResponseBodyException e) {
            throw new RetrofitException(String.format("read ResponseBody error! request=%s, response=%s", request, response), e);
        }
        return new RetrofitException(msg);
    }

    public static RetrofitException errorExecuting(Request request, IOException cause) {
        return new RetrofitIOException(cause.getMessage() + ", request=" + request, cause);
    }

    public static RetrofitException errorUnknown(Request request, Exception cause) {
        if (cause instanceof RetrofitException) {
            return (RetrofitException) cause;
        }
        return new RetrofitException(cause.getMessage() + ", request=" + request, cause);
    }
}
