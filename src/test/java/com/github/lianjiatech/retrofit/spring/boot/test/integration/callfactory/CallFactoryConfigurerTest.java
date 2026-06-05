package com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.core.CallFactoryConfigurer;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory.CallFactoryConfigurerServices.DefaultCallTimeoutService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.callfactory.CallFactoryConfigurerServices.ShortCallTimeoutService;

/**
 * 集成测试：CallFactoryConfigurer SPI。
 * <ul>
 *     <li>TestCallFactoryConfigurer 对 DefaultCallTimeoutService 动态覆盖 callTimeout</li>
 *     <li>@DynamicCallTimeout(ms=500) 注解使慢响应触发超时</li>
 *     <li>无注解的方法使用全局默认超时（5000ms），快响应正常返回</li>
 *     <li>ShortCallTimeoutService 通过 @Timeout.callTimeoutMs=500 静态配置超时</li>
 * </ul>
 */
@SpringBootTest(classes = {RetrofitBootApplication.class, CallFactoryConfigurerTest.TestConfig.class})
@RunWith(SpringRunner.class)
public class CallFactoryConfigurerTest extends MockWebServerTest {

    @Autowired
    private DefaultCallTimeoutService defaultCallTimeoutService;

    @Autowired
    private ShortCallTimeoutService shortCallTimeoutService;

    @Configuration
    static class TestConfig {
        @Bean
        public CallFactoryConfigurer testCallFactoryConfigurer() {
            return new TestCallFactoryConfigurer();
        }
    }

    @Test
    public void dynamicCallTimeout_shortDelayCausesError() {
        // @DynamicCallTimeout(ms=500) + server delay 2s → callTimeout 超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            defaultCallTimeoutService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // ok — callTimeout 超时被 ErrorDecoder 包装
        }
    }

    @Test
    public void noAnnotation_defaultTimeout_allowsFastResponse() {
        // 无 @DynamicCallTimeout 注解 → 使用全局默认 readTimeout=5000ms，0 延迟正常返回
        mockServerReturnObject(USER_MIKE, 0);
        assertNotNull(defaultCallTimeoutService.getUserNoAnnotation(Long100));
    }

    @Test
    public void staticCallTimeout_shortDelayCausesError() {
        // @Timeout.callTimeoutMs=500 + server delay 2s → 静态 callTimeout 超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            shortCallTimeoutService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // ok
        }
    }
}