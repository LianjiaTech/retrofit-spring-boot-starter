package com.github.lianjiatech.retrofit.spring.boot.test.integration.reactive.rx;

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

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author 陈添明
 * @since 2023/12/17 12:20 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class RxJava2UserServiceTest extends MockWebServerTest {

    @Autowired
    private RxJava2UserService rxJava2UserService;

    @Test
    public void saveUserReturnCompletableForRx2() {
        mockServerReturnString(null);
        Completable completable = rxJava2UserService.saveUserReturnCompletableForRx2(USER_MIKE);
        completable.blockingAwait();
    }

    @Test
    public void getUserReturnSingleForRx2() {
        mockServerReturnObject(USER_MIKE);
        Single<User> userReturnSingle = rxJava2UserService.getUserReturnSingleForRx2(Long100);
        User user = userReturnSingle.blockingGet();
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }
}