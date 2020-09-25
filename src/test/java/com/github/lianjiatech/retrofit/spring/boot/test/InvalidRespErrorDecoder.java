package com.github.lianjiatech.retrofit.spring.boot.test;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 陈添明
 */
public class InvalidRespErrorDecoder implements ErrorDecoder {

    @Override
    public RuntimeException invalidRespDecode(Request request, Response response) {
        return null;
    }
}
