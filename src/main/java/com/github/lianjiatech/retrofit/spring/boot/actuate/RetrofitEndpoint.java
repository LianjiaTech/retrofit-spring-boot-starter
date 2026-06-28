package com.github.lianjiatech.retrofit.spring.boot.actuate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import com.github.lianjiatech.retrofit.spring.boot.config.GlobalConnectionPoolProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.GlobalTimeoutProperty;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClientResolution;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.log.GlobalLogProperty;
import com.github.lianjiatech.retrofit.spring.boot.metrics.MetricsProperty;
import com.github.lianjiatech.retrofit.spring.boot.retry.GlobalRetryProperty;
import com.github.lianjiatech.retrofit.spring.boot.retry.RetryRule;

import retrofit2.CallAdapter;
import retrofit2.Converter;

/**
 * 暴露所有 {@code @RetrofitClient} 接口元信息的 Actuator Endpoint，访问路径 {@code /actuator/retrofit}。
 *
 * <p>通过 {@link ListableBeanFactory#getBeanNamesForType(Class)} 枚举所有 {@link RetrofitFactoryBean}，
 * 再用工厂前缀 {@code &} 取得 FactoryBean 实例本身（不触发 {@code getObject()}、不创建 Retrofit 代理），
 * 调用其 {@link RetrofitFactoryBean#describe()} 读取 client 级配置。{@code global} 段由
 * {@link RetrofitProperties} 直接映射。
 *
 * @author 陈添明
 */
@Endpoint(id = "retrofit")
public class RetrofitEndpoint {

    private final ListableBeanFactory beanFactory;

    private final RetrofitProperties properties;

    public RetrofitEndpoint(ListableBeanFactory beanFactory, RetrofitProperties properties) {
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    @ReadOperation
    public Map<String, Object> retrofitClients() {
        List<RetrofitClientResolution> clients = collectClients();
        Map<String, Object> result = new LinkedHashMap<>(3);
        result.put("count", clients.size());
        result.put("global", buildGlobalInfo());
        result.put("clients", clients);
        return result;
    }

    /**
     * 按接口全限定名查询单个 client。actuator 自动支持 {@code /actuator/retrofit/{interfaceName}}。
     * 未匹配到返回 {@code null}（HTTP 404）。
     */
    @ReadOperation
    public RetrofitClientResolution retrofitClient(@Selector String interfaceName) {
        for (RetrofitClientResolution client : collectClients()) {
            if (client.getInterfaceName().equals(interfaceName)) {
                return client;
            }
        }
        return null;
    }

    private List<RetrofitClientResolution> collectClients() {
        String[] beanNames = beanFactory.getBeanNamesForType(RetrofitFactoryBean.class);
        List<RetrofitClientResolution> clients = new ArrayList<>(beanNames.length);
        for (String beanName : beanNames) {
            // & 前缀取 FactoryBean 实例本身，而非其产品；不会触发 Retrofit 代理创建。
            RetrofitFactoryBean<?> factoryBean =
                    (RetrofitFactoryBean<?>)beanFactory.getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName);
            RetrofitClientResolution resolution = factoryBean.describe();
            resolution.setBeanName(beanName);
            clients.add(resolution);
        }
        clients.sort((a, b) -> a.getInterfaceName().compareTo(b.getInterfaceName()));
        return clients;
    }

    private RetrofitGlobalInfo buildGlobalInfo() {
        RetrofitGlobalInfo info = new RetrofitGlobalInfo();
        info.setAutoSetPrototypeScopeForPathMatchInterceptor(
                properties.isAutoSetPrototypeScopeForPathMathInterceptor());
        info.setEnableErrorDecoder(properties.isEnableErrorDecoder());
        info.setGlobalConverterFactories(converterFactoryNames(properties.getGlobalConverterFactories()));
        info.setGlobalCallAdapterFactories(callAdapterFactoryNames(properties.getGlobalCallAdapterFactories()));

        GlobalTimeoutProperty timeoutProperty = properties.getGlobalTimeout();
        RetrofitGlobalInfo.Timeout timeout = new RetrofitGlobalInfo.Timeout();
        timeout.setConnectMs(timeoutProperty.getConnectTimeoutMs());
        timeout.setReadMs(timeoutProperty.getReadTimeoutMs());
        timeout.setWriteMs(timeoutProperty.getWriteTimeoutMs());
        timeout.setCallMs(timeoutProperty.getCallTimeoutMs());
        info.setTimeout(timeout);

        GlobalConnectionPoolProperty poolProperty = properties.getGlobalConnectionPool();
        RetrofitGlobalInfo.ConnectionPool pool = new RetrofitGlobalInfo.ConnectionPool();
        pool.setMaxIdleConnections(poolProperty.getMaxIdleConnections());
        pool.setKeepAliveDurationMs(poolProperty.getKeepAliveDurationMs());
        info.setConnectionPool(pool);

        info.setLog(buildLog(properties.getGlobalLog()));
        info.setRetry(buildRetry(properties.getGlobalRetry()));
        info.setDegrade(buildDegrade(properties.getDegrade()));
        info.setMetrics(buildMetrics(properties.getMetrics()));
        return info;
    }

