package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import okhttp3.Request;
import okhttp3.Response;

/**
 * if response is not successful, then decode the HTTP information into exception message.
 *
 * @author 陈添明
 */
public class DefaultErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(Request request, Response response) {
        if (!response.isSuccessful()) {
            throw RetrofitException.errorStatus(request, response);
        }
        return null;
    }
}
