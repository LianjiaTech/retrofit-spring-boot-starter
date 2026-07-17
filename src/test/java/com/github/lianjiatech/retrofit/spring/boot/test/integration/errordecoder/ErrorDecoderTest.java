package com.github.lianjiatech.retrofit.spring.boot.test.integration.errordecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.base.UserService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.errordecoder.CustomErrorDecoder.BizException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 集成测试：覆盖 ErrorDecoder 三个分支：
 * <ul>
 *     <li>默认 ErrorDecoder：4xx/5xx 都抛 {@link RetrofitException}</li>
 *     <li>自定义 ErrorDecoder（{@link CustomErrorDecoder}）：4xx 改抛 BizException</li>
 *     <li>IO 异常路径（连接断开）：自定义 decoder 包装为 BizException</li>
 *     <li>RetrofitException 在 exceptionDecode 中透传</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class ErrorDecoderTest extends MockWebServerTest {

    @Autowired
    private UserService userService; // 默认 DefaultErrorDecoder

    @Autowired
    private CustomErrorDecoderUserService customService;

    /* ---------- 默认 ErrorDecoder ---------- */

    @Test(expected = RetrofitException.class)
    public void defaultDecoder_4xx_throwsRetrofitException() {
        mockServerReturnObject(USER_MIKE, 0, 404);
        userService.getUser(Long100);
    }

    @Test(expected = RetrofitException.class)
    public void defaultDecoder_5xx_throwsRetrofitException() {
        mockServerReturnObject(USER_MIKE, 0, 503);
        userService.getUser(Long100);
    }

    @Test
    public void defaultDecoder_2xx_returnsBody() {
        mockServerReturnObject(USER_MIKE);
        assertNotNull(userService.getUser(Long100));
    }

    /* ---------- 自定义 ErrorDecoder ---------- */

    @Test
    public void customDecoder_4xx_throwsBizException() {
        mockServerReturnObject(USER_MIKE, 0, 400);
        try {
            customService.getUser(Long100);
            fail("expected BizException");
        } catch (BizException e) {
            assertEquals(400, e.code);
            assertEquals("client-error", e.getMessage());
        }
    }

    @Test
    public void customDecoder_5xx_fallsBackToRetrofitException() {
        // 自定义 decoder 在 5xx 时复用默认行为
        mockServerReturnObject(USER_MIKE, 0, 500);
        try {
            customService.getUser(Long100);
            fail("expected RetrofitException");
        } catch (RetrofitException e) {
            assertTrue(e.getMessage().contains("invalid Response"));
        }
    }

    @Test
    public void customDecoder_ioException_isWrapped() {
        // 让 server 在收到请求后断开连接，触发 IOException
        server.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                .setResponseCode(SUCCESS_CODE)
                .setBodyDelay(0, TimeUnit.MILLISECONDS));

        try {
            customService.getUser(Long100);
            fail("expected BizException from ioExceptionDecode");
        } catch (BizException e) {
            assertEquals(0, e.code);
            assertTrue("应包含底层 IOException 类名: " + e.getMessage(),
                    e.getMessage().startsWith("io:"));
        }
    }
}
