package com.github.lianjiatech.retrofit.spring.boot.test.integration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author 陈添明
 * @since 2023/12/16 10:17 下午
 */

public class MockWebServerTest {

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final long Long100 = 100L;
    public static final long Long200 = 200L;
    public static final String MIKE = "mike";
    public static final String EMMA = "emma";
    public static final String FALL_BACK = "fallBack";
    public static final int INT20 = 20;
    public static final int INT30 = 30;


    public static final User USER_MIKE = new User().setId(Long100)
            .setName(MIKE)
            .setAge(INT20)
            .setMale(true);

    public static final User USER_EMMA = new User().setId(Long200)
            .setName(EMMA)
            .setAge(INT30)
            .setMale(false);

    public static final User USER_FALL_BACK = new User().setId(Long200)
            .setName(FALL_BACK)
            .setAge(-1)
            .setMale(false);



    public MockWebServer server;

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @SneakyThrows
    public static String writeValueAsString(Object obj) {
        return JSON_MAPPER.writeValueAsString(obj);
    }

    @Before
    public void before() throws IOException {
        server = new MockWebServer();
        server.start(8080);

    }

    @After
    public void after() throws IOException {
        server.close();
    }

    public void mockServerReturnString(Object object) {
        // mock
        MockResponse response = new MockResponse()
                .setResponseCode(SUCCESS_CODE)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(writeValueAsString(object));
        server.enqueue(response);
    }

    public void mockServerReturnObject(Object object) {
        mockServerReturnObject(object, 0);
    }

    public void mockServerReturnObject(Object object, int bodyDelaySeconds) {
       mockServerReturnObject(object, bodyDelaySeconds, SUCCESS_CODE);
    }

    public void mockServerReturnObject(Object object, int bodyDelaySeconds, int responseCode) {
        MockResponse response = new MockResponse()
                .setResponseCode(responseCode)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBodyDelay(bodyDelaySeconds, TimeUnit.SECONDS)
                .setBody(writeValueAsString(object));
        server.enqueue(response);
    }

}