    private RetrofitGlobalInfo.Log buildLog(GlobalLogProperty property) {
        RetrofitGlobalInfo.Log log = new RetrofitGlobalInfo.Log();
        log.setEnable(property.isEnable());
        log.setLogName(property.getLogName());
        log.setLogLevel(property.getLogLevel().name());
        log.setLogStrategy(property.getLogStrategy().name());
        log.setAggregate(property.isAggregate());
        log.setRedactHeaders(property.getRedactHeaders() == null ? null : Arrays.asList(property.getRedactHeaders()));
        return log;
    }

    private RetrofitGlobalInfo.Retry buildRetry(GlobalRetryProperty property) {
        RetrofitGlobalInfo.Retry retry = new RetrofitGlobalInfo.Retry();
        retry.setEnable(property.isEnable());
        retry.setMaxRetries(property.getMaxRetries());
        retry.setIntervalMs(property.getIntervalMs());
        retry.setBackoffStrategy(property.getBackoffStrategy() == null ? null : property.getBackoffStrategy().name());
        retry.setMaxIntervalMs(property.getMaxIntervalMs());
        retry.setJitter(property.getJitter());
        List<Integer> statusCodes = new ArrayList<>();
        if (property.getRetryStatusCodes() != null) {
            for (int code : property.getRetryStatusCodes()) {
                statusCodes.add(code);
            }
        }
        retry.setRetryStatusCodes(statusCodes);
        List<String> exceptionClasses = new ArrayList<>();
        if (property.getRetryExceptionClasses() != null) {
            for (Class<? extends Throwable> clazz : property.getRetryExceptionClasses()) {
                exceptionClasses.add(clazz.getName());
            }
        }
        retry.setRetryExceptionClasses(exceptionClasses);
        List<String> rules = new ArrayList<>();
        if (property.getRetryRules() != null) {
            for (RetryRule rule : property.getRetryRules()) {
                rules.add(rule.name());
            }
        }
        retry.setRetryRules(rules);
        return retry;
    }

    private RetrofitGlobalInfo.Degrade buildDegrade(DegradeProperty property) {
        RetrofitGlobalInfo.Degrade degrade = new RetrofitGlobalInfo.Degrade();
        degrade.setDegradeType(property.getDegradeType());

        RetrofitGlobalInfo.Degrade.Sentinel sentinel = new RetrofitGlobalInfo.Degrade.Sentinel();
        sentinel.setEnable(property.getGlobalSentinelDegrade().isEnable());
        sentinel.setRuleCount(property.getGlobalSentinelDegrade().getRules() == null
                ? 0 : property.getGlobalSentinelDegrade().getRules().length);
        degrade.setSentinel(sentinel);

        RetrofitGlobalInfo.Degrade.Resilience4j resilience4j = new RetrofitGlobalInfo.Degrade.Resilience4j();
        resilience4j.setEnable(property.getGlobalResilience4jDegrade().isEnable());
        resilience4j.setCircuitBreakerConfigName(
                property.getGlobalResilience4jDegrade().getCircuitBreakerConfigName());
        degrade.setResilience4j(resilience4j);
        return degrade;
    }

    private RetrofitGlobalInfo.Metrics buildMetrics(MetricsProperty property) {
        RetrofitGlobalInfo.Metrics metrics = new RetrofitGlobalInfo.Metrics();
        metrics.setEnable(property.isEnable());
        metrics.setMetricNamePrefix(property.getMetricNamePrefix());
        metrics.setTagHost(property.getTags().isHost());
        metrics.setTagUri(property.getTags().isUri());
        metrics.setExtraTags(property.safeExtraTags());
        return metrics;
    }

    private static List<String> converterFactoryNames(Class<? extends Converter.Factory>[] classes) {
        List<String> names = new ArrayList<>();
        if (classes != null) {
            for (Class<?> clazz : classes) {
                names.add(clazz.getName());
            }
        }
        return names;
    }

    private static List<String> callAdapterFactoryNames(Class<? extends CallAdapter.Factory>[] classes) {
        List<String> names = new ArrayList<>();
        if (classes != null) {
            for (Class<?> clazz : classes) {
                names.add(clazz.getName());
            }
        }
        return names;
    }
}
