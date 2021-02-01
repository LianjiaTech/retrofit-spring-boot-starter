package com.github.lianjiatech.retrofit.spring.boot.interceptor;

import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class DefaultLoggingInterceptor extends BaseLoggingInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(DefaultLoggingInterceptor.class);

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
            return logger::debug;
        } else if (level == LogLevel.ERROR) {
            return logger::error;
        } else if (level == LogLevel.INFO) {
            return logger::info;
        } else if (level == LogLevel.WARN) {
            return logger::warn;
        }
        throw new UnsupportedOperationException("We don't support this log level currently.");
    }
}
