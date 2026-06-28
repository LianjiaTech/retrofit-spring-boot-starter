package com.github.lianjiatech.retrofit.spring.boot.test.integration.bdfprocessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 集成测试：覆盖 M2 — {@code PathMatchInterceptorBdfProcessor} 仅做类型匹配，
 * 不应触发候选类的 {@code <clinit>}。
 * <p>
 * 老实现使用 {@code Class.forName(name)}（initialize=true）会过早触发用户类静态块，
 * 在 {@code BeanDefinitionRegistryPostProcessor} 阶段读取尚未就绪的 Bean 引发问题。
 * 4.0.6 之后改为 {@code ClassUtils.resolveClassName} 应避免这种副作用。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class BdfProcessorClassInitTest {

    @Test
    public void bdfProcessor_doesNotTriggerInterceptorClinit() {
        // Spring 已完成上下文启动 — 包括 BdfProcessor 阶段；
        // 此时 ClinitTrackingInterceptor 仅被 BdfProcessor 通过类名扫描过类型，
        // 不应被实例化（@Component 也是 prototype，无法主动 instantiate），
        // 因此其 static 块绝不应被执行。
        assertFalse("BdfProcessor 不应触发 interceptor 类的 <clinit>",
                ClinitTracker.INTERCEPTOR_INITIALIZED.get());
    }

    @Test
    public void interceptorClass_canStillBeInitialized_whenActuallyUsed() {
        // 反向验证：当真正主动使用该类（new 实例）时，<clinit> 才会触发
        new ClinitTrackingInterceptor();
        assertTrue("主动 new 之后应触发 <clinit>",
                ClinitTracker.INTERCEPTOR_INITIALIZED.get());
    }
}
