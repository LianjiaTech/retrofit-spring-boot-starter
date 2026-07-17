package com.github.lianjiatech.retrofit.spring.boot.test.integration.scalars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import okhttp3.mockwebserver.MockResponse;

import static org.junit.Assert.assertEquals;

/**
 * 集成测试：默认开启 enable-scalars-converter 时，ScalarsConverterFactory 被最前置注入，
 * String / 基本类型返回值按响应体原始文本返回，POJO 仍由 Jackson 反序列化。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class ScalarsConverterTest extends MockWebServerTest {

    @Autowired
    private ScalarsUserService service;

    private void enqueueBody(String body) {
        server.enqueue(new MockResponse().setResponseCode(SUCCESS_CODE).setBody(body));
    }

    @Test
    public void string_plainText_returnedAsRawBody() {
        // 纯文本不是合法 JSON，旧默认（Jackson）会抛 JsonParseException；Scalars 直接原样返回。
        enqueueBody("pong");
        assertEquals("pong", service.plainText());
    }

    @Test
    public void string_quotedJsonLiteral_keepsQuotes() {
        // 带引号的 JSON 字符串字面量，Scalars 原样返回（保留引号），区别于 Jackson 的去引号行为。
        enqueueBody("\"hello\"");
        assertEquals("\"hello\"", service.quotedString());
    }

    @Test
    public void integerValue_parsedFromText() {
        enqueueBody("42");
        assertEquals(Integer.valueOf(42), service.intValue());
    }

    @Test
    public void longValue_parsedFromText() {
        enqueueBody("12345678901");
        assertEquals(Long.valueOf(12345678901L), service.longValue());
    }

    @Test
    public void booleanValue_parsedFromText() {
        enqueueBody("true");
        assertEquals(Boolean.TRUE, service.booleanValue());
    }

    @Test
    public void doubleValue_parsedFromText() {
        enqueueBody("3.14");
        assertEquals(3.14, service.doubleValue(), 0.0);
    }

    @Test
    public void pojo_stillDeserializedByJackson() {
        // 回归：前置 Scalars 不处理 POJO，仍落到 Jackson 正常反序列化。
        mockServerReturnObject(USER_MIKE);
        User user = service.user();
        assertEquals(USER_MIKE, user);
    }
}
