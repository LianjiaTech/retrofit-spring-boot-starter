package com.github.lianjiatech.retrofit.spring.boot.test.integration.scalars;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import okhttp3.mockwebserver.MockResponse;

import static org.junit.Assert.assertEquals;

/**
 * 集成测试：关闭 enable-scalars-converter 后，String 返回值回退到旧行为（经 Jackson 反序列化），
 * 验证开关可关闭 ScalarsConverterFactory 的前置注入。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@TestPropertySource(properties = "retrofit.enable-scalars-converter=false")
@RunWith(SpringRunner.class)
public class ScalarsConverterDisabledTest extends MockWebServerTest {

    @Autowired
    private ScalarsUserService service;

    @Test
    public void string_quotedJsonLiteral_strippedByJackson_whenDisabled() {
        // 关闭 Scalars：带引号的 JSON 字符串字面量被 Jackson 解析为去引号的 String。
        server.enqueue(new MockResponse().setResponseCode(SUCCESS_CODE).setBody("\"hello\""));
        assertEquals("hello", service.quotedString());
    }
}
