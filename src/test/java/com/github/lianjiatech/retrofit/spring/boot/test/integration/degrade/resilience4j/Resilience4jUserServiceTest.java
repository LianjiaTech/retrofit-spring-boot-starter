package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.resilience4j;

import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
@SpringBootTest(classes = {RetrofitBootApplication.class},
        properties = {"retrofit.degrade.degrade-type=resilience4j"})
@RunWith(SpringRunner.class)
public class Resilience4jUserServiceTest extends MockWebServerTest {

    @Autowired
    private Resilience4jUserService resilience4jUserService;

    @Test
    public void getName() {
        Set<String> set = IntStream.range(0, 50).parallel().mapToObj(i -> {
            mockServerReturnObject(MIKE, 0, SUCCESS_CODE);
            try {
                return resilience4jUserService.getName(Long100);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toSet());
        assertTrue(set.contains(FALL_BACK));
    }

    @Test
    public void getUser() {
        Set<User> set = IntStream.range(0, 50).parallel().mapToObj(i -> {
            mockServerReturnObject(MIKE, 0, SUCCESS_CODE);
            try {
                return resilience4jUserService.getUser(Long100);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toSet());
        assertTrue(!set.contains(USER_FALL_BACK));
    }
}