package com.github.lianjiatech.retrofit.spring.boot.log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Invocation;

/**
 * @author 陈添明
 * @since 2022/4/30 8:21 下午
 */
public class LoggingInterceptor implements Interceptor {

    protected final GlobalLogProperty globalLogProperty;

    /**
     * 缓存非 aggregate 模式下每个 (method, service) 对应的 HttpLoggingInterceptor。
     * 该拦截器的 Logger 是无状态的，可安全地并发复用；aggregate 模式需要有状态的 BufferingLogger，不缓存。
     */
    private final ConcurrentHashMap<Method, ConcurrentHashMap<Class<?>, HttpLoggingInterceptor>> interceptorCache =
            new ConcurrentHashMap<>(64);

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

        boolean aggregate = logging == null ? globalLogProperty.isAggregate() : logging.aggregate();
        Invocation invocation = chain.request().tag(Invocation.class);

        if (!aggregate && invocation != null) {
            HttpLoggingInterceptor cached = interceptorCache
                    .computeIfAbsent(invocation.method(), k -> new ConcurrentHashMap<>(4))
                    .computeIfAbsent(invocation.service(), k -> buildInterceptor(logging, logStrategy));
            return cached.intercept(chain);
        }

        // aggregate 模式：每次请求必须创建独立的 BufferingLogger 以避免并发串扰
        LogLevel logLevel = logging == null ? globalLogProperty.getLogLevel() : logging.logLevel();
        String logName =
                logging == null || logging.logName().isEmpty() ? globalLogProperty.getLogName() : logging.logName();
        HttpLoggingInterceptor.Logger matchLogger = matchLogger(logName, logLevel);
        BufferingLogger bufferingLogger = new BufferingLogger(matchLogger);
        HttpLoggingInterceptor httpLoggingInterceptor =
                buildInterceptorWithLogger(logging, logStrategy, bufferingLogger);
        Response response = httpLoggingInterceptor.intercept(chain);
        bufferingLogger.flush();
        return response;
    }

    private HttpLoggingInterceptor buildInterceptor(Logging logging, LogStrategy logStrategy) {
        LogLevel logLevel = logging == null ? globalLogProperty.getLogLevel() : logging.logLevel();
        String logName =
                logging == null || logging.logName().isEmpty() ? globalLogProperty.getLogName() : logging.logName();
        return buildInterceptorWithLogger(logging, logStrategy, matchLogger(logName, logLevel));
    }

    private HttpLoggingInterceptor buildInterceptorWithLogger(Logging logging, LogStrategy logStrategy,
            HttpLoggingInterceptor.Logger logger) {
        HttpLoggingInterceptor interceptor =
                new HttpLoggingInterceptor(logger).setLevel(HttpLoggingInterceptor.Level.valueOf(logStrategy.name()));
        String[] globalRedactHeaders = globalLogProperty.getRedactHeaders();
        if (globalRedactHeaders != null) {
            for (String redactHeader : globalRedactHeaders) {
                interceptor.redactHeader(redactHeader);
            }
        }
        if (logging != null && logging.redactHeaders() != null) {
            for (String redactHeader : logging.redactHeaders()) {
                interceptor.redactHeader(redactHeader);
            }
        }
        return interceptor;
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
