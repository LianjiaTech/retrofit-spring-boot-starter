package com.github.lianjiatech.retrofit.spring.boot.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.AnnotatedElementUtils;

import lombok.experimental.UtilityClass;

/**
 * @author 陈添明
 * @since 2022/4/30 3:02 下午
 */
@UtilityClass
public class AnnotationExtendUtils {

    /**
     * 缓存注解查找结果，避免每次请求都进行反射扫描。
     * Key: (method, clazz, annotationType) 三元组；Value: Optional 包装的注解（Optional.empty() 表示不存在）。
     */
    private static final ConcurrentHashMap<AnnotationCacheKey, Optional<? extends Annotation>> ANNOTATION_CACHE =
            new ConcurrentHashMap<>(256);

    /**
     * 查找方法及其类上的指定注解，优先返回方法上的。结果会被缓存。
     *
     * @param <A> 注解泛型参数
     * @param method 方法
     * @param clazz 类型
     * @param annotationType 注解类型
     * @return 方法及其类上的指定注解。
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A findMergedAnnotation(Method method, Class<?> clazz,
            Class<A> annotationType) {
        AnnotationCacheKey key = new AnnotationCacheKey(method, clazz, annotationType);
        Optional<? extends Annotation> cached = ANNOTATION_CACHE.computeIfAbsent(key, k -> {
            A annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
            if (annotation != null) {
                return Optional.of(annotation);
            }
            return Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(clazz, annotationType));
        });
        return (A) cached.orElse(null);
    }

    /**
     * 判断某个类及其公有方法上是否存在指定注解。
     *
     * @param <A> 注解泛型参数
     * @param clazz 类
     * @param annotationType 注解类型
     * @return 判断某个类及其公有方法上是否存在指定注解。
     */
    public static <A extends Annotation> boolean isAnnotationPresentIncludeMethod(Class<?> clazz,
            Class<A> annotationType) {
        if (AnnotatedElementUtils.findMergedAnnotation(clazz, annotationType) != null) {
            return true;
        }
        for (Method method : clazz.getMethods()) {
            if (AnnotatedElementUtils.findMergedAnnotation(method, annotationType) != null) {
                return true;
            }
        }
        return false;
    }

    private static final class AnnotationCacheKey {
        private final Method method;
        private final Class<?> clazz;
        private final Class<? extends Annotation> annotationType;

        AnnotationCacheKey(Method method, Class<?> clazz, Class<? extends Annotation> annotationType) {
            this.method = method;
            this.clazz = clazz;
            this.annotationType = annotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationCacheKey)) {
                return false;
            }
            AnnotationCacheKey k = (AnnotationCacheKey) o;
            return Objects.equals(method, k.method)
                    && Objects.equals(clazz, k.clazz)
                    && Objects.equals(annotationType, k.annotationType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, clazz, annotationType);
        }
    }
}
