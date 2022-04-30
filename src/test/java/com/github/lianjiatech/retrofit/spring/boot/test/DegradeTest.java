package com.github.lianjiatech.retrofit.spring.boot.test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import com.github.lianjiatech.retrofit.spring.boot.test.http.DegradeApi;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author 陈添明
 * @since 2022/1/21 4:20 下午
 */
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
public class DegradeTest {

    @Autowired
    private DegradeApi degradeApi;

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
    public void test() throws IOException {
        Random random = new Random(System.currentTimeMillis());
        IntStream.range(0, 1000).parallel().forEach((i) -> {
            try {
                Person mockPerson = new Person().setId(1L)
                        .setName("test")
                        .setAge(10);
                Result mockResult = new Result<>()
                        .setCode(0)
                        .setMsg("ok")
                        .setData(mockPerson);
                MockResponse response = new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/text; charset=utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .setBody(objectMapper.writeValueAsString(mockResult))
                        .setHeadersDelay(random.nextInt(1000), TimeUnit.MILLISECONDS);
                server.enqueue(response);
                System.out.println(degradeApi.getPerson(2L).getCode());
            } catch (Exception e) {
                System.out.println("抛出异常：" + e.getMessage());
            } finally {
                System.out.println("当前请求轮次： " + (i + 1));
            }
        });
    }
}
