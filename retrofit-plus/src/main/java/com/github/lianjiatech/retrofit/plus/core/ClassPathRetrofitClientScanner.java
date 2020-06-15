package com.github.lianjiatech.retrofit.plus.core;

import com.github.lianjiatech.retrofit.plus.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.plus.config.Config;
import com.github.lianjiatech.retrofit.plus.util.BeanExtendUtils;
import com.github.lianjiatech.retrofit.plus.util.SpringBootBindUtil;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author 陈添明
 */
public class ClassPathRetrofitClientScanner extends ClassPathBeanDefinitionScanner {

    private final ClassLoader classLoader;
    private String retrofitHelperRefBeanName;

    private RetrofitHelper retrofitHelper;

    private RetrofitFactoryBean<?> retrofitFactoryBean = new RetrofitFactoryBean<>();

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
            logger.warn("No RetrofitClient was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(
            AnnotatedBeanDefinition beanDefinition) {
        if (beanDefinition.getMetadata().isInterface()) {
            try {
                Class<?> target = ClassUtils.forName(
                        beanDefinition.getMetadata().getClassName(),
                        classLoader);
                return !target.isAnnotation();
            } catch (Exception ex) {
                logger.error("load class exception:", ex);
            }
        }
        return false;
    }


    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            if (logger.isDebugEnabled()) {
                logger.debug("Creating RetrofitClientBean with name '" + holder.getBeanName()
                        + "' and '" + definition.getBeanClassName() + "' Interface");
            }
            definition.getConstructorArgumentValues().addGenericArgumentValue(Objects.requireNonNull(definition.getBeanClassName()));
            // beanClass全部设置为RetrofitFactoryBean
            definition.setBeanClass(this.retrofitFactoryBean.getClass());
            if (StringUtils.hasText(this.retrofitHelperRefBeanName)) {
                // bean的方式配置
                definition.getPropertyValues().add("retrofitHelper", new RuntimeBeanReference(this.retrofitHelperRefBeanName));
            } else {
                // spring-boot配置
                definition.getPropertyValues().add("retrofitHelper", this.retrofitHelper);
            }
        }
    }


    public void setRetrofitHelperRefBeanName(String retrofitHelperRefBeanName) {
        this.retrofitHelperRefBeanName = retrofitHelperRefBeanName;
    }


    public void setRetrofitEnvironment(Environment environment) throws IllegalAccessException {
        // 配置属性
        Config config = SpringBootBindUtil.bind(environment, Config.class, Config.PREFIX);
        // 默认属性
        Config defaultConfig = new Config();
        // 合并属性
        Config mergeConfig = mergeConfig(config, defaultConfig);
        if (retrofitHelper == null) {
            retrofitHelper = new RetrofitHelper();
        }
        retrofitHelper.setConfig(mergeConfig);
    }


    private Config mergeConfig(Config config, Config defaultConfig) throws IllegalAccessException {
        if (config == null) {
            return defaultConfig;
        }
        if (defaultConfig == null) {
            return config;
        }
        // 属性合并，config属性优先级更高
        return BeanExtendUtils.combineProperties(config, defaultConfig);
    }

}
