package com.github.lianjiatech.retrofit.spring.boot.core;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author 陈添明
 */
public class RetrofitClientScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware {

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(RetrofitScan.class.getName()));
        if (attributes == null) {
            return;
        }
        // Scan the @RetrofitClient annotated interface under the specified path and register it to the BeanDefinitionRegistry
        ClassPathRetrofitClientScanner scanner = new ClassPathRetrofitClientScanner(registry, classLoader);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        // Specify the base package for scanning
        String[] basePackages = getPackagesToScan(attributes);
        scanner.registerFilters();
        // Scan and register to BeanDefinition
        scanner.doScan(basePackages);
    }


    /**
     * 获取扫描的基础包路径
     *
     * @return 基础包路径
     */
    private String[] getPackagesToScan(AnnotationAttributes attributes) {
        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        if (!ObjectUtils.isEmpty(value)) {
            Assert.state(ObjectUtils.isEmpty(basePackages),
                    "@RetrofitScan basePackages and value attributes are mutually exclusive");
        }
        Set<String> packagesToScan = new LinkedHashSet<>();
        packagesToScan.addAll(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        return packagesToScan.toArray(new String[packagesToScan.size()]);
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
