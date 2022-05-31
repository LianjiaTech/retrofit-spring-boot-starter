package com.github.lianjiatech.retrofit.spring.boot.log;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 同一个请求的日志聚合在一起打印。 The logs of the same request are aggregated and printed together.
 * @author 陈添明
 * @since 2022/5/31 10:49 上午
 */
public class AggregateLoggingInterceptor extends LoggingInterceptor {

    public AggregateLoggingInterceptor(GlobalLogProperty globalLogProperty) {
        super(globalLogProperty);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Logging logging = findLogging(chain);
        if (!needLog(logging)) {
            return chain.proceed(chain.request());
        }
        LogLevel logLevel = logging == null ? globalLogProperty.getLogLevel() : logging.logLevel();
        LogStrategy logStrategy = logging == null ? globalLogProperty.getLogStrategy() : logging.logStrategy();
        BufferingLogger bufferingLogger = new BufferingLogger(matchLogger(logLevel));
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(bufferingLogger)
                .setLevel(HttpLoggingInterceptor.Level.valueOf(logStrategy.name()));
        Response response = httpLoggingInterceptor.intercept(chain);
        bufferingLogger.flush();
        return response;
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
