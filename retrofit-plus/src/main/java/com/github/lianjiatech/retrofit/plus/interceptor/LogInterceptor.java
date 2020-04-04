/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lianjiatech.retrofit.plus.interceptor;

import com.github.lianjiatech.retrofit.plus.core.RequestHolder;
import com.github.lianjiatech.retrofit.plus.core.ResponseHolder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.event.Level;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author 陈添明
 */
@Slf4j
public final class LogInterceptor implements Interceptor {

    public interface Logger {
        /**
         * 打印日志
         *
         * @param message 日志message
         */
        void log(String message);
    }

    public LogInterceptor(Logger logger, LogStrategy logStrategy) {
        this.logger = logger;
        this.logStrategy = logStrategy;
    }

    private final Logger logger;

    private LogStrategy logStrategy;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (logStrategy == LogStrategy.NONE) {
            return chain.proceed(request);
        }
        Response response = null;
        long start = System.currentTimeMillis();
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("[HTTP FAILED:] " + e);
            throw e;
        } finally {
            // 无论请求成功或者失败，都要打印日志
            printLog(request, response, start);
        }
        return response;
    }

    private void printLog(Request request, Response response, long start) {
        try {
            RequestHolder requestHolder = new RequestHolder(request);
            ResponseHolder responseHolder = new ResponseHolder(response, response == null ? null : response.body());
            // 是否打印body
            boolean logBody = logStrategy == LogStrategy.BODY;
            // 是否打印headers
            boolean logHeaders = logBody || logStrategy == LogStrategy.HEADERS;
            // 打印请求基本信息
            String basicRequestString = requestHolder.basicString();
            if (StringUtils.hasText(basicRequestString)) {
                logger.log(basicRequestString);
            }
            logHeadersAndBody(logBody, logHeaders, requestHolder.headersString(), requestHolder.bodyString());
            // 打印响应基本信息
            String basicResponseString = responseHolder.basicString(System.currentTimeMillis() - start);
            if (StringUtils.hasText(basicResponseString)) {
                logger.log(basicResponseString);
            }
            logHeadersAndBody(logBody, logHeaders, responseHolder.headersString(), responseHolder.bodyString());
        } catch (Exception e) {
            // 打印日志异常，不能影响正常功能
            log.warn("Failed to print log!", e);
        }
    }


    private void logHeadersAndBody(boolean logBody, boolean logHeaders, String headerString, String bodyString) {
        if (logHeaders) {
            if (StringUtils.hasText(headerString)) {
                logger.log(headerString);
            }
            if (logBody) {
                if (StringUtils.hasText(bodyString)) {
                    logger.log(bodyString);
                }
            }
        }
    }

    public static LogInterceptor.Logger innerLogger(Level level, org.slf4j.Logger logger) {
        if (level == Level.DEBUG) {
            return logger::debug;
        } else if (level == Level.ERROR) {
            return logger::error;
        } else if (level == Level.INFO) {
            return logger::info;
        } else if (level == Level.TRACE) {
            return logger::trace;
        } else if (level == Level.WARN) {
            return logger::warn;
        }
        throw new UnsupportedOperationException("We don't support this log level currently.");
    }
}
