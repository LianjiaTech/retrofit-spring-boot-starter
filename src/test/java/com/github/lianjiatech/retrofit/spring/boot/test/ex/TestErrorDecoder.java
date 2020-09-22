package com.github.lianjiatech.retrofit.spring.boot.test.ex;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.exception.ReadResponseBodyException;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 陈添明
 */
@Component
public class TestErrorDecoder implements ErrorDecoder {

    @Override
    public RuntimeException invalidRespDecode(Request request, Response response) {

        if (!response.isSuccessful()) {
            String responseBody = null;
            try {
                responseBody = RetrofitUtils.readResponseBody(response);
            } catch (ReadResponseBodyException e) {
                // do nothing
            }
            throw new AppException("非法响应，request=" + request + "  response=" + response + "body=" + responseBody);
        }

        return null;
    }

    @Override
    public RuntimeException ioExceptionDecode(Request request, IOException cause) {
        throw new AppIOException("应用网络异常！request=" + request);
    }
}
