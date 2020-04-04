package com.github.lianjiatech.retrofit.plus.annotation;

import com.github.lianjiatech.retrofit.plus.core.RetrofitClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author 陈添明
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RetrofitClientRegistrar.class)
public @interface RetrofitScan {

    /**
     * <p>扫描包路径</p>
     * 与basePackages含义相同
     *
     * @return 扫描包路径
     */
    String[] value() default {};


    /**
     * <p>扫描包路径</p>
     * 与value含义相同
     *
     * @return 扫描包路径
     */
    String[] basePackages() default {};

    /**
     * 扫描的classes
     *
     * @return 扫描的classes
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * <p>
     * 配置一个retrofitHelper的Bean实例名称
     * </p>
     * 如果该配置存在，会使用spring容器中对应的retrofitHelper的Bean实例属性来初始化retrofit <br>
     * 如果该配置项为空，则使用上下文属性初始化retrofit
     *
     * @return retrofitHelper的Bean实例名称
     */
    String retrofitHelperRef() default "";
}
