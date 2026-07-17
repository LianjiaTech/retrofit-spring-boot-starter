package com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.MethodTimeoutServices.ClassTimeoutService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.MethodTimeoutServices.MethodOverridesClassService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.MethodTimeoutServices.NoTimeoutService;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.timeout.MethodTimeoutServices.OnlyMethodTimeoutService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 方法级 @Timeout 集成测试，覆盖：
 * <ul>
 *     <li>类级 @Timeout 生效（慢响应触发超时）</li>
 *     <li>方法级 @Timeout 覆盖类级（慢方法用更长超时能成功）</li>
 *     <li>仅方法级 @Timeout（无类级注解）</li>
 *     <li>无 @Timeout 时使用全局配置</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class MethodTimeoutTest extends MockWebServerTest {

    @Autowired
    private ClassTimeoutService classTimeoutService;

    @Autowired
    private MethodOverridesClassService methodOverridesClassService;

    @Autowired
    private OnlyMethodTimeoutService onlyMethodTimeoutService;

    @Autowired
    private NoTimeoutService noTimeoutService;

    // ===== 类级 @Timeout =====

    @Test
    public void classTimeout_shortDelayCausesTimeout() {
        // 类级 readTimeout=1000ms；server 延迟 2s → 超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            classTimeoutService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // RetrofitIOException 是 RetrofitException 子类，统一 catch 即可
        }
    }

    // ===== 方法级覆盖类级 =====

    @Test
    public void methodTimeoutOverridesClass_shortResponse_succeeds() {
        // 类级 readTimeout=1000ms，方法级 readTimeout=5000ms
        // server 延迟 2s → 方法级 5000ms 允许，成功返回
        mockServerReturnObject(USER_MIKE, 2);
        assertNotNull(methodOverridesClassService.getUserWithLongTimeout(Long100));
    }

    @Test
    public void classTimeout_appliesWhenMethodHasNoOverride() {
        // 类级 readTimeout=1000ms，方法无 @Timeout
        // server 延迟 2s → 类级 1000ms 超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            methodOverridesClassService.getUser(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // RetrofitIOException 是 RetrofitException 子类，统一 catch 即可
        }
    }

    // ===== 仅方法级 @Timeout =====

    @Test
    public void onlyMethodTimeout_shortDelayCausesTimeout() {
        // 方法级 readTimeout=500ms；server 延迟 2s → 超时
        mockServerReturnObject(USER_MIKE, 2);
        try {
            onlyMethodTimeoutService.getUserShortTimeout(Long100);
            fail("expected timeout");
        } catch (RetrofitException e) {
            // RetrofitIOException 是 RetrofitException 子类，统一 catch 即可
        }
    }

    @Test
    public void onlyMethodTimeout_defaultMethod_usesGlobalTimeout() {
        // 无 @Timeout 的方法使用全局配置（10s）；0 延迟 → 成功
        mockServerReturnObject(USER_MIKE, 0);
        assertNotNull(onlyMethodTimeoutService.getUserDefault(Long100));
    }

    // ===== 无 @Timeout =====

    @Test
    public void noTimeout_usesGlobalConfig() {
        // 无 @Timeout，使用全局配置（10s）；0 延迟 → 成功
        mockServerReturnObject(USER_MIKE, 0);
        assertNotNull(noTimeoutService.getUser(Long100));
    }
}