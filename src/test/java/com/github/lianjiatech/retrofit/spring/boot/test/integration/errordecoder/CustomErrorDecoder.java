package com.github.lianjiatech.retrofit.spring.boot.test.integration.errordecoder;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 自定义 ErrorDecoder：
 * - invalid 响应：4xx 抛 BizException(400/code)，5xx 走默认；
 * - IO 异常：包装成 BizException(0/io)；
 * - 其它异常：包装成 BizException(-1/unknown)。
 *
 * @author 陈添明
 */
@Component
public class CustomErrorDecoder implements ErrorDecoder {

    public static class BizException extends RuntimeException {
        public final int code;
        public BizException(int code, String msg) {
            super(msg);
            this.code = code;
        }
    }

    @Override
    public RuntimeException invalidRespDecode(Request request, Response response) {
        if (response.code() >= 400 && response.code() < 500) {
            response.close();
            return new BizException(response.code(), "client-error");
        }
        // 5xx 复用默认行为
        if (!response.isSuccessful()) {
            throw RetrofitException.errorStatus(request, response);
        }
        return null;
    }

    @Override
    public RuntimeException ioExceptionDecode(Request request, IOException cause) {
        return new BizException(0, "io:" + cause.getClass().getSimpleName());
    }

    @Override
    public RuntimeException exceptionDecode(Request request, Exception cause) {
        if (cause instanceof RetrofitException) {
            return (RetrofitException) cause;
        }
        return new BizException(-1, "unknown:" + cause.getClass().getSimpleName());
    }
}
