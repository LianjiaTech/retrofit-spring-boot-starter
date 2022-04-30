package com.github.lianjiatech.retrofit.spring.boot.core;


import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 * This will just scan the same base package as Spring Boot does. If you want more power, you can explicitly use
 * {@link com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan} but this will get typed mappers working correctly, out-of-the-box,
 * similar to using Spring Data JPA repositories.
 *
 * @author 陈添明
 */
@Slf4j
public class AutoConfiguredRetrofitScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware {

    private BeanFactory beanFactory;

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!AutoConfigurationPackages.has(this.beanFactory)) {
            log.debug("Could not determine auto-configuration package, automatic retrofit scanning disabled.");
            return;
        }

        log.debug("Searching for retrofits annotated with @RetrofitClient");

        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        if (log.isDebugEnabled()) {
            packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
        }

        // Scan the @RetrofitClient annotated interface under the specified path and register it to the BeanDefinitionRegistry
        ClassPathRetrofitClientScanner scanner = new ClassPathRetrofitClientScanner(registry, classLoader);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        String[] packageArr = packages.toArray(new String[0]);
        scanner.registerFilters();
        // Scan and register to BeanDefinition
        scanner.doScan(packageArr);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
