package com.github.lianjiatech.retrofit.spring.boot.test.integration.chooser;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author 陈添明
 * @since 2023/12/17 10:03 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class ChooserOkHttpUserServiceTest extends MockWebServerTest {

    @Autowired
    private ChooserOkHttpUserService chooserOkHttpUserService;

    @Test
    public void getUser() {
        mockServerReturnObject(USER_MIKE);
        User user = chooserOkHttpUserService.getUser(Long100);
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }
}