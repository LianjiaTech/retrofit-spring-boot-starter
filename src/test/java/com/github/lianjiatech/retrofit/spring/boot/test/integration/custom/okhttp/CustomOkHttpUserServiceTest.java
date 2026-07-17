package com.github.lianjiatech.retrofit.spring.boot.test.integration.custom.okhttp;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;

/**
 * @author 陈添明
 * @since 2023/12/17 10:03 上午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class CustomOkHttpUserServiceTest extends MockWebServerTest {

    @Autowired
    private CustomOkHttpUserService customOkHttpUserService;

    @Test(expected = RetrofitException.class)
    public void getUser() {
        // 自动okhttpClient，超时时间设置为1s，但是服务端2s才返回，因此该调用会超时
        mockServerReturnObject(USER_MIKE, 2);
        customOkHttpUserService.getUser(Long100);
    }
}