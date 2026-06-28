package com.github.lianjiatech.retrofit.spring.boot.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.ClassUtils;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 陈添明
 */
@Slf4j
public class PathMatchInterceptorBdfProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassLoader classLoader = getClass().getClassLoader();
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            // 仅做类型匹配，不能触发目标类的 <clinit>：
            // 1) 此阶段是 BeanDefinitionRegistryPostProcessor，过早执行用户类的静态块
            // 可能读取尚未就绪的 Bean、放大 native-image 可达性图；
            // 2) ClassUtils.resolveClassName 内部使用 Class.forName(name, false, loader)，
            // 与 Class.forName(name) 不同，不会初始化目标类。
            try {
                Class<?> beanClass = ClassUtils.resolveClassName(beanClassName, classLoader);
                if (BasePathMatchInterceptor.class.isAssignableFrom(beanClass)) {
                    beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
                }
            } catch (IllegalArgumentException ex) {
                // 类不可解析（被混淆/移除等），保持原 scope，不影响其它 bean
                log.debug("Skip scope auto-set; class not resolvable: {}", beanClassName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
