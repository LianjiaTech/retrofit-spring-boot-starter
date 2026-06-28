package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import retrofit2.Response;

/**
 * 集成测试：path-match 拦截器 include/exclude 优先级。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class PathMatchPrecedenceTest extends MockWebServerTest {

    @Autowired
    private PathMatchPrecedenceUserService service;

    @Test
    public void exclude_takesPrecedenceOverInclude() {
        mockServerReturnObject(USER_MIKE);
        Response<User> response = service.getUser(Long100);
        assertEquals(SUCCESS_CODE, response.code());
        // 同时命中 include 与 exclude，exclude 优先 → 不执行 doIntercept
        assertNull("命中 exclude 时不应执行拦截器", response.headers().get("path.match"));
    }

    @Test
    public void includeMatchAndExcludeMiss_invokesInterceptor() {
        mockServerReturnString(MIKE);
        Response<String> response = service.getName(Long100);
        assertEquals(SUCCESS_CODE, response.code());
        assertEquals("true", response.headers().get("path.match"));
    }
}
