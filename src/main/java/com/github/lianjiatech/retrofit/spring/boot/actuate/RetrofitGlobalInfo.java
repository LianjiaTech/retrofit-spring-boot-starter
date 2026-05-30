package com.github.lianjiatech.retrofit.spring.boot.actuate;

import java.util.List;
import java.util.Map;

/**
 * Retrofit 全局配置快照，对应 {@code RetrofitProperties} 的全部字段，作为 {@code /actuator/retrofit}
 * 响应中的 {@code global} 段。
 *
 * <p>当某个 client 的 {@code logging}/{@code retry} 标记为 {@code source="global"}（接口无对应注解、
 * 运行时回落到全局配置）时，消费方应查阅此段获取实际生效值。
 *
 * @author 陈添明
 */
public class RetrofitGlobalInfo {

    private boolean autoSetPrototypeScopeForPathMatchInterceptor;
    private boolean enableErrorDecoder;
    private List<String> globalConverterFactories;
    private List<String> globalCallAdapterFactories;
    private Timeout timeout;
    private ConnectionPool connectionPool;
    private Log log;
    private Retry retry;
    private Degrade degrade;
    private Metrics metrics;

    public static class Timeout {
        private int connectMs;
        private int readMs;
        private int writeMs;
        private int callMs;

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getWriteMs() {
            return writeMs;
        }

        public void setWriteMs(int writeMs) {
            this.writeMs = writeMs;
        }

        public int getCallMs() {
            return callMs;
        }

        public void setCallMs(int callMs) {
            this.callMs = callMs;
        }
    }

    public static class ConnectionPool {
        private int maxIdleConnections;
        private long keepAliveDurationMs;

        public int getMaxIdleConnections() {
            return maxIdleConnections;
        }

        public void setMaxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
        }

        public long getKeepAliveDurationMs() {
            return keepAliveDurationMs;
        }

        public void setKeepAliveDurationMs(long keepAliveDurationMs) {
            this.keepAliveDurationMs = keepAliveDurationMs;
        }
    }

    public static class Log {
        private boolean enable;
        private String logName;
        private String logLevel;
        private String logStrategy;
        private boolean aggregate;
        private List<String> redactHeaders;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getLogName() {
            return logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public String getLogStrategy() {
            return logStrategy;
        }

        public void setLogStrategy(String logStrategy) {
            this.logStrategy = logStrategy;
        }

        public boolean isAggregate() {
            return aggregate;
        }

        public void setAggregate(boolean aggregate) {
            this.aggregate = aggregate;
        }

        public List<String> getRedactHeaders() {
            return redactHeaders;
        }

        public void setRedactHeaders(List<String> redactHeaders) {
            this.redactHeaders = redactHeaders;
        }
    }

    public static class Retry {
        private boolean enable;
        private int maxRetries;
        private int intervalMs;
        private List<String> retryRules;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(int intervalMs) {
            this.intervalMs = intervalMs;
        }

        public List<String> getRetryRules() {
            return retryRules;
        }

        public void setRetryRules(List<String> retryRules) {
            this.retryRules = retryRules;
        }
    }

    public static class Degrade {
        private String degradeType;
        private Sentinel sentinel;
        private Resilience4j resilience4j;

        public String getDegradeType() {
            return degradeType;
        }

        public void setDegradeType(String degradeType) {
            this.degradeType = degradeType;
        }

        public Sentinel getSentinel() {
            return sentinel;
        }

        public void setSentinel(Sentinel sentinel) {
            this.sentinel = sentinel;
        }

        public Resilience4j getResilience4j() {
            return resilience4j;
        }

        public void setResilience4j(Resilience4j resilience4j) {
            this.resilience4j = resilience4j;
        }

        public static class Sentinel {
            private boolean enable;
            private int ruleCount;

            public boolean isEnable() {
                return enable;
            }

            public void setEnable(boolean enable) {
                this.enable = enable;
            }

            public int getRuleCount() {
                return ruleCount;
            }

            public void setRuleCount(int ruleCount) {
                this.ruleCount = ruleCount;
            }
        }

        public static class Resilience4j {
            private boolean enable;
            private String circuitBreakerConfigName;

            public boolean isEnable() {
                return enable;
            }

            public void setEnable(boolean enable) {
                this.enable = enable;
            }

            public String getCircuitBreakerConfigName() {
                return circuitBreakerConfigName;
            }

            public void setCircuitBreakerConfigName(String circuitBreakerConfigName) {
                this.circuitBreakerConfigName = circuitBreakerConfigName;
            }
        }
    }

    public static class Metrics {
        private boolean enable;
        private String metricNamePrefix;
        private boolean tagHost;
        private boolean tagUri;
        private Map<String, String> extraTags;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getMetricNamePrefix() {
            return metricNamePrefix;
        }

        public void setMetricNamePrefix(String metricNamePrefix) {
            this.metricNamePrefix = metricNamePrefix;
        }

        public boolean isTagHost() {
            return tagHost;
        }

        public void setTagHost(boolean tagHost) {
            this.tagHost = tagHost;
        }

        public boolean isTagUri() {
            return tagUri;
        }

        public void setTagUri(boolean tagUri) {
            this.tagUri = tagUri;
        }

        public Map<String, String> getExtraTags() {
            return extraTags;
        }

        public void setExtraTags(Map<String, String> extraTags) {
            this.extraTags = extraTags;
        }
    }

    public boolean isAutoSetPrototypeScopeForPathMatchInterceptor() {
        return autoSetPrototypeScopeForPathMatchInterceptor;
    }

    public void setAutoSetPrototypeScopeForPathMatchInterceptor(boolean autoSetPrototypeScopeForPathMatchInterceptor) {
        this.autoSetPrototypeScopeForPathMatchInterceptor = autoSetPrototypeScopeForPathMatchInterceptor;
    }

    public boolean isEnableErrorDecoder() {
        return enableErrorDecoder;
    }

    public void setEnableErrorDecoder(boolean enableErrorDecoder) {
        this.enableErrorDecoder = enableErrorDecoder;
    }

    public List<String> getGlobalConverterFactories() {
        return globalConverterFactories;
    }

    public void setGlobalConverterFactories(List<String> globalConverterFactories) {
        this.globalConverterFactories = globalConverterFactories;
    }

    public List<String> getGlobalCallAdapterFactories() {
        return globalCallAdapterFactories;
    }

    public void setGlobalCallAdapterFactories(List<String> globalCallAdapterFactories) {
        this.globalCallAdapterFactories = globalCallAdapterFactories;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Degrade getDegrade() {
        return degrade;
    }

    public void setDegrade(Degrade degrade) {
        this.degrade = degrade;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }
}
