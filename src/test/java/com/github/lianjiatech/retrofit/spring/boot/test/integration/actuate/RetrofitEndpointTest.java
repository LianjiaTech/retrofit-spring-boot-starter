package com.github.lianjiatech.retrofit.spring.boot.test.integration.actuate;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.actuate.RetrofitEndpoint;
import com.github.lianjiatech.retrofit.spring.boot.actuate.RetrofitGlobalInfo;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClientResolution;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Actuator Endpoint 集成测试。覆盖：
 * <ul>
 *     <li>列举所有 client + count + global 段；</li>
 *     <li>@Selector 按接口名单查；</li>
 *     <li>接口级 @Logging/@Retry → source=interface 且展开值正确；</li>
 *     <li>无注解 client → source=global 且不重复值；</li>
 *     <li>超时显式覆盖不计入 inheritedFields；完全继承时全部计入；</li>
 *     <li>连接池未覆盖时全部继承；</li>
 *     <li>degrade 未启用 → enabled=false, type=none；</li>
 *     <li>global 段映射 RetrofitProperties 全字段。</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class},
        properties = "management.endpoints.web.exposure.include=retrofit")
@RunWith(SpringRunner.class)
public class RetrofitEndpointTest {

    private static final String ANNOTATED = AnnotatedActuateService.class.getName();
    private static final String PLAIN = PlainActuateService.class.getName();

    @Autowired
    private RetrofitEndpoint retrofitEndpoint;

    @Test
    public void endpointBeanIsRegistered() {
        assertNotNull("RetrofitEndpoint 应在暴露后被装配", retrofitEndpoint);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listAllClientsContainsCountGlobalAndClients() {
        Map<String, Object> result = retrofitEndpoint.retrofitClients();

        assertTrue(result.containsKey("count"));
        assertTrue(result.containsKey("global"));
        assertTrue(result.containsKey("clients"));

        List<RetrofitClientResolution> clients = (List<RetrofitClientResolution>)result.get("clients");
        assertEquals(clients.size(), result.get("count"));
        // 测试上下文里至少包含本测试声明的两个 client
        assertTrue(findByInterface(clients, ANNOTATED) != null);
        assertTrue(findByInterface(clients, PLAIN) != null);
    }

    @Test
    public void selectorReturnsSingleClient() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(PLAIN);
        assertNotNull(client);
        assertEquals(PLAIN, client.getInterfaceName());
    }

    @Test
    public void selectorReturnsNullForUnknownInterface() {
        assertNull(retrofitEndpoint.retrofitClient("com.x.NotExists"));
    }

    @Test
    public void annotatedClientResolvesInterfaceSourceWithExpandedValues() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(ANNOTATED);
        assertNotNull(client);

        RetrofitClientResolution.Logging logging = client.getLogging();
        assertEquals("interface", logging.getSource());
        assertEquals(Boolean.TRUE, logging.getEnable());
        assertEquals("DEBUG", logging.getLogLevel());
        assertEquals("BODY", logging.getLogStrategy());

