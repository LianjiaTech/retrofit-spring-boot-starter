package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.sentinel;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author 陈添明
 * @since 2023/12/17 5:18 下午
 */
@Service
public class SentinelFallbackUserService implements SentinelUserService {
    @Override
    public String getName(Long id) {
        return MockWebServerTest.FALL_BACK;
    }

    @Override
    public User getUser(Long id) {
        return MockWebServerTest.USER_FALL_BACK;
    }
}
