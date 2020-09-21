package com.github.lianjiatech.retrofit.spring.boot.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import com.github.lianjiatech.retrofit.spring.boot.test.http.HttpApi;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 陈添明
 */
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
public class RetrofitExceptionTest {


    private static final Logger logger = LoggerFactory.getLogger(RetrofitExceptionTest.class);

    @Autowired
    private HttpApi httpApi;

    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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


    @Test(expected = Throwable.class)
//    @Test
    public void testHttpResponseFailure() throws IOException {
        // mock
        Map<String, Object> map = new HashMap<>(4);
        map.put("errorMessage", "我就是要手动报个错");
        map.put("errorCode", 500);

        Result mockResult = new Result<>()
                .setCode(0)
                .setMsg("ok")
                .setData(objectMapper.writeValueAsString(map));
        MockResponse response = new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(objectMapper.writeValueAsString(mockResult));
        server.enqueue(response);

        Person person = new Person().setId(1L).setName("test").setAge(10);
        Person error = httpApi.error(person);
        System.out.println(error);
    }

    @Test(expected = Throwable.class)
//    @Test
    public void testIOException() {
        Person person = new Person().setId(1L).setName("test").setAge(10);
        Person error = httpApi.error(person);
    }

}
