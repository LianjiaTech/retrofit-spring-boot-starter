package com.github.lianjiatech.retrofit.spring.boot.test.decoder;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.test.RetrofitTestApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author 陈添明
 */
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
public class ErrorDecoderTest {

    @Autowired
    private ErrorDecoderTestApi errorDecoderTestApi;

    private MockWebServer server;

    private static final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Before
    public void before() throws IOException {
        System.out.println("=========开启MockWebServer===========");
        server = new MockWebServer();
        server.start(8080);

    }

    @After
    public void after() throws IOException {
        System.out.println("=========关闭MockWebServer===========");
        server.close();
    }

    @Test(expected = Throwable.class)
    public void test() throws JsonProcessingException {
        // mock
        Person mockPerson = new Person().setId(1L)
                .setName("test")
                .setAge(10);

        Result mockResult = new Result<>()
                .setCode(0)
                .setMsg("ok")
                .setData(objectMapper.writeValueAsString(mockPerson));
        MockResponse response = new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(objectMapper.writeValueAsString(mockResult));
        server.enqueue(response);

        Person person = new Person().setId(1L).setName("test").setAge(10);
        Person error = errorDecoderTestApi.error(person);
        System.out.println(error);

    }

}
