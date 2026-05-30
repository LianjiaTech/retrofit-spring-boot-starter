package com.github.lianjiatech.retrofit.spring.boot.core;

import java.util.List;

/**
 * {@code @RetrofitClient} 接口的运行期解析结果，由 {@link RetrofitFactoryBean#describe()} 产出。
 *
 * <p>这是一个不含任何 actuator / web 依赖的纯值对象（core 包），用于在不触发产品 Bean
 * （即 {@code FactoryBean.getObject()} 创建的 Retrofit 代理）的前提下，向上层（如 Actuator
 * Endpoint）暴露单个 client 的完整配置。所有"继承全局"的字段（超时/连接池中值为
 * {@link Constants#INVALID_VALUE} 的项）都已在此解析为最终生效值，并通过
 * {@code inheritedTimeoutFields}/{@code inheritedPoolFields} 标明哪些字段来自全局兜底。
 *
 * @author 陈添明
 */
public class RetrofitClientResolution {

    private String beanName;

    private String interfaceName;

    private String baseUrl;

    /**
     * 已解析的最终 baseUrl。仅当该接口已被实例化（{@link RetrofitFactoryBean#getObject()} 触发过）
     * 时才有值，否则为 {@code null}（懒解析未触发）。
     */
    private String resolvedBaseUrl;

    private String serviceId;

    private String path;

    private List<String> converterFactories;

    private List<String> callAdapterFactories;

    private String fallback;

    private String fallbackFactory;

    private String errorDecoder;

    private boolean validateEagerly;

    private String sourceOkHttpClient;

    /**
     * 超时配置。当 {@link #sourceOkHttpClient} 非空时这些值不生效（由源 OkHttpClient 决定），
     * 此时 {@link #timeoutEffective} 为 {@code false}。
     */
    private Timeout timeout;

    private Pool pool;

    /**
     * 超时/连接池配置是否生效。{@code sourceOkHttpClient} 为空（NO_SOURCE_OK_HTTP_CLIENT）时为 true。
     */
    private boolean timeoutEffective;

    private Logging logging;

    private Retry retry;

    private Degrade degrade;

    /**
     * 超时配置（毫秒）。各字段已解析为最终生效值；{@code inheritedFields} 列出哪些字段
     * 在接口注解上为 {@link Constants#INVALID_VALUE}、实际取自全局 {@code GlobalTimeoutProperty}。
     */
    public static class Timeout {
        private int connectMs;
        private int readMs;
        private int writeMs;
        private int callMs;
        private List<String> inheritedFields;

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

        public List<String> getInheritedFields() {
            return inheritedFields;
        }

        public void setInheritedFields(List<String> inheritedFields) {
            this.inheritedFields = inheritedFields;
        }
    }

    /**
     * 连接池配置。语义同 {@link Timeout}：值已解析为最终生效值，{@code inheritedFields}
     * 标明哪些来自全局 {@code GlobalConnectionPoolProperty}。
     */
    public static class Pool {
        private int maxIdleConnections;
        private long keepAliveDurationMs;
        private List<String> inheritedFields;

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

        public List<String> getInheritedFields() {
            return inheritedFields;
        }

        public void setInheritedFields(List<String> inheritedFields) {
            this.inheritedFields = inheritedFields;
        }
    }

    /**
     * 日志配置。{@code source="interface"} 表示接口上存在 {@code @Logging} 注解，此时其余字段为注解展开值；
     * {@code source="global"} 表示接口无注解、运行时回落到全局日志配置，此时其余字段为 {@code null}，
     * 消费方应查阅顶层 {@code global.log} 段。方法级 {@code @Logging} 不在此处下钻（运行时方法注解优先）。
     */
    public static class Logging {
        private String source;
        private Boolean enable;
        private String logLevel;
        private String logStrategy;
        private Boolean aggregate;
        private String logName;
        private List<String> redactHeaders;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
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

        public Boolean getAggregate() {
            return aggregate;
        }

        public void setAggregate(Boolean aggregate) {
            this.aggregate = aggregate;
        }

        public String getLogName() {
            return logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }

        public List<String> getRedactHeaders() {
            return redactHeaders;
        }

        public void setRedactHeaders(List<String> redactHeaders) {
            this.redactHeaders = redactHeaders;
        }
    }

    /**
     * 重试配置。{@code source} 语义同 {@link Logging}。方法级 {@code @Retry} 不下钻。
     */
    public static class Retry {
        private String source;
        private Boolean enable;
        private Integer maxRetries;
        private Integer intervalMs;
        private List<String> retryRules;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Boolean getEnable() {
            return enable;
        }

        public void setEnable(Boolean enable) {
            this.enable = enable;
        }

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }

        public Integer getIntervalMs() {
            return intervalMs;
        }

        public void setIntervalMs(Integer intervalMs) {
            this.intervalMs = intervalMs;
        }

        public List<String> getRetryRules() {
            return retryRules;
        }

        public void setRetryRules(List<String> retryRules) {
            this.retryRules = retryRules;
        }
    }

    /**
     * 熔断降级配置。{@code enabled} 取自 {@code RetrofitDegrade.isEnableDegrade(interface)}，
     * {@code type} 为全局 {@code degrade.degrade-type}（none/sentinel/resilience4j）。
     */
    public static class Degrade {
        private boolean enabled;
        private String type;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getResolvedBaseUrl() {
        return resolvedBaseUrl;
    }

    public void setResolvedBaseUrl(String resolvedBaseUrl) {
        this.resolvedBaseUrl = resolvedBaseUrl;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getConverterFactories() {
        return converterFactories;
    }

    public void setConverterFactories(List<String> converterFactories) {
        this.converterFactories = converterFactories;
    }

    public List<String> getCallAdapterFactories() {
        return callAdapterFactories;
    }

    public void setCallAdapterFactories(List<String> callAdapterFactories) {
        this.callAdapterFactories = callAdapterFactories;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(String fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }

    public String getErrorDecoder() {
        return errorDecoder;
    }

    public void setErrorDecoder(String errorDecoder) {
        this.errorDecoder = errorDecoder;
    }

    public boolean isValidateEagerly() {
        return validateEagerly;
    }

    public void setValidateEagerly(boolean validateEagerly) {
        this.validateEagerly = validateEagerly;
    }

    public String getSourceOkHttpClient() {
        return sourceOkHttpClient;
    }

    public void setSourceOkHttpClient(String sourceOkHttpClient) {
        this.sourceOkHttpClient = sourceOkHttpClient;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public boolean isTimeoutEffective() {
        return timeoutEffective;
    }

    public void setTimeoutEffective(boolean timeoutEffective) {
        this.timeoutEffective = timeoutEffective;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
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
}
