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

/**
 * @author 陈添明
 * @since 2023/12/17 12:50 下午
 */
@SpringBootTest(classes = {RetrofitBootApplication.class},
        properties = {"retrofit.degrade.degrade-type=resilience4j",
            "retrofit.degrade.global-resilience4j-degrade.enable=true",
            "retrofit.degrade.global-resilience4j-degrade.circuit-breaker-config-name=testCircuitBreakerConfig"})
@RunWith(SpringRunner.class)
public class GlobalResilience4jUserServiceTest extends MockWebServerTest {

    @Autowired
    private GlobalResilience4jUserService globalResilience4jUserService;

    @Test
    public void getName() {
        Set<String> set = IntStream.range(0, 50).parallel().mapToObj(i -> {
            mockServerReturnObject(MIKE, 0, SUCCESS_CODE);
            try {
                return globalResilience4jUserService.getName(Long100);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toSet());
        assertTrue(set.contains(FALL_BACK));
    }
}