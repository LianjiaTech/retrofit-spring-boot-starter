package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Request;
import okhttp3.Response;

/**
 * retrofit调用报警格式化器，用于将request和response格式化为可阅读的String数据。
 * 在调用失败时，将该信息织入Exception消息中。
 *
 * @author 陈添明
 */
public abstract class BaseAlarmFormatter {

    /**
     * 格式化okHttp的request和response数据
     *
     * @param request  request
     * @param response response
     * @return 格式化okHttp的request和response数据
     */
    public abstract String alarmFormat(Request request, Response response);
}
