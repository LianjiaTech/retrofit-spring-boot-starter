package com.github.lianjiatech.retrofit.spring.boot.core;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.InterceptMark;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercepts;
import com.github.lianjiatech.retrofit.spring.boot.timeout.Timeout;

import lombok.extern.slf4j.Slf4j;

/**
 * GraalVM Native Image / Spring AOT 支持：在 AOT 构建期为每个 {@code @RetrofitClient} 接口自动注册
 * native image 所需的反射、JDK 动态代理与序列化 hints，使组件在 native 下无需用户手写 reflect-config.json。
 *
 * <p><b>仅在 AOT 构建期生效</b>：{@link BeanFactoryInitializationAotProcessor#processAheadOfTime} 只会被
 * {@code spring-boot:process-aot} / native 编译触发，普通 JVM 启动与 native 运行期都不会调用。该 processor
 * 本身无状态、无运行期逻辑，注册成空 bean 仅为让 Spring 在 AOT 阶段发现它，对运行期性能零影响。
 *
 * <p>关键前提：{@code RetrofitFactoryBean} 的 BeanDefinition 由 scanner（{@code ImportBeanDefinitionRegistrar}）
 * 在 AOT 处理前注册完毕，构造参数即接口全限定名。本 processor 据此枚举全部 client 接口并补齐 hints。
 *
 * @author 陈添明
 */
@Slf4j
public class RetrofitAotProcessor implements BeanFactoryInitializationAotProcessor {

