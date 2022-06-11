package com.github.lianjiatech.retrofit.spring.boot.test.reactive.completable;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.RetrofitTestApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author 陈添明
 */
@SpringBootTest(classes = RetrofitTestApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class Rxjava2CompletableTest {

    @Autowired
    private Rxjava2CompletableTestApi rxjava2CompletableTestApi;

    private MockWebServer server;

    private static Gson gson = new GsonBuilder()
            .create();

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
    public void ping() {
        MockResponse response = new MockResponse()
                .setResponseCode(200)
                .addHeader("Cache-Control", "no-cache");

        server.enqueue(response);

        Completable completable = rxjava2CompletableTestApi.ping();
        completable.blockingAwait();
    }
}
