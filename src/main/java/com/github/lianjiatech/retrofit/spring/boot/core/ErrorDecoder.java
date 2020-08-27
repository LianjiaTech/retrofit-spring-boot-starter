package com.github.lianjiatech.retrofit.spring.boot.core;

import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
public interface ErrorDecoder {


    /**
     * 当无效响应的时候，将http信息解码到异常中，无效响应由业务自行判断。
     * When the response is invalid, decode the http information into the exception, invalid response is determined by business.
     *
     * @param request  request
     * @param response response
     * @return If it returns null, the processing is ignored and the processing continues with the original response.
     */
    Exception decode(Request request, Response response);

}
