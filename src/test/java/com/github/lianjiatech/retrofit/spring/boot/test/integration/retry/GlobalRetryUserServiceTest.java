package com.github.lianjiatech.retrofit.spring.boot.test.integration.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

/**
 * @author 陈添明
 * @since 2023/12/17 12:50 下午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class}, properties = {"retrofit.global-retry.enable=true", "retrofit.global-retry.interval-ms=10",
    "retrofit.global-retry.max-retries=3", "retrofit.global-retry.retry-rules=[RESPONSE_STATUS_NOT_2XX]"})
@RunWith(SpringRunner.class)
public class GlobalRetryUserServiceTest extends MockWebServerTest {

    @Autowired
    private GlobalRetryUserService globalRetryUserService;

    @Test
    public void getName() {
        mockServerReturnObject(MIKE, 0, ERROR_CODE);
        mockServerReturnObject(MIKE, 0, ERROR_CODE);
        mockServerReturnObject(MIKE, 0, SUCCESS_CODE);
        globalRetryUserService.getName(Long100);
    }

    @Test
    public void getUser() {
        mockServerReturnObject(USER_MIKE, 0, ERROR_CODE);
        mockServerReturnObject(USER_MIKE, 0, ERROR_CODE);
        mockServerReturnObject(USER_MIKE, 0, SUCCESS_CODE);
        User user = globalRetryUserService.getUser(Long100);
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
        mockServerReturnObject(users, 0, ERROR_CODE);
        mockServerReturnObject(users, 0, ERROR_CODE);
        mockServerReturnObject(users, 0, SUCCESS_CODE);

        List<User> all = globalRetryUserService.getAll();
        assertEquals(users, all);
    }
}