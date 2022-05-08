package com.github.lianjiatech.retrofit.spring.boot.log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

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

    protected final GlobalLogProperty globalLogProperty;

    public LoggingInterceptor(GlobalLogProperty globalLogProperty) {
        this.globalLogProperty = globalLogProperty;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        Logging logging = AnnotationExtendUtils.findMergedAnnotation(method, method.getDeclaringClass(), Logging.class);
        if (!needLog(logging)) {
            return chain.proceed(request);
        }
        LogLevel logLevel = logging == null ? globalLogProperty.getLogLevel() : logging.logLevel();
        LogStrategy logStrategy = logging == null ? globalLogProperty.getLogStrategy() : logging.logStrategy();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(matchLogger(logLevel));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(logStrategy.name()));
        return httpLoggingInterceptor.intercept(chain);
    }

    protected boolean needLog(Logging logging) {
        if (globalLogProperty.isEnable()) {
            if (logging == null) {
                return true;
            }
            return logging.enable();
        } else {
            return logging != null && logging.enable();
        }
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
