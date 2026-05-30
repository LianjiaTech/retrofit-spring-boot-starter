package com.github.lianjiatech.retrofit.spring.boot.test.integration.actuate;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.actuate.RetrofitEndpoint;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;

/**
 * 未通过 {@code management.endpoints.web.exposure.include} 暴露 retrofit endpoint 时，
 * {@code @ConditionalOnAvailableEndpoint} 应使 {@link RetrofitEndpoint} 不被装配。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class RetrofitEndpointDisabledTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void endpointNotRegisteredWhenNotExposed() {
        assertFalse("未暴露时不应装配 RetrofitEndpoint",
                applicationContext.getBeanNamesForType(RetrofitEndpoint.class).length > 0);
    }
}
