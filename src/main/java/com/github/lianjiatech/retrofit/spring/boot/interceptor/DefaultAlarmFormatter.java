package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
public class DefaultAlarmFormatter extends BaseAlarmFormatter {
    /**
     * 格式化okHttp的request和response数据
     *
     * @param request  request
     * @param response response
     * @return 格式化okHttp的request和response数据
     */
    @Override
    public String alarmFormat(Request request, Response response) {

        StringBuffer stringBuffer = new StringBuffer("HTTP execute fail！");

        if (request != null) {
            stringBuffer.append(request.toString());
        }

        if (response != null) {
            stringBuffer.append("; ").append(response.toString());
        }

        return stringBuffer.toString();
    }
}
