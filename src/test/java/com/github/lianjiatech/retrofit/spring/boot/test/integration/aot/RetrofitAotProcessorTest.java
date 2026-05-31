package com.github.lianjiatech.retrofit.spring.boot.test.integration.aot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.InMemoryGeneratedFiles;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.javapoet.ClassName;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitAotProcessor;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClientResolution;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;

import okhttp3.Interceptor;
import okhttp3.Response;
import retrofit2.http.GET;

/**
 * 验证 {@link RetrofitAotProcessor} 在 AOT 构建期为 {@code @RetrofitClient} 接口注册了 native image
 * 所需的反射 / JDK 动态代理 / 序列化 hints。纯 JVM 单元测试，无需真正构建 native image。
 *
 * @author 陈添明
 */
public class RetrofitAotProcessorTest {

    /**
     * 模拟 scanner 的产物：beanClass = RetrofitFactoryBean，构造参数为接口全限定名。
     */
    private DefaultListableBeanFactory beanFactoryWith(Class<?> retrofitInterface) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        AbstractBeanDefinition definition = BeanDefinitionBuilder
                .genericBeanDefinition(RetrofitFactoryBean.class)
                .addConstructorArgValue(retrofitInterface.getName())
                .getBeanDefinition();
        beanFactory.registerBeanDefinition(retrofitInterface.getSimpleName(), definition);
        return beanFactory;
    }

    private RuntimeHints process(Class<?> retrofitInterface) {
        DefaultListableBeanFactory beanFactory = beanFactoryWith(retrofitInterface);
        RetrofitAotProcessor processor = new RetrofitAotProcessor();
        BeanFactoryInitializationAotContribution contribution = processor.processAheadOfTime(beanFactory);
        assertTrue("应产生 AOT contribution", contribution != null);
        GenerationContext generationContext = new DefaultGenerationContext(
                new ClassNameGenerator(ClassName.get("com.example", "Test")), new InMemoryGeneratedFiles());
        contribution.applyTo(generationContext, null);
        return generationContext.getRuntimeHints();
    }

    @Test
    public void shouldResolveRetrofitInterfacesFromBeanDefinitions() {
        DefaultListableBeanFactory beanFactory = beanFactoryWith(PlainAotService.class);
        Set<Class<?>> interfaces = RetrofitAotProcessor.resolveRetrofitInterfaces(beanFactory);
        assertTrue("应从 BeanDefinition 解析出接口", interfaces.contains(PlainAotService.class));
    }

    @Test
    public void shouldRegisterJdkProxyForInterface() {
        RuntimeHints hints = process(PlainAotService.class);
        assertTrue("接口应注册 JDK 动态代理 hint",
                RuntimeHintsPredicates.proxies().forInterfaces(PlainAotService.class).test(hints));
    }

    @Test
    public void shouldRegisterReflectionForInterface() {
        RuntimeHints hints = process(PlainAotService.class);
        assertTrue("接口应注册反射 hint",
                RuntimeHintsPredicates.reflection().onType(PlainAotService.class).test(hints));
    }

    @Test
    public void shouldRegisterReflectionForAnnotationReferencedTypes() {
        RuntimeHints hints = process(DegradeAotService.class);
        assertTrue("fallbackFactory 应注册可反射构造 hint",
                RuntimeHintsPredicates.reflection()
                        .onType(AotFallbackFactory.class)
                        .withMemberCategory(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
                        .test(hints));
    }

    @Test
    public void shouldRegisterReflectionForInterceptorHandler() {
        RuntimeHints hints = process(InterceptAotService.class);
        assertTrue("拦截器 handler 应注册可反射构造 + 方法 hint",
                RuntimeHintsPredicates.reflection()
                        .onType(AotPathMatchInterceptor.class)
                        .withMemberCategory(MemberCategory.INVOKE_PUBLIC_METHODS)
                        .test(hints));
    }

    @Test
    public void shouldRegisterSerializationHintsForActuatorValueObjects() {
        RuntimeHints hints = process(PlainAotService.class);
        assertTrue("Actuator 值对象应注册反射序列化 hint",
                RuntimeHintsPredicates.reflection()
                        .onType(RetrofitClientResolution.class)
                        .withMemberCategory(MemberCategory.INVOKE_PUBLIC_METHODS)
                        .test(hints));
        assertTrue("Actuator 值对象嵌套类应注册反射序列化 hint",
                RuntimeHintsPredicates.reflection()
                        .onType(RetrofitClientResolution.Timeout.class)
                        .withMemberCategory(MemberCategory.INVOKE_PUBLIC_METHODS)
                        .test(hints));
    }

    @Test
    public void shouldIgnoreNonRetrofitBeanDefinitions() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("plain",
                BeanDefinitionBuilder.genericBeanDefinition(String.class).getBeanDefinition());
        Set<Class<?>> interfaces = RetrofitAotProcessor.resolveRetrofitInterfaces(beanFactory);
        assertFalse("非 RetrofitFactoryBean 不应被解析", interfaces.contains(String.class));
        assertTrue("无 RetrofitClient 时接口集合应为空", interfaces.isEmpty());
    }

    @RetrofitClient(baseUrl = "http://localhost")
    interface PlainAotService {
        @GET("/ping")
        String ping();
    }

    @RetrofitClient(baseUrl = "http://localhost", fallbackFactory = AotFallbackFactory.class)
    interface DegradeAotService {
        @GET("/ping")
        String ping();
    }

    @RetrofitClient(baseUrl = "http://localhost")
    @Intercept(handler = AotPathMatchInterceptor.class, include = "/**")
    interface InterceptAotService {
        @GET("/ping")
        String ping();
    }

    static class AotFallbackFactory implements FallbackFactory<DegradeAotService> {
        @Override
        public DegradeAotService create(Throwable cause) {
            return null;
        }
    }

    static class AotPathMatchInterceptor extends BasePathMatchInterceptor {
        @Override
        protected Response doIntercept(Interceptor.Chain chain) throws java.io.IOException {
            return chain.proceed(chain.request());
        }
    }
}
