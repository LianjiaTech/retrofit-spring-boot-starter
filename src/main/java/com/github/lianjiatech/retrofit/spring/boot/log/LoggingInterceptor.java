package com.github.lianjiatech.retrofit.spring.boot.log;

import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Invocation;

import java.io.IOException;

/**
 * @author 陈添明
 * @since 2022/4/30 8:21 下午
 */
public class LoggingInterceptor implements Interceptor {

    protected final GlobalLogProperty globalLogProperty;

    public LoggingInterceptor(GlobalLogProperty globalLogProperty) {
        this.globalLogProperty = globalLogProperty;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Logging logging = findLogging(chain);
        if (!needLog(logging)) {
            return chain.proceed(chain.request());
        }
        LogStrategy logStrategy = logging == null ? globalLogProperty.getLogStrategy() : logging.logStrategy();
        if (logStrategy == LogStrategy.NONE) {
            return chain.proceed(chain.request());
        }

        LogLevel logLevel = logging == null ? globalLogProperty.getLogLevel() : logging.logLevel();
        boolean aggregate = logging == null ? globalLogProperty.isAggregate() : logging.aggregate();
        String logName = logging == null || logging.logName().isEmpty() ? globalLogProperty.getLogName() : logging.logName();
        HttpLoggingInterceptor.Logger matchLogger = matchLogger(logName, logLevel);
        HttpLoggingInterceptor.Logger logger = aggregate ? new BufferingLogger(matchLogger) : matchLogger;
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(logger)
                .setLevel(HttpLoggingInterceptor.Level.valueOf(logStrategy.name()));
        Response response = httpLoggingInterceptor.intercept(chain);
        if (aggregate) {
            ((BufferingLogger) logger).flush();
        }
        return response;
    }

    protected Logging findLogging(Chain chain) {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return null;
        }
        return AnnotationExtendUtils.findMergedAnnotation(invocation.method(), invocation.service(), Logging.class);
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

    protected HttpLoggingInterceptor.Logger matchLogger(String logName, LogLevel level) {
        Logger log = LoggerFactory.getLogger(logName);
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

    private static class BufferingLogger implements HttpLoggingInterceptor.Logger {

        private StringBuilder buffer = new StringBuilder(System.lineSeparator());

        private final HttpLoggingInterceptor.Logger delegate;

        public BufferingLogger(HttpLoggingInterceptor.Logger delegate) {
            this.delegate = delegate;
        }

        @Override
        public void log(String message) {
            buffer.append(message).append(System.lineSeparator());
        }

        public void flush() {
            delegate.log(buffer.toString());
            buffer = new StringBuilder(System.lineSeparator());
        }
    }
}
