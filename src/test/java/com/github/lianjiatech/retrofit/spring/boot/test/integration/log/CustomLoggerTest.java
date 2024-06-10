package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.lianjiatech.retrofit.spring.boot.log.GlobalLogProperty;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Logging#logger()} based on logback
 *
 * @author Hason
 */
@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class CustomLoggerTest extends MockWebServerTest {

    @Autowired
    private CustomLoggerUserService service;

    @Autowired
    private DefaultLoggerUserService defaultLoggerUserService;

    private TestAppender testAppender;

    @Before
    public void setup() {
        testAppender = new TestAppender();
    }

    @Test
    public void shouldEqualsDefaultLogger() {
        // given
        GlobalLogProperty property = new GlobalLogProperty();
        Logger logger = (Logger) LoggerFactory.getLogger(property.getLogger());
        logger.addAppender(testAppender);
        testAppender.start();

        mockServerReturnString(MIKE);

        // when
        defaultLoggerUserService.getName(Long100);

        // then 应当使用类的logger打印日志
        assertEquals(property.getLogger(), testAppender.lastEvent.getLoggerName());
    }

    @Test
    public void shouldEqualsClassLogger() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(CustomLoggerUserService.LOGGER);
        logger.addAppender(testAppender);
        testAppender.start();

        mockServerReturnString(MIKE);

        // when
        service.getName(Long100);

        // then 应当使用类的logger打印日志
        assertEquals(CustomLoggerUserService.LOGGER, testAppender.lastEvent.getLoggerName());
    }

    @Test
    public void shouldEqualsMethodLogger() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(CustomLoggerUserService.LOGGER);
        logger.addAppender(testAppender);
        testAppender.start();

        mockServerReturnObject(USER_MIKE);

        // when
        User user = service.getUser(Long100);

        // then 应当使用类的logger打印日志
        assertEquals(CustomLoggerUserService.LOGGER + ".getUser", testAppender.lastEvent.getLoggerName());
    }

    static class TestAppender extends AppenderBase<ILoggingEvent> {
        private ILoggingEvent lastEvent;

        @Override
        protected void append(ILoggingEvent event) {
            this.lastEvent = event;
        }
    }

}