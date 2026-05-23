package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 集成测试：覆盖 4.0.6 改动的日志策略与 redactHeaders 行为。
 * <ul>
 *     <li>NONE：完全无日志输出</li>
 *     <li>BASIC：仅请求 / 响应行</li>
 *     <li>HEADERS：包含 headers，但不应输出 body</li>
 *     <li>BODY：包含 body</li>
 *     <li>全局 redactHeaders 真正遮蔽敏感请求头（application.yml 配置 Authorization）</li>
 *     <li>方法级 redactHeaders 与全局叠加</li>
 * </ul>
 *
 * @author 陈添明
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class LogStrategyTest extends MockWebServerTest {

    @Autowired
    private LogStrategyUserService service;

    private final List<TrackingAppender> attachedAppenders = new ArrayList<TrackingAppender>();

    @Before
    public void resetAppenders() {
        attachedAppenders.clear();
    }

    @After
    public void detachAppenders() {
        for (TrackingAppender app : attachedAppenders) {
            app.detach();
        }
    }

    private TrackingAppender attach(String loggerName) {
        TrackingAppender app = new TrackingAppender(loggerName);
        app.attach();
        attachedAppenders.add(app);
        return app;
    }

    /* ---------- LogStrategy 各档 ---------- */

    @Test
    public void none_emitsNothing() {
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_NONE);
        mockServerReturnObject(USER_MIKE);
        service.getUserNone(Long100);

        assertTrue("NONE 不应产生任何日志，实际: " + app.events.size(), app.events.isEmpty());
    }

    @Test
    public void basic_logsRequestAndResponseLines_butNoHeaders() {
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_BASIC);
        mockServerReturnObject(USER_MIKE);
        service.getUserBasic(Long100);

        String all = app.allMessages();
        assertTrue("应记录请求行: " + all, all.contains("--> GET"));
        assertTrue("应记录响应行: " + all, all.contains("<-- 200"));
        // BASIC 不输出 headers/body 详情；只判断没有 Content-Type 这类 header 行就够了
        assertFalse("BASIC 不应包含 Content-Type 头: " + all, all.contains("Content-Type:"));
    }

    @Test
    public void headers_logsHeaderLines() {
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_HEADERS);
        mockServerReturnObject(USER_MIKE);
        service.getUserHeaders(Long100, "trace-abc");

        String all = app.allMessages();
        assertTrue("HEADERS 应包含自定义请求头: " + all, all.contains("X-Custom-Trace"));
        assertTrue("HEADERS 应包含响应 Content-Type: " + all,
                all.contains("Content-Type:") || all.contains("content-type:"));
    }

    @Test
    public void body_logsResponseBody() {
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_BODY);
        mockServerReturnObject(USER_MIKE);
        service.getUserBody(Long100);

        String all = app.allMessages();
        // BODY 级别应能看到响应体内容 — User 序列化后含 "mike"
        assertTrue("BODY 应输出响应体内容: " + all, all.contains(MIKE));
    }

    /* ---------- redactHeaders ---------- */

    @Test
    public void globalRedactHeaders_masksAuthorization() {
        // application.yml 配置了 redact-headers: [Authorization]
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_REDACT);
        mockServerReturnString(MIKE);
        service.getNameWithGlobalRedact(Long100, "Bearer my-secret-token");

        String all = app.allMessages();
        assertTrue("应记录 Authorization 头名: " + all, all.contains("Authorization"));
        assertFalse("Authorization 值不应明文出现: " + all, all.contains("my-secret-token"));
    }

    @Test
    public void methodLevelRedactHeaders_masksXSecret_andStillMasksGlobal() {
        TrackingAppender app = attach(LogStrategyUserService.LOGGER_REDACT_METHOD);
        mockServerReturnString(MIKE);
        service.getNameWithMethodRedact(Long100, "Bearer global-secret", "method-secret-value");

        String all = app.allMessages();
        assertTrue("应记录 X-Secret 头名: " + all, all.contains("X-Secret"));
        assertFalse("X-Secret 值应被遮蔽: " + all, all.contains("method-secret-value"));
        assertFalse("Authorization 值仍应被全局规则遮蔽: " + all, all.contains("global-secret"));
    }

    /* ---------- helper ---------- */

    static class TrackingAppender extends AppenderBase<ILoggingEvent> {
        final List<ILoggingEvent> events = Collections.synchronizedList(new ArrayList<ILoggingEvent>());
        private final String loggerName;
        private Logger boundLogger;

        TrackingAppender(String loggerName) {
            this.loggerName = loggerName;
        }

        void attach() {
            boundLogger = (Logger) LoggerFactory.getLogger(loggerName);
            boundLogger.addAppender(this);
            this.start();
        }

        void detach() {
            if (boundLogger != null) {
                boundLogger.detachAppender(this);
            }
            this.stop();
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            events.add(eventObject);
        }

        String allMessages() {
            StringBuilder sb = new StringBuilder();
            for (ILoggingEvent e : events) {
                sb.append(e.getFormattedMessage()).append('\n');
            }
            return sb.toString();
        }
    }
}
