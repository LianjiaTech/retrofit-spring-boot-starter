package com.github.lianjiatech.retrofit.spring.boot.test.integration.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;

import okhttp3.Headers;
import retrofit2.Response;

/**
 * @author 陈添明
 * @since 2023/12/17 10:56 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class InterceptorUserServiceTest extends MockWebServerTest {

    @Autowired
    private InterceptorUserService interceptorUserService;

    @Test
    public void getName() {
        mockServerReturnString(MIKE);
        Response<String> response = interceptorUserService.getName(Long100);
        String body = response.body();
        assertEquals(MIKE, body);
        Headers headers = response.headers();
        // 使用了全局拦截器
        String global = headers.get("global");
        assertEquals("true", global);
        // 使用路径匹配拦截器
        String pathMatch = headers.get("path.match");
        assertEquals("true", pathMatch);
        // 没有使用@Sign自定义注解拦截器
        String accessKeyId = headers.get("accessKeyId");
        String accessKeySecret = headers.get("accessKeySecret");
        assertNull(accessKeyId);
        assertNull(accessKeySecret);
    }

    @Test
    public void getUser() {
        mockServerReturnObject(USER_MIKE);
        Response<User> response = interceptorUserService.getUser(Long100);
        User body = response.body();
        assertEquals(USER_MIKE, body);
        Headers headers = response.headers();
        // 使用了全局拦截器
        String global = headers.get("global");
        assertEquals("true", global);
        // 没有使用路径匹配拦截器
        String pathMatch = headers.get("path.match");
        assertNull(pathMatch);
        // 没有使用@Sign自定义注解拦截器
        String accessKeyId = headers.get("accessKeyId");
        String accessKeySecret = headers.get("accessKeySecret");
        assertNull(accessKeyId);
        assertNull(accessKeySecret);
    }

    @Test
    public void getAll() {
        List<User> users = new ArrayList<>();
        users.add(USER_MIKE);
        users.add(USER_EMMA);
        mockServerReturnObject(users);

        Response<List<User>> response = interceptorUserService.getAll();
        List<User> body = response.body();
        assertEquals(users, body);
        Headers headers = response.headers();
        // 使用了全局拦截器
        String global = headers.get("global");
        assertEquals("true", global);
        // 使用路径匹配拦截器
        String pathMatch = headers.get("path.match");
        assertEquals("true", pathMatch);
        // 使用@Sign自定义注解拦截器
        String accessKeyId = headers.get("accessKeyId");
        String accessKeySecret = headers.get("accessKeySecret");
        assertEquals("root", accessKeyId);
        assertEquals("123456", accessKeySecret);
    }
}