    @Override
    public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        Set<Class<?>> retrofitInterfaces = resolveRetrofitInterfaces(beanFactory);
        return (generationContext, beanFactoryInitializationCode) -> {
            RuntimeHints hints = generationContext.getRuntimeHints();
            registerStaticHints(hints);
            for (Class<?> retrofitInterface : retrofitInterfaces) {
                registerInterfaceHints(hints, retrofitInterface);
            }
        };
    }

    /**
     * 枚举容器中所有 {@code RetrofitFactoryBean} 的 BeanDefinition，从其构造参数解析出被代理的接口类型。
     * <p>
     * scanner 在 {@code processBeanDefinitions} 中把接口全限定名作为唯一构造参数写入，这里据此还原。
     * 无法加载的类（依赖缺失等）跳过并打 WARN，与运行期 scanner 的容错策略一致。
     */
    public static Set<Class<?>> resolveRetrofitInterfaces(ConfigurableListableBeanFactory beanFactory) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (!RetrofitFactoryBean.class.getName().equals(definition.getBeanClassName())) {
                continue;
            }
            String interfaceName = extractInterfaceName(definition);
            if (interfaceName == null) {
                continue;
            }
            try {
                interfaces.add(ClassUtils.forName(interfaceName, classLoader));
            } catch (ClassNotFoundException | LinkageError ex) {
                log.warn("Skip AOT hints for @RetrofitClient '{}': cannot load class ({})",
                        interfaceName, ex.getMessage());
            }
        }
        return interfaces;
    }

    private static String extractInterfaceName(BeanDefinition definition) {
        ConstructorArgumentValues args = definition.getConstructorArgumentValues();
        ConstructorArgumentValues.ValueHolder holder = args.getArgumentValue(0, String.class);
        if (holder != null && holder.getValue() instanceof String) {
            return (String)holder.getValue();
        }
        return null;
    }

    /**
     * 为单个 client 接口注册：JDK 动态代理 hint（Retrofit.create / DegradeProxy 都基于接口生成代理）、
     * 接口自身的反射（方法签名、注解解析），以及 {@code @RetrofitClient} / {@code @InterceptMark} 注解
     * 上引用到的类（converterFactory / callAdapterFactory / errorDecoder / baseUrlParser / fallback /
     * fallbackFactory / 拦截器 handler）的反射构造与方法访问。
     */
    private void registerInterfaceHints(RuntimeHints hints, Class<?> retrofitInterface) {
        // Retrofit.create(interface) 与降级代理都用 Proxy.newProxyInstance(interface)
        hints.proxies().registerJdkProxy(retrofitInterface);
        ReflectionHints reflection = hints.reflection();
        // 接口方法 + 注解需在 native 下反射可见，供 Retrofit 解析方法签名与参数注解
        reflection.registerType(retrofitInterface,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS);

        RetrofitClient retrofitClient =
                AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, RetrofitClient.class);
        if (retrofitClient != null) {
            registerInstantiableTypes(reflection, retrofitClient.baseUrlParser());
            registerInstantiableTypes(reflection, retrofitClient.converterFactories());
            registerInstantiableTypes(reflection, retrofitClient.callAdapterFactories());
            registerInstantiableTypes(reflection, retrofitClient.errorDecoder());
            registerInstantiableTypes(reflection, retrofitClient.fallback());
            registerInstantiableTypes(reflection, retrofitClient.fallbackFactory());
        }
        // @Timeout 注解需在 native 下反射可见（类级和方法级）
        reflection.registerType(Timeout.class,
                MemberCategory.INVOKE_PUBLIC_METHODS);
        registerInterceptorHandlers(reflection, retrofitInterface);
    }

    /**
     * 扫描接口上被 {@code @InterceptMark} 标记的注解（含 {@code @Intercepts} 容器内的重复注解），
     * 把它们引用的 {@code handler} 拦截器类登记为可反射实例化 + 可访问 setter（{@code BeanExtendUtils.populate}
     * 通过 setter 注入属性）。
     */
    private void registerInterceptorHandlers(ReflectionHints reflection, Class<?> retrofitInterface) {
        Annotation[] classAnnotations = AnnotationUtils.getAnnotations(retrofitInterface);
        if (classAnnotations == null) {
            return;
        }
        List<Annotation> interceptAnnotations = new ArrayList<>();
        for (Annotation classAnnotation : classAnnotations) {
            Class<? extends Annotation> annotationType = classAnnotation.annotationType();
            if (annotationType.isAnnotationPresent(InterceptMark.class)) {
                interceptAnnotations.add(classAnnotation);
            }
            if (classAnnotation instanceof Intercepts) {
                Intercept[] value = ((Intercepts)classAnnotation).value();
                interceptAnnotations.addAll(java.util.Arrays.asList(value));
            }
        }
        for (Annotation interceptAnnotation : interceptAnnotations) {
            Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(interceptAnnotation);
            Object handler = attributes.get("handler");
            if (handler instanceof Class) {
                // 拦截器除反射构造外，还要通过 setter 注入属性（BeanExtendUtils.populate）
                reflection.registerType((Class<?>)handler,
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            }
        }
    }

    /**
     * 把通过 {@code AppContextUtils.getBeanOrNew} 反射创建的类型登记为：可反射调用公开构造器 + 公开静态
     * {@code create()} 方法（Retrofit 的 Converter.Factory 等静态工厂模式）。void.class 占位（fallback/
     * fallbackFactory 未配置时的默认值）跳过。
     */
    private void registerInstantiableTypes(ReflectionHints reflection, Class<?>... types) {
        for (Class<?> type : types) {
            if (type == null || void.class.isAssignableFrom(type)) {
                continue;
            }
            reflection.registerType(type,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
        }
    }

    /**
     * Actuator Endpoint 返回的值对象需被 Jackson 通过反射序列化（getter）。仅当类路径存在 actuator 时，
     * 这些类才会被实际使用，但无条件注册 hints 无副作用：未引入 actuator 时 hints 不会被消费。
     */
    private void registerStaticHints(RuntimeHints hints) {
        ReflectionHints reflection = hints.reflection();
        registerValueObject(reflection, RetrofitClientResolution.class);
        registerValueObject(reflection,
                com.github.lianjiatech.retrofit.spring.boot.actuate.RetrofitGlobalInfo.class);
    }

    private void registerValueObject(ReflectionHints reflection, Class<?> type) {
        reflection.registerType(type, MemberCategory.INVOKE_PUBLIC_METHODS);
        for (Class<?> nested : type.getDeclaredClasses()) {
            registerValueObject(reflection, nested);
        }
    }
}
