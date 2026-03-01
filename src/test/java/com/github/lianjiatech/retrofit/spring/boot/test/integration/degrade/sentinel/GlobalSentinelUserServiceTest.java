package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.sentinel;

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
        properties = {"retrofit.degrade.degrade-type=sentinel", "retrofit.degrade.global-sentinel-degrade.enable=true",
            "retrofit.degrade.global-sentinel-degrade.rules[0].grade=0", "retrofit.degrade.global-sentinel-degrade.rules[0].count=2",
            "retrofit.degrade.global-sentinel-degrade.rules[0].time-window=5"})
@RunWith(SpringRunner.class)
public class GlobalSentinelUserServiceTest extends MockWebServerTest {

    @Autowired
    private GlobalSentinelUserService globalSentinelUserService;

    @Test
    public void getName() {
        Set<String> set = IntStream.range(0, 50).parallel().mapToObj(i -> {
            mockServerReturnObject(MIKE, 0, SUCCESS_CODE);
            try {
                return globalSentinelUserService.getName(Long100);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toSet());
        assertTrue(set.contains(FALL_BACK));
    }
}