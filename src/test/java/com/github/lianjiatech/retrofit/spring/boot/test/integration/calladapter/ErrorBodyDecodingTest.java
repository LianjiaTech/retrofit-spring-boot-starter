package com.github.lianjiatech.retrofit.spring.boot.test.integration.calladapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

/**
 * 集成测试：禁用 ErrorDecoder 时，BodyCallAdapter 走 errorBody → Converter 反序列化路径。
 * <p>
 * 当 retrofit.enable-error-decoder=false 时，{@code ErrorDecoderInterceptor} 不会被装入
 * OkHttp client，非 2xx 响应不会被中断，由 BodyCallAdapter 用响应类型的 Converter 解码 errorBody。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class},
        properties = {"retrofit.enable-error-decoder=false"})
@TestPropertySource(properties = {"retrofit.enable-error-decoder=false"})
@RunWith(SpringRunner.class)
public class ErrorBodyDecodingTest extends MockWebServerTest {

    @Autowired
    private ErrorBodyUserService service;

    @Test
    public void non2xx_withErrorBody_isDecodedAsResponseType() {
        // 即使 status=500，但 body 是合法的 User JSON → converter 仍可反序列化
        mockServerReturnObject(USER_MIKE, 0, 500);

        User user = service.getUser(Long100);
        assertNotNull("禁用 ErrorDecoder 时应返回反序列化后的 errorBody", user);
        assertEquals(MIKE, user.getName());
    }
}
