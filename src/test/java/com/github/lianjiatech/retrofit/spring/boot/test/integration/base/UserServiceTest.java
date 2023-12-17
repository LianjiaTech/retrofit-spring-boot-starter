package com.github.lianjiatech.retrofit.spring.boot.test.integration.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author 陈添明
 * @since 2023/12/16 10:46 下午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class UserServiceTest extends MockWebServerTest {

    @Autowired
    private UserService userService;

    @Test
    public void getName() {
        mockServerReturnString(MIKE);
        String name = userService.getName(Long100);
        assertEquals(MIKE, name);
    }

    @Test
    public void getUser() {
        mockServerReturnObject(USER_MIKE);
        User user = userService.getUser(Long100);
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }

    @Test(expected = RetrofitException.class)
    public void getUserError() {
        mockServerReturnObject(USER_MIKE, 0, ERROR_CODE);
        User user = userService.getUser(Long100);
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }

    @Test
    public void getAll() {
        List<User> users = new ArrayList<>();
        users.add(USER_MIKE);
        users.add(USER_EMMA);
        mockServerReturnObject(users);

        List<User> all = userService.getAll();
        assertEquals(users, all);
    }

    @Test
    public void getUserAsync() {
        mockServerReturnObject(USER_MIKE);
        CompletableFuture<User> userCompletableFuture = userService.getUserAsync(Long100);
        assertNotNull(userCompletableFuture);
        User user;
        try {
            user = userCompletableFuture.get();
        } catch (Exception e) {
            throw new IllegalStateException("userCompletableFuture get error");
        }
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }

    @Test
    public void getUserReturnResponse() {
        mockServerReturnObject(USER_MIKE);
        Response<User> userReturnResponse = userService.getUserReturnResponse(Long100);
        assertNotNull(userReturnResponse);
        assertEquals(SUCCESS_CODE, userReturnResponse.code());
        User user = userReturnResponse.body();
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }

    @Test
    public void getUserReturnCall() {
        mockServerReturnObject(USER_MIKE);
        Call<User> userReturnCall = userService.getUserReturnCall(Long100);
        assertNotNull(userReturnCall);
        try {
            Response<User> userResponse = userReturnCall.execute();
            assertEquals(SUCCESS_CODE, userResponse.code());
            User user = userResponse.body();
            assertNotNull(user);
            assertEquals(Long100, user.getId());
            assertEquals(MIKE, user.getName());
            assertEquals(INT20, user.getAge());
            assertTrue(user.isMale());
        } catch (IOException e) {
            throw new IllegalStateException("userReturnCall execute error");
        }
    }

    @Test
    public void isMale() {
        mockServerReturnString(true);
        Boolean male = userService.isMale(Long100);
        assertTrue(male);
    }

    @Test
    public void saveUser() {
        mockServerReturnString(null);
        userService.saveUser(USER_MIKE);
    }

    @Test
    public void saveUserList() {
        List<User> users = new ArrayList<>();
        users.add(USER_MIKE);
        users.add(USER_EMMA);
        mockServerReturnString(null);
        userService.saveUserList(users);
    }
}