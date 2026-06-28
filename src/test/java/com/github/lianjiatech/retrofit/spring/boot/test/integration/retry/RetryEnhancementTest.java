package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import static org.junit.Assert.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;

/**
 * 集成测试：覆盖重试增强能力 —— 指数退避、状态码条件触发、异常类型条件触发。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class RetryEnhancementTest extends MockWebServerTest {

    @Autowired
    private RetryEnhancementUserService service;

    /**
     * 指数退避 + jitter：前两次 500，第三次 200。maxRetries=3 足以重试到成功。
     */
    @Test
    public void exponentialBackoff_retriesUntilSuccess() {
        mockServerReturnObject(USER_MIKE, 0, ERROR_CODE);
        mockServerReturnObject(USER_MIKE, 0, ERROR_CODE);
        mockServerReturnObject(USER_MIKE, 0, SUCCESS_CODE);

        User user = service.getUserExponential(Long100);
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(3, server.getRequestCount());
    }

    /**
     * retryStatusCodes={503} 命中：503 → 503 → 200，应重试成功。
     */
    @Test
    public void statusCode503_inList_retries() {
        mockServerReturnObject(USER_MIKE, 0, 503);
        mockServerReturnObject(USER_MIKE, 0, 503);
        mockServerReturnObject(USER_MIKE, 0, SUCCESS_CODE);

        User user = service.getUserStatusCode503(Long100);
        assertNotNull(user);
        assertEquals(3, server.getRequestCount());
    }

    /**
     * retryStatusCodes={503} 不命中：返回 500 不在列表 → 不重试，仅 1 次请求。
     */
    @Test
    public void statusCode500_notInList_doesNotRetry() {
        mockServerReturnObject(USER_MIKE, 0, 500);

        try {
            service.getUserStatusCode503(Long100);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertEquals("only one request should have been issued", 1, server.getRequestCount());
        }
    }

    /**
     * retryExceptionClasses={SocketTimeoutException} 命中：读超时触发重试。
     * readTimeoutMs=200，前两次服务端不返回任何响应（NO_RESPONSE）→ 等待响应头时读超时
     * （SocketTimeoutException 在拦截器层抛出）；第三次正常返回。
     */
    @Test
    public void socketTimeout_inList_retries() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        mockServerReturnObject(USER_MIKE, 0, SUCCESS_CODE);

        User user = service.getUserOnlyTimeout(Long100);
        assertNotNull(user);
        assertEquals(3, server.getRequestCount());
    }

    /**
     * retryExceptionClasses={SocketTimeoutException} 不命中：连接断开抛出的 IOException
     * 并非 SocketTimeoutException → 虽命中 OCCUR_IO_EXCEPTION 规则，但被异常类型收窄阻止重试，仅 1 次请求。
     */
    @Test
    public void nonTimeoutIoException_notInList_doesNotRetry() {
        server.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                .setBodyDelay(0, TimeUnit.MILLISECONDS));

        try {
            service.getUserOnlyTimeout(Long100);
            fail("expected exception");
        } catch (Exception e) {
            assertEquals("only one request should have been issued", 1, server.getRequestCount());
        }
    }
}
