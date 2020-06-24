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
        return "这个请求失败了。。。。";
    }
}
