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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


/**
 * @author 陈添明
 * @since 2023/12/17 12:20 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class RxJava3UserServiceTest extends MockWebServerTest {

    @Autowired
    private RxJava3UserService rxJava3UserService;

    @Test
    public void saveUserReturnCompletableForRx3() {
        mockServerReturnString(null);
        Completable completable = rxJava3UserService.saveUserReturnCompletableForRx3(USER_MIKE);
        completable.blockingAwait();
    }

    @Test
    public void getUserReturnSingleForRx3() {
        mockServerReturnObject(USER_MIKE);
        Single<User> userReturnSingle = rxJava3UserService.getUserReturnSingleForRx3(Long100);
        User user = userReturnSingle.blockingGet();
        assertNotNull(user);
        assertEquals(Long100, user.getId());
        assertEquals(MIKE, user.getName());
        assertEquals(INT20, user.getAge());
        assertTrue(user.isMale());
    }
}