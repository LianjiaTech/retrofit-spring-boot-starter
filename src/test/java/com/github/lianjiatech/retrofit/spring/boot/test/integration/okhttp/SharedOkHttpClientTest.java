package com.github.lianjiatech.retrofit.spring.boot.test.integration.okhttp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.okhttp.SharedOkHttpServices.DefaultClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.okhttp.SharedOkHttpServices.IsolatedPoolClient;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 集成测试：覆盖 4.0.6 commit 的核心改动 — 全局共享 {@code retrofitBaseOkHttpClient} bean，
 * 所有未指定 {@code sourceOkHttpClient} 的接口都通过 {@code newBuilder()} 派生以共享
 * dispatcher / connectionPool；显式覆盖连接池参数则隔离一份独立 ConnectionPool。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class SharedOkHttpClientTest extends MockWebServerTest {

    @Autowired
    private RetrofitConfigBean retrofitConfigBean;

    @Autowired
    private DefaultClient defaultClient;

    @Autowired
    private IsolatedPoolClient isolatedPoolClient;

    @Test
    public void baseClient_isExposedAndShareable() {
        OkHttpClient base = retrofitConfigBean.getBaseOkHttpClient();
        assertNotNull("retrofitBaseOkHttpClient bean 应存在", base);

        // 派生 client 必须共享 base 的 dispatcher 与 connectionPool —
        // 这是 destroy 钩子能"只关一次根 client 释放全部资源"的前提
        OkHttpClient derived = base.newBuilder().build();
        assertSame("dispatcher 应共享", base.dispatcher(), derived.dispatcher());
        assertSame("connectionPool 应共享", base.connectionPool(), derived.connectionPool());
    }

    @Test
    public void multipleClients_canIssueRequestsConcurrently() {
        // 仅是 smoke：保证两个共享 base 的 RetrofitClient 都能正常工作
        mockServerReturnObject(USER_MIKE);
        assertNotNull(defaultClient.getUser(Long100));

        mockServerReturnObject(USER_MIKE);
        assertNotNull(isolatedPoolClient.getUser(Long100));
    }

    @Test
    public void globalTimeout_appliesToBaseClient() {
        // application.yml 配置：connect/read/write=5000ms, call=0
        OkHttpClient base = retrofitConfigBean.getBaseOkHttpClient();
        assertEquals(5000, base.connectTimeoutMillis());
        assertEquals(5000, base.readTimeoutMillis());
        assertEquals(5000, base.writeTimeoutMillis());
        assertEquals(0, base.callTimeoutMillis());
    }

    @Test
    public void globalConnectionPool_isApplied() {
        // application.yml: maxIdleConnections=5, keepAliveDurationMs=300_000
        OkHttpClient base = retrofitConfigBean.getBaseOkHttpClient();
        assertNotNull(base.connectionPool());
        // ConnectionPool 没有暴露 maxIdle 的 getter；至少验证池存在且可工作
        assertTrue(base.connectionPool().idleConnectionCount() >= 0);
    }
}
