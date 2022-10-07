package com.github.lianjiatech.retrofit.spring.boot.test.degrade;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.test.RetrofitTestApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author yukdawn@gmail.com
 */
@ActiveProfiles("sentinel")
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class DegradeSentinelTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final ObjectMapper objectMapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private MockWebServer server;

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

    @Test
    public void testDegrade() {
        long count = IntStream.range(0, 100).parallel().map((i) -> {
            try {
                Person mockPerson = new Person().setId(1L)
                        .setName("test")
                        .setAge(10);
                Result<?> mockResult = new Result<>()
                        .setCode(0)
                        .setMsg("ok")
                        .setData(mockPerson);
                MockResponse response = new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/text; charset=utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(objectMapper.writeValueAsString(mockResult))
                        .setBodyDelay(5, TimeUnit.SECONDS);
                server.enqueue(response);
                return applicationContext.getBean(DegradeSentinelApi.class).getPerson1(2L).getCode();
            } catch (Exception e) {
                return 100;
            }
        }).filter(i -> i == -1).count();
        System.out.println(count);
        Assert.assertTrue(count > 80L);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}