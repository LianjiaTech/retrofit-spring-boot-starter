package com.github.lianjiatech.retrofit.spring.boot.test.integration.degrade.sentinel;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;

/**
 * @author 陈添明
 * @since 2023/12/17 3:54 下午
 */
public class GlobalSentinelFallbackUserService implements GlobalSentinelUserService {

    @Override
    public String getName(Long id) {
        return MockWebServerTest.FALL_BACK;
    }
}
