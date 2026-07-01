package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetryFailedException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 集成测试：覆盖重试穷尽 / 规则不命中场景。
 * <p>
 * 现有 RetryUserServiceTest / GlobalRetryUserServiceTest 只覆盖了"3 次失败再成功"，
 * 本测试补足穷尽与规则不命中的关键分支。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class RetryExhaustionTest extends MockWebServerTest {

    @Autowired
    private RetryExhaustionUserService service;

    @Test
    public void status5xx_exhausted_returnsLastResponse_throwsViaErrorDecoder() {
        // maxRetries=2 → 共调 3 次；全部 5xx → 最后由 ErrorDecoderInterceptor 抛 RetrofitException
        mockServerReturnObject(USER_MIKE, 0, 500);
        mockServerReturnObject(USER_MIKE, 0, 500);
        mockServerReturnObject(USER_MIKE, 0, 500);

        try {
            service.getUserStatusRetry(Long100);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertTrue(e.getMessage().contains("invalid Response"));
        }
    }

    @Test
    public void ioException_exhausted_throwsRetryFailedException() {
        // OCCUR_IO_EXCEPTION 规则：穷尽后 RetryInterceptor 抛 RetryFailedException；
        // 该异常进一步被 ErrorDecoderInterceptor 的 exceptionDecode 包装成 RetrofitException
        // （cause = RetryFailedException, cause.cause = 原始 IOException）
        for (int i = 0; i < 3; i++) {
            server.enqueue(new MockResponse()
                    .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                    .setResponseCode(SUCCESS_CODE)
                    .setBodyDelay(0, TimeUnit.MILLISECONDS));
        }

        try {
            service.getUserIoRetry(Long100);
            fail("expected RetrofitException wrapping RetryFailedException");
        } catch (RetrofitException e) {
            assertNotNull(e.getCause());
            assertTrue("cause 应是 RetryFailedException，实际: " + e.getCause(),
                    e.getCause() instanceof RetryFailedException);
            assertNotNull("RetryFailedException 应保留底层 IOException 作为 cause",
                    e.getCause().getCause());
        }
    }

    @Test
    public void status5xx_butOnlyIoRule_doesNotRetry() {
        // OCCUR_IO_EXCEPTION 规则下，5xx 不在重试规则里 — 第 1 次失败就交给 ErrorDecoder
        // 这里只 enqueue 1 次，如果发生重试就会因队列耗尽报错
        mockServerReturnObject(USER_MIKE, 0, 500);

        try {
            service.getUserIoRetry(Long100);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertEquals("only one server request should have been issued", 1, server.getRequestCount());
        }
    }
}
