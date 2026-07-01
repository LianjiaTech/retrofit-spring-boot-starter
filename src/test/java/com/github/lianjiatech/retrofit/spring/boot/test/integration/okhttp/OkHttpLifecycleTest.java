package com.github.lianjiatech.retrofit.spring.boot.test.integration.okhttp;

import java.io.IOException;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 集成测试：验证 4.0.6 commit 引入的 {@code OkHttpClientLifecycle} 钩子在 Spring
 * ApplicationContext 关闭时正确释放 OkHttp 资源（dispatcher / connectionPool）。
 * <p>
 * 该测试独立启动 / 关闭一个 SpringApplication，避免污染主测试上下文。
 *
 * @author 陈添明
 */
public class OkHttpLifecycleTest {

    @Test
    public void contextClose_shutdownsDispatcher_andEvictsConnectionPool() throws IOException {
        // 启一个独立的 mock server 给该测试使用，避免占用默认 8080 端口冲突
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            server.enqueue(new MockResponse().setResponseCode(200).setBody("hi"));

            ConfigurableApplicationContext ctx = new SpringApplication(RetrofitBootApplication.class)
                    .run("--server.port=0",
                            "--test.baseUrl=" + server.url("/").toString());

            RetrofitConfigBean cfg = ctx.getBean(RetrofitConfigBean.class);
            OkHttpClient base = cfg.getBaseOkHttpClient();
            assertNotNull(base);
            assertTrue("启动时 dispatcher executor 应处于运行态",
                    !base.dispatcher().executorService().isShutdown());

            // 关闭 ApplicationContext，触发 OkHttpClientLifecycle.destroy()
            ctx.close();

            assertTrue("context.close() 后 dispatcher executor 必须已 shutdown",
                    base.dispatcher().executorService().isShutdown());
            assertEquals("connectionPool 应被清空", 0, base.connectionPool().idleConnectionCount());
        } finally {
            server.shutdown();
        }
    }
}
