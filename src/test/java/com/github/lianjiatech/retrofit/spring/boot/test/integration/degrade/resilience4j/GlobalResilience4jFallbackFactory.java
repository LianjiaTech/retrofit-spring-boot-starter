package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.resilience4j;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 陈添明
 * @since 2023/12/17 3:54 下午
 */
@Component
@Slf4j
public class GlobalResilience4jFallbackFactory implements FallbackFactory<GlobalResilience4jUserService> {

    @Override
    public GlobalResilience4jUserService create(Throwable cause) {
        log.error("触发熔断了！", cause);
        return id -> MockWebServerTest.FALL_BACK;
    }
}
