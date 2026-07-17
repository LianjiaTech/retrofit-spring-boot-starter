package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import static org.junit.Assert.*;

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
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class LogUserServiceTest extends MockWebServerTest {

    @Autowired
    private LogUserService logUserService;

    @Test
    public void getName() {
        mockServerReturnString(MIKE);
        String name = logUserService.getName(Long100);
        assertEquals(MIKE, name);
    }

    @Test
    public void getUser() {
        mockServerReturnObject(USER_MIKE);
        User user = logUserService.getUser(Long100);
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

        List<User> all = logUserService.getAll();
        assertEquals(users, all);
    }

    @Test
    public void getNameWithHeader() {
        mockServerReturnString(MIKE);
        String name = logUserService.getNameWithHeader(Long100, "ASDFASD", "AAAAXXXXX");
        assertEquals(MIKE, name);
    }
}