package com.github.lianjiatech.retrofit.plus.core;

import com.github.lianjiatech.retrofit.plus.annotation.RetrofitScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author 陈添明
 */
public class RetrofitClientRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

    private final static Logger logger = LoggerFactory.getLogger(RetrofitClientRegistrar.class);

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(RetrofitScan.class.getName()));
        // 扫描指定路径下@RetrofitClient注解的接口，并注册到BeanDefinitionRegistry
        ClassPathRetrofitClientScanner scanner = new ClassPathRetrofitClientScanner(registry, classLoader);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        //指定扫描的基础包
        String[] basePackages = getPackagesToScan(attributes);
        //优先级 retrofitHelperRef > spring-boot
        String retrofitHelperRef = attributes.getString("retrofitHelperRef");
        if (StringUtils.hasText(retrofitHelperRef)) {
            // 基于javaBean配置
            scanner.setRetrofitHelperRefBeanName(retrofitHelperRef);
        } else {
            // spring-boot配置
            try {
                scanner.setRetrofitEnvironment(this.environment);
            } catch (Exception e) {
                logger.warn("只有 Spring Boot 环境中可以通过 Environment(配置文件,环境变量,运行参数等方式) 配置通用 retrofit，" +
                        "其他环境请通过 @RetrofitScan 注解中的 retrofitHelperRef进行配置!", e);
            }
        }
        scanner.registerFilters();
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

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
