package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author 陈添明
 */
public class DefaultErrorDecoder extends InvalidRespDecoder {


    /**
     * 当无效响应的时候，将HTTP信息解码到异常中，无效响应由业务自行判断。
     * When the response is invalid, decode the HTTP information into the exception, invalid response is determined by business.
     *
     * @param request  request
     * @param response response
     * @return If it returns null, the processing is ignored and the processing continues with the original response.
     */
    @Override
    public RuntimeException invalidRespDecode(Request request, Response response) {
        if (!response.isSuccessful()) {
            throw RetrofitException.errorStatus(request, response);
        }
        return null;
    }
}
