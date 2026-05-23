package com.github.lianjiatech.retrofit.spring.boot.test.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.lianjiatech.retrofit.spring.boot.util.AppContextUtils;
import org.junit.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 单元测试：4.0.6 commit 收窄了 {@code getBeanOrNew} 的异常捕获范围
 * （仅捕获 NoSuchBeanDefinitionException），其它异常应向上传递。
 */
public class AppContextUtilsTest {

    public static class WithDefaultCtor {
        public WithDefaultCtor() {}
    }

    public static class NoDefaultCtor {
        public NoDefaultCtor(String s) {}
    }

    public static class StaticCreate {
        private final String marker;
        private StaticCreate(String marker) { this.marker = marker; }
        public static StaticCreate create() { return new StaticCreate("from-create"); }
    }

    public static class ThrowsInCtor {
        public ThrowsInCtor() {
            throw new IllegalStateException("ctor boom");
        }
    }

    public static class ThrowsInCreate {
        // 仅提供有参构造器，确保 getDeclaredConstructor() 抛 NoSuchMethodException 后回退到 create()
        public ThrowsInCreate(String s) {}
        public static ThrowsInCreate create() {
            throw new IllegalArgumentException("create boom");
        }
    }

    @Test
    public void getBeanOrNew_returnsBeanFromContextIfPresent() {
        GenericApplicationContext ctx = new GenericApplicationContext();
        WithDefaultCtor existing = new WithDefaultCtor();
        ctx.registerBean("w", WithDefaultCtor.class, () -> existing);
        ctx.refresh();

        WithDefaultCtor result = AppContextUtils.getBeanOrNew(ctx, WithDefaultCtor.class);
        assertSame("应返回容器中的实例", existing, result);
    }

    @Test
    public void getBeanOrNew_fallsBackToReflection_whenBeanMissing() {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();

        WithDefaultCtor result = AppContextUtils.getBeanOrNew(ctx, WithDefaultCtor.class);
        assertNotNull("Bean 不存在时应反射创建实例", result);
    }

    @Test
    public void getBeanOrNew_fallsBackToStaticCreate_whenNoPublicNoArgCtor() {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();

        StaticCreate result = AppContextUtils.getBeanOrNew(ctx, StaticCreate.class);
        assertNotNull(result);
        assertEquals("from-create", result.marker);
    }

    @Test
    public void getBeanOrNew_throwsWhenNoCtorAndNoCreate() {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();
        try {
            AppContextUtils.getBeanOrNew(ctx, NoDefaultCtor.class);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate"));
        }
    }

    @Test
    public void getBeanOrNew_propagatesCtorException() {
        // 4.0.6 关键变更：构造异常不再被静默吞，应向上抛出
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();

        try {
            AppContextUtils.getBeanOrNew(ctx, ThrowsInCtor.class);
            fail("expected IllegalStateException with original cause");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("threw an exception"));
            assertNotNull(e.getCause());
            assertEquals("ctor boom", e.getCause().getMessage());
        }
    }

    @Test
    public void getBeanOrNew_propagatesStaticCreateException() {
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();

        try {
            AppContextUtils.getBeanOrNew(ctx, ThrowsInCreate.class);
            fail("expected IllegalStateException with original cause");
        } catch (IllegalStateException e) {
            assertNotNull(e.getCause());
            assertEquals("create boom", e.getCause().getMessage());
        }
    }

    @Test
    public void getBeanOrNew_rethrowsNoUniqueBeanDefinitionException() {
        // 4.0.6 关键变更：多个候选 Bean 应直接抛出，而不是静默退化为反射实例
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.registerBean("w1", WithDefaultCtor.class, WithDefaultCtor::new);
        ctx.registerBean("w2", WithDefaultCtor.class, WithDefaultCtor::new);
        ctx.refresh();

        try {
            AppContextUtils.getBeanOrNew(ctx, WithDefaultCtor.class);
            fail("expected NoUniqueBeanDefinitionException");
        } catch (NoUniqueBeanDefinitionException expected) {
            // ok
        }
    }
}
