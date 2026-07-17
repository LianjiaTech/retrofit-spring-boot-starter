package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.resilience4j;

import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

/**
 * @author 陈添明
 * @since 2023/12/17 5:18 下午
 */
@Service
@Slf4j
public class Resilience4jFallbackFactory implements FallbackFactory<Resilience4jUserService> {

    @Override
    public Resilience4jUserService create(Throwable cause) {
        log.error("触发熔断了！", cause);
        return new Resilience4jUserService() {
            @Override
            public String getName(Long id) {
                return MockWebServerTest.FALL_BACK;
            }

            @Override
            public User getUser(Long id) {
                return MockWebServerTest.USER_FALL_BACK;
            }
        };
    }
}
