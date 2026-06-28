package com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitIOException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.TimeoutDimensionsServices.CallTimeoutService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.TimeoutDimensionsServices.DefaultTimeoutService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.TimeoutDimensionsServices.ReadTimeoutService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 集成测试：覆盖各超时维度。
 * <ul>
 *     <li>注解级 readTimeout 覆盖全局：极小值会让慢响应触发超时</li>
 *     <li>注解级 callTimeout 覆盖全局：作为整体调用上限</li>
 *     <li>未覆盖时使用全局配置（5000ms），快响应正常返回</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class TimeoutDimensionsTest extends MockWebServerTest {

    @Autowired
    private ReadTimeoutService readTimeoutService;

    @Autowired
    private CallTimeoutService callTimeoutService;

    @Autowired
    private DefaultTimeoutService defaultTimeoutService;

    @Test
    public void readTimeout_overridesGlobal_shortDelayCausesIOException() {
        // readTimeout=500ms；server 延迟 2s 才返回 → 应触发超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            readTimeoutService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitIOException e) {
            // 默认 ErrorDecoder 把 IOException 包装成 RetrofitIOException — ok
        } catch (RetrofitException e) {
            // ok
        }
    }

    @Test
    public void callTimeout_overridesGlobal_shortDelayCausesError() {
        // callTimeout=500ms；server 延迟 2s 才返回 → 应触发超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            callTimeoutService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // ok
        }
    }

    @Test
    public void defaultTimeout_5000ms_allowsFastResponse() {
        // 全局 read/connect/write 都是 5s，0 延迟应正常返回
        mockServerReturnObject(USER_MIKE, 0);
        assertNotNull(defaultTimeoutService.getUser(Long100));
    }
}