        RetrofitClientResolution.Retry retry = client.getRetry();
        assertEquals("interface", retry.getSource());
        assertEquals(Boolean.TRUE, retry.getEnable());
        assertEquals(Integer.valueOf(5), retry.getMaxRetries());
        assertEquals(Integer.valueOf(200), retry.getIntervalMs());
        assertTrue(retry.getRetryRules().contains("OCCUR_EXCEPTION"));
    }

    @Test
    public void plainClientFallsBackToGlobalSource() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(PLAIN);
        assertNotNull(client);

        assertEquals("global", client.getLogging().getSource());
        assertNull("source=global 时不重复展开值", client.getLogging().getEnable());
        assertEquals("global", client.getRetry().getSource());
        assertNull(client.getRetry().getMaxRetries());
    }

    @Test
    public void explicitTimeoutIsNotMarkedInherited() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(ANNOTATED);
        assertTrue(client.isTimeoutEffective());

        RetrofitClientResolution.Timeout timeout = client.getTimeout();
        // 接口显式配置 connect/read = 3000，不应在 inheritedFields 中
        assertEquals(3000, timeout.getConnectMs());
        assertEquals(3000, timeout.getReadMs());
        assertFalse(timeout.getInheritedFields().contains("connectMs"));
        assertFalse(timeout.getInheritedFields().contains("readMs"));
        // write/call 未覆盖 → 继承全局（application.yml: write=5000, call=0）
        assertEquals(5000, timeout.getWriteMs());
        assertTrue(timeout.getInheritedFields().contains("writeMs"));
        assertTrue(timeout.getInheritedFields().contains("callMs"));
    }

    @Test
    public void plainClientInheritsAllTimeoutAndPool() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(PLAIN);
        RetrofitClientResolution.Timeout timeout = client.getTimeout();
        // application.yml 全局：connect/read/write=5000, call=0
        assertEquals(5000, timeout.getConnectMs());
        assertTrue(timeout.getInheritedFields().contains("connectMs"));
        assertTrue(timeout.getInheritedFields().contains("readMs"));
        assertTrue(timeout.getInheritedFields().contains("writeMs"));
        assertTrue(timeout.getInheritedFields().contains("callMs"));

        RetrofitClientResolution.Pool pool = client.getPool();
        assertEquals(5, pool.getMaxIdleConnections());
        assertEquals(300_000L, pool.getKeepAliveDurationMs());
        assertTrue(pool.getInheritedFields().contains("maxIdleConnections"));
        assertTrue(pool.getInheritedFields().contains("keepAliveDurationMs"));
    }

    @Test
    public void degradeDisabledByDefault() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(PLAIN);
        RetrofitClientResolution.Degrade degrade = client.getDegrade();
        assertNotNull(degrade);
        assertFalse(degrade.isEnabled());
        assertEquals("none", degrade.getType());
    }

    @Test
    public void clientBaseFieldsResolved() {
        RetrofitClientResolution client = retrofitEndpoint.retrofitClient(PLAIN);
        assertEquals("${test.baseUrl}", client.getBaseUrl());
        assertNotNull("beanName 应被填充", client.getBeanName());
        // errorDecoder 默认值
        assertTrue(client.getErrorDecoder().contains("DefaultErrorDecoder"));
        assertFalse(client.isValidateEagerly());
        // 未指定 sourceOkHttpClient → null
        assertNull(client.getSourceOkHttpClient());
    }

    @Test
    public void globalSectionMapsProperties() {
        RetrofitGlobalInfo global = (RetrofitGlobalInfo)retrofitEndpoint.retrofitClients().get("global");
        assertNotNull(global);
        // application.yml: global-log.enable=true, log-strategy=basic
        assertTrue(global.getLog().isEnable());
        assertEquals("BASIC", global.getLog().getLogStrategy());
        // global-retry.enable=false
        assertFalse(global.getRetry().isEnable());
        assertEquals(2, global.getRetry().getMaxRetries());
        // global-timeout
        assertEquals(5000, global.getTimeout().getConnectMs());
        assertEquals(0, global.getTimeout().getCallMs());
        // connection pool
        assertEquals(5, global.getConnectionPool().getMaxIdleConnections());
        // degrade
        assertEquals("none", global.getDegrade().getDegradeType());
        assertFalse(global.getDegrade().getSentinel().isEnable());
        assertEquals("defaultCircuitBreakerConfig",
                global.getDegrade().getResilience4j().getCircuitBreakerConfigName());
        // metrics 默认关闭
        assertFalse(global.getMetrics().isEnable());
        assertEquals("retrofit.client", global.getMetrics().getMetricNamePrefix());
    }

    private RetrofitClientResolution findByInterface(List<RetrofitClientResolution> clients, String name) {
        for (RetrofitClientResolution c : clients) {
            if (c.getInterfaceName().equals(name)) {
                return c;
            }
        }
        return null;
    }
}
