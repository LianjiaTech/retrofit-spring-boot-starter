package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author 陈添明
 */
@Slf4j
public class DefaultLoggingInterceptor extends BaseLoggingInterceptor {

    private final HttpLoggingInterceptor httpLoggingInterceptor;

    public DefaultLoggingInterceptor(LogLevel logLevel, LogStrategy logStrategy) {
        super(logLevel, logStrategy);
        HttpLoggingInterceptor.Logger logger = httpLoggingInterceptorLogger(logLevel);
        httpLoggingInterceptor = new HttpLoggingInterceptor(logger);
        String name = logStrategy.name();
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.valueOf(name);
        httpLoggingInterceptor.setLevel(level);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        return httpLoggingInterceptor.intercept(chain);
    }


    public HttpLoggingInterceptor.Logger httpLoggingInterceptorLogger(LogLevel level) {
        if (level == LogLevel.DEBUG) {
            return log::debug;
        } else if (level == LogLevel.ERROR) {
            return log::error;
        } else if (level == LogLevel.INFO) {
            return log::info;
        } else if (level == LogLevel.WARN) {
            return log::warn;
        }
        throw new UnsupportedOperationException("We don't support this log level currently.");
    }
}
