package com.github.lianjiatech.retrofit.spring.boot.test.integration.reactive.mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;

import reactor.core.publisher.Mono;

/**
 * @author 陈添明
 * @since 2023/12/17 12:15 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class MonoUserServiceTest extends MockWebServerTest {

    @Autowired
    private MonoUserService monoUserService;

        @Test
    public void getUserReturnMono() {
        mockServerReturnObject(USER_MIKE);
        Mono<User> userReturnMono = monoUserService.getUserReturnMono(Long100);
        User user = userReturnMono.block();
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }

}