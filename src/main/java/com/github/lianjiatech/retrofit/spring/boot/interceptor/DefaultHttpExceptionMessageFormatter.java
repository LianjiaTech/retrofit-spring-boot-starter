package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
public class DefaultHttpExceptionMessageFormatter extends BaseHttpExceptionMessageFormatter {
    /**
     * 格式化okHttp的request和response数据
     *
     * @param request  request
     * @param response response
     * @return 格式化okHttp的request和response数据
     */
    @Override
    public String alarmFormat(Request request, Response response) {

        StringBuilder builder = new StringBuilder("HTTP execute fail！");

        if (request != null) {
            builder.append(request.toString());
        }

        if (response != null) {
            builder.append("; ").append(response.toString());
        }

        return builder.toString();
    }
}
