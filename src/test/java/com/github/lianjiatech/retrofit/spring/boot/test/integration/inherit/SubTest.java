package com.github.lianjiatech.retrofit.spring.boot.test.integration.inherit;

import com.github.lianjiatech.retrofit.spring.boot.test.integration.MockWebServerTest;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.RetrofitBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {RetrofitBootApplication.class})
@RunWith(SpringRunner.class)
public class SubTest extends MockWebServerTest {

    @Autowired
    private Sub sub;

    @Test
    public void getNameSub() {
        mockServerReturnString(MIKE);
        String name = sub.getNameSub(Long100);
        assertEquals(MIKE, name);
    }

    @Test
    public void getNameSuper() {
        mockServerReturnString(MIKE);
        String name = sub.getNameSuper(Long100);
        assertEquals(MIKE, name);
    }
}
