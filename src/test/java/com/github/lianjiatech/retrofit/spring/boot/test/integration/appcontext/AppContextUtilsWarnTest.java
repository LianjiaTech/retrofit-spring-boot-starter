package com.github.lianjiatech.retrofit.spring.boot.test.integration.appcontext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;

import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * 集成测试：覆盖 H2 — {@link AppContextUtils#getBeanOrNew} 在反射回退路径上打 WARN，
 * 提示用户该实例脱离 Spring 管理（@Autowired / @PostConstruct 不生效）。
 * <p>
 * {@link ReflectiveOnlyErrorDecoder} 故意不加 @Component；首次请求会触发
 * {@code @RetrofitClient(errorDecoder=...)} 的解析，走到反射 fallback 分支。
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class AppContextUtilsWarnTest extends MockWebServerTest {

    @Autowired
    private ReflectiveDecoderUserService service;

    private TrackingAppender appender;
    private Logger boundLogger;

    @Before
    public void attachAppender() {
        boundLogger = (Logger)LoggerFactory.getLogger(AppContextUtils.class);
        appender = new TrackingAppender();
        appender.setContext(boundLogger.getLoggerContext());
        boundLogger.addAppender(appender);
        appender.start();
    }

    @After
    public void detachAppender() {
        if (boundLogger != null && appender != null) {
            boundLogger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    public void reflectiveFallback_emitsWarnWithReadableMessage() {
        mockServerReturnString(MIKE);

        // 第一次调用会解析并实例化 ErrorDecoder
        service.getName(Long100);

        // 触发反射创建后必须有 WARN，并提示 @Autowired 不会生效
        boolean foundWarn = false;
        for (ILoggingEvent ev : appender.events) {
            if (ev.getLevel() == Level.WARN) {
                String msg = ev.getFormattedMessage();
                if (msg.contains(ReflectiveOnlyErrorDecoder.class.getName())
                        && msg.contains("@Autowired")) {
                    foundWarn = true;
                    break;
                }
            }
        }
        assertTrue("应记录反射回退的 WARN 日志，提示 @Autowired 不生效", foundWarn);
    }

    static class TrackingAppender extends AppenderBase<ILoggingEvent> {
        final List<ILoggingEvent> events = Collections.synchronizedList(new ArrayList<ILoggingEvent>());

        @Override
        protected void append(ILoggingEvent eventObject) {
            events.add(eventObject);
        }
    }
}
