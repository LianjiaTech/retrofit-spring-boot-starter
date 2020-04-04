package com.github.lianjiatech.retrofit.spring.boot.test;

import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import com.github.lianjiatech.retrofit.spring.boot.test.http.HttpApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 陈添明
 */
@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class ApplicationTest {


    @Autowired
    private HttpApi httpApi;

    @Test
    public void test() {
        Result<Person> person = httpApi.getPerson(1L);
        Person data = person.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(1L, data.getId().longValue());
        Assert.assertEquals("test", data.getName());
        Assert.assertEquals(10, data.getAge().intValue());
    }

}
