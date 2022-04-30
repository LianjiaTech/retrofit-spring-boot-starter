package com.github.lianjiatech.retrofit.spring.boot.log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

import com.github.lianjiatech.retrofit.spring.boot.config.LogProperty;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Invocation;

/**
 * @author 陈添明
 * @since 2022/4/30 8:21 下午
 */
@Slf4j
public class LoggingInterceptor implements Interceptor {

    private final LogProperty logProperty;

    public LoggingInterceptor(LogProperty logProperty) {
        this.logProperty = logProperty;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        // 获取重试配置
        Logging logging = method.getDeclaringClass().getAnnotation(Logging.class);
        if (!needLog(logging)) {
            return chain.proceed(request);
        }
        LogLevel logLevel = logging == null ? logProperty.getGlobalLogLevel() : logging.logLevel();
        LogStrategy logStrategy = logging == null ? logProperty.getGlobalLogStrategy() : logging.logStrategy();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(matchLogger(logLevel));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(logStrategy.name()));
        return httpLoggingInterceptor.intercept(chain);
    }

    protected boolean needLog(Logging logging) {
        if (logProperty.isEnableGlobalLog()) {
            // 开启全局打印日志的情况下
            // 没配置@Logging，需要重试
            if (logging == null) {
                return true;
            }
            // 配置了@Logging，enable==true，需要打印日志
            if (logging.enable()) {
                return true;
            }
        } else {
            // 未开启全局日志打印
            // 配置了@Logging，enable==true，需要打印日志
            if (logging != null && logging.enable()) {
                return true;
            }
        }
        // 其他情况，不需要打印日志
        return false;
    }

    protected HttpLoggingInterceptor.Logger matchLogger(LogLevel level) {
        if (level == LogLevel.DEBUG) {
            return log::debug;
        } else if (level == LogLevel.ERROR) {
            return log::error;
        } else if (level == LogLevel.INFO) {
            return log::info;
        } else if (level == LogLevel.WARN) {
            return log::warn;
        } else if (level == LogLevel.TRACE) {
            return log::trace;
        }
        throw new UnsupportedOperationException("We don't support this log level currently.");
    }
}
