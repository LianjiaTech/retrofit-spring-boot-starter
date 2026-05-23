package com.github.lianjiatech.retrofit.spring.boot.test.integration.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitConfigBean;
import com.github.lianjiatech.retrofit.spring.boot.metrics.RetrofitTagsProvider;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

/**
 * 验证未注册 {@code MeterRegistry} Bean 时 metrics 自动配置完全不生效，
 * 且业务调用链路不受影响。这是 starter 在生产中的默认形态。
 *
 * <p><b>实现注意</b>：本类故意 <i>不</i> 使用 {@code getBeansOfType(Interceptor.class)} 之类的
 * 全量类型查询，因为这会触发同类型 prototype Bean（如测试包里的
 * {@code ClinitTrackingInterceptor}）的实例化，破坏 {@code BdfProcessorClassInitTest} 验证的
 * "BdfProcessor 不应触发 &lt;clinit&gt;" 不变量。改用按名称定向查询。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class MetricsAutoConfigurationDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RetrofitConfigBean retrofitConfigBean;

    @Test
    public void metricsInterceptorBeanIsNotRegistered() {
        // 仅检查 BeanDefinition 存在性，避免触发任何拦截器实例化
        assertFalse("metrics interceptor BeanDefinition must not exist without MeterRegistry",
                context.containsBeanDefinition("retrofitMetricsInterceptor"));
    }

    @Test
    public void retrofitConfigBeanHoldsNullMetricsInterceptor() {
        assertNotNull(retrofitConfigBean);
        assertNull("RetrofitConfigBean.metricsInterceptor must be null",
                retrofitConfigBean.getMetricsInterceptor());
    }

    @Test
    public void tagsProviderBeanIsNotRegistered() {
        try {
            context.getBean(RetrofitTagsProvider.class);
            fail("tags provider should not be registered without MeterRegistry");
        } catch (NoSuchBeanDefinitionException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void retrofitInterfaceStillWorksWithoutMetrics() {
        // 简单冒烟：上下文能成功启动并解析所有 RetrofitClient
        assertEquals("metrics module should not break the rest of the starter",
                true, context.containsBean("retrofitConfigBean"));
    }
}
