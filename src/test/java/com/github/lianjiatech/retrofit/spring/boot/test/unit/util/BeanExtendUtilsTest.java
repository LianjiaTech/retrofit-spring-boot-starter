package com.github.lianjiatech.retrofit.spring.boot.test.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.lianjiatech.retrofit.spring.boot.util.BeanExtendUtils;
import lombok.Data;
import org.junit.Test;

/**
 * 单元测试：4.0.6 commit 将反射写失败由静默吞改为 warn 日志。
 * 这里验证：成功路径、字段不存在/无 setter 静默忽略、写入异常不抛。
 */
public class BeanExtendUtilsTest {

    @Data
    public static class Bean {
        private String name;
        private int age;
        private final String readonly = "ro";
    }

    @Test
    public void populate_setsPropertiesByName() {
        Bean bean = new Bean();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", "alice");
        props.put("age", 30);

        BeanExtendUtils.populate(bean, props);

        assertEquals("alice", bean.getName());
        assertEquals(30, bean.getAge());
    }

    @Test
    public void populate_ignoresUnknownProperty() {
        Bean bean = new Bean();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", "bob");
        props.put("noSuchField", "x");

        BeanExtendUtils.populate(bean, props);

        assertEquals("bob", bean.getName());
    }

    @Test
    public void populate_ignoresPropertyWithoutSetter() {
        Bean bean = new Bean();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("readonly", "should-not-apply");

        BeanExtendUtils.populate(bean, props);
        assertEquals("ro", bean.getReadonly());
    }

    @Test
    public void populate_swallowsTypeMismatch_doesNotThrow() {
        // 4.0.6 关键：类型不匹配（IllegalArgumentException）应该 warn 不应抛
        Bean bean = new Bean();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("age", "not-an-int");

        // 不应抛
        BeanExtendUtils.populate(bean, props);
        assertEquals(0, bean.getAge());
    }

    @Test
    public void setProperty_isNullSafe_onMissingField() {
        // 字段不存在直接静默忽略
        Bean bean = new Bean();
        BeanExtendUtils.setProperty(bean, "noField", "x");
        assertNull(bean.getName());
    }

    @Test
    public void populate_doesNothingWhenBeanOrPropertiesNull() {
        // 这两个分支都是 no-op，确保无 NPE
        BeanExtendUtils.populate(null, new LinkedHashMap<>());
        BeanExtendUtils.populate(new Bean(), null);
    }

    @Test
    public void populate_skipsNullKey() {
        Bean bean = new Bean();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(null, "ignored");
        props.put("name", "carol");

        BeanExtendUtils.populate(bean, props);
        assertEquals("carol", bean.getName());
    }
}
