package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import okhttp3.mockwebserver.MockResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Metrics 拦截器全链路集成测试。覆盖：
 * <ul>
 *     <li>成功请求：timer 增长，tag 中 status=2xx / outcome=SUCCESS / uri=user/{id}；</li>
 *     <li>4xx/5xx 响应：状态桶正确；</li>
 *     <li>IO 异常：error counter 增长且业务异常照常上抛；</li>
 *     <li>active LongTaskTimer 至少被注册（即使在快速测试中 active=0）；</li>
 *     <li>指标名前缀来自 {@code retrofit.metrics.metric-name-prefix}（默认 retrofit.client）。</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class}, properties = "retrofit.metrics.enable=true")
@Import(MetricsTestConfig.class)
@RunWith(SpringRunner.class)
public class MetricsInterceptorTest extends MockWebServerTest {

    @Autowired
    private MetricsUserService metricsUserService;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Spring 测试上下文在同一份 ApplicationContext 内复用 MeterRegistry，导致跨测试方法计数累加。
     * 这里在每次测试前清空所有 meter，确保断言只针对当前用例的调用。
     */
    @org.junit.Before
    public void clearMeters() {
        // 父类 before 已经初始化 server，这里只清理 metrics
        meterRegistry.clear();
    }

    @Test
    public void successRequestRecordsTimer() {
        mockServerReturnObject(USER_MIKE);
        User user = metricsUserService.getUser(Long100);
        assertNotNull(user);

        Timer timer = meterRegistry.find("retrofit.client.requests")
                .tag("client", "MetricsUserService")
                .tag("method", "getUser")
                .tag("status", "2xx")
                .tag("outcome", "SUCCESS")
                .tag("uri", "user/{id}")
                .timer();
        assertNotNull("expected success timer with full tag set", timer);
        assertEquals(1L, timer.count());
        assertTrue("non-zero total time recorded", timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS) > 0);
    }

    @Test
    public void serverErrorRecordsTimerWith5xxBucket() {
        // 5xx 响应不抛异常（默认未启用 ErrorDecoder 拦截器抛错，但即使抛错 timer 也应已记录）
        // 这里我们直接检验 timer 的 5xx 桶
        MockResponse response = new MockResponse()
                .setResponseCode(503)
                .setBody("{\"id\":0,\"name\":\"x\",\"age\":0,\"male\":false}");
        server.enqueue(response);

        try {
            metricsUserService.getUser(Long100);
        } catch (Exception ignored) {
            // 5xx 在 ErrorDecoder 默认实现下可能抛异常，这里不关心业务结果
        }

        Timer timer = meterRegistry.find("retrofit.client.requests")
                .tag("status", "5xx")
                .tag("outcome", "SERVER_ERROR")
                .timer();
        assertNotNull("expected 5xx timer", timer);
        assertEquals(1L, timer.count());
    }

    @Test
    public void connectionFailureRecordsErrorCounterAndRethrows() throws Exception {
        // 关掉 mock server 模拟连接失败
        server.close();
        try {
            metricsUserService.getUser(Long100);
            fail("expected exception when server is down");
        } catch (Exception expected) {
            // OK：业务异常照常上抛
        }

        Counter errors = meterRegistry.find("retrofit.client.errors")
                .tag("client", "MetricsUserService")
                .tag("method", "getUser")
                .tag("status", "IO_ERROR")
                .tag("outcome", "IO_ERROR")
                .counter();
        assertNotNull("expected error counter for IO failure", errors);
        assertTrue(errors.count() >= 1.0d);
        // 同时 timer 也应该记录一笔（用于"失败也算 RT"）
        Timer timer = meterRegistry.find("retrofit.client.requests")
                .tag("status", "IO_ERROR")
                .timer();
        assertNotNull(timer);
        assertTrue(timer.count() >= 1L);
    }

    @Test
    public void longTaskTimerIsRegistered() {
        mockServerReturnObject(USER_MIKE);
        metricsUserService.getUser(Long100);

        LongTaskTimer ltt = meterRegistry.find("retrofit.client.requests.active")
                .tag("client", "MetricsUserService")
                .longTaskTimer();
        assertNotNull("active long-task-timer should be registered after a request", ltt);
        // 请求结束后 active=0
        assertEquals(0, ltt.activeTasks());
    }

    @Test
    public void multipleCallsAccumulate() {
        for (int i = 0; i < 3; i++) {
            mockServerReturnObject(USER_MIKE);
            metricsUserService.getUser(Long100);
        }
        Timer timer = meterRegistry.find("retrofit.client.requests")
                .tag("client", "MetricsUserService")
                .tag("method", "getUser")
                .tag("status", "2xx")
                .timer();
        assertNotNull(timer);
        assertEquals(3L, timer.count());
    }

    @Test
    public void differentMethodsCreateSeparateTimers() {
        mockServerReturnObject(USER_MIKE);
        metricsUserService.getUser(Long100);

        mockServerReturnString("ok");
        metricsUserService.create(MIKE);

        Timer getTimer = meterRegistry.find("retrofit.client.requests")
                .tag("method", "getUser")
                .tag("status", "2xx")
                .timer();
        Timer postTimer = meterRegistry.find("retrofit.client.requests")
                .tag("method", "create")
                .tag("status", "2xx")
                .timer();
        assertNotNull(getTimer);
        assertNotNull(postTimer);
        assertEquals(1L, getTimer.count());
        assertEquals(1L, postTimer.count());
    }

    @Test
    public void noMetricsLeakIntoUnrelatedNamespace() {
        mockServerReturnObject(USER_MIKE);
        metricsUserService.getUser(Long100);

        try {
            meterRegistry.get("retrofit.client.unknown.meter").meter();
            fail("unexpected meter found in registry");
        } catch (MeterNotFoundException expected) {
            // OK
        }
        // 全部 retrofit 指标必须以 retrofit.client. 前缀开头
        for (Meter m : meterRegistry.getMeters()) {
            String name = m.getId().getName();
            if (name.startsWith("retrofit.")) {
                assertTrue("unexpected metric name: " + name, name.startsWith("retrofit.client."));
            }
        }
    }
}
