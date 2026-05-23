package com.github.lianjiatech.retrofit.spring.boot.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 陈添明
 */
@Slf4j
public class ClassPathRetrofitClientScanner extends ClassPathBeanDefinitionScanner {

    private final ClassLoader classLoader;

    public ClassPathRetrofitClientScanner(BeanDefinitionRegistry registry, ClassLoader classLoader) {
        super(registry, false);
        this.classLoader = classLoader;
    }

    public void registerFilters() {
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(RetrofitClient.class);
        this.addIncludeFilter(annotationTypeFilter);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            log.warn("No RetrofitClient was found in '" + Arrays.toString(basePackages)
                    + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(
            AnnotatedBeanDefinition beanDefinition) {
        if (beanDefinition.getMetadata().isInterface()) {
            String className = beanDefinition.getMetadata().getClassName();
            try {
                Class<?> target = ClassUtils.forName(className, classLoader);
                return !target.isAnnotation();
            } catch (ClassNotFoundException ex) {
                // 通常是该接口的依赖（如 RxJava/Reactor 类）在当前运行时缺失：
                // 报告具体接口名 + 缺失的类，便于用户定位 — 否则用户只会得到下游
                // NoSuchBeanDefinitionException，离根因很远。
                log.error("Skip @RetrofitClient candidate '{}': missing dependency class '{}'. "
                        + "Add the corresponding dependency or remove the unused return type.",
                        className, ex.getMessage());
            } catch (LinkageError ex) {
                log.error("Skip @RetrofitClient candidate '{}': link error '{}'. "
                        + "Likely a transitive dependency conflict.", className, ex.getMessage());
            } catch (Exception ex) {
                log.error("Skip @RetrofitClient candidate '{}' due to unexpected error", className, ex);
            }
        }
        return false;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition)holder.getBeanDefinition();
            if (log.isDebugEnabled()) {
                log.debug("Creating RetrofitClientBean with name '" + holder.getBeanName()
                        + "' and '" + definition.getBeanClassName() + "' Interface");
            }
            definition.getConstructorArgumentValues()
                    .addGenericArgumentValue(Objects.requireNonNull(definition.getBeanClassName()));
            // beanClass全部设置为RetrofitFactoryBean
            definition.setBeanClass(RetrofitFactoryBean.class);
        }
    }
}
