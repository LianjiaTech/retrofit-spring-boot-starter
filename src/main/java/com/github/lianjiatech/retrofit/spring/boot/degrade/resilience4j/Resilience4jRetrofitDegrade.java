package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.StopWatch;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 * @since 2022/5/1 8:02 下午
 */
public class Resilience4jRetrofitDegrade extends BaseRetrofitDegrade implements ApplicationContextAware {

    protected final CircuitBreakerRegistry circuitBreakerRegistry;
    protected final GlobalResilience4jDegradeProperty globalResilience4jDegradeProperty;
    protected ApplicationContext applicationContext;

    public Resilience4jRetrofitDegrade(CircuitBreakerRegistry circuitBreakerRegistry,
            GlobalResilience4jDegradeProperty globalResilience4jDegradeProperty) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.globalResilience4jDegradeProperty = globalResilience4jDegradeProperty;
    }

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        if (globalResilience4jDegradeProperty.isEnable()) {
            Resilience4jDegrade resilience4jDegrade =
                    AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, Resilience4jDegrade.class);
            if (resilience4jDegrade == null) {
                return true;
            }
            return resilience4jDegrade.enable();
        } else {
            return AnnotationExtendUtils.isAnnotationPresentIncludeMethod(retrofitInterface, Resilience4jDegrade.class);
        }
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface) {
        for (Method method : retrofitInterface.getMethods()) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            Resilience4jDegrade resilience4jDegrade =
                    AnnotationExtendUtils.findMergedAnnotation(method, method.getDeclaringClass(),
                            Resilience4jDegrade.class);
            if (!needDegrade(resilience4jDegrade)) {
                continue;
            }
            String circuitBreakerConfigBeanName =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getCircuitBreakerConfigBeanName()
                            : resilience4jDegrade.circuitBreakerConfigBeanName();
            circuitBreakerRegistry.circuitBreaker(parseResourceName(method),
                    applicationContext.getBean(circuitBreakerConfigBeanName, CircuitBreakerConfig.class));
        }
    }

    protected boolean needDegrade(Resilience4jDegrade resilience4jDegrade) {
        if (globalResilience4jDegradeProperty.isEnable()) {
            if (resilience4jDegrade == null) {
                return true;
            }
            return resilience4jDegrade.enable();
        } else {
            return resilience4jDegrade != null && resilience4jDegrade.enable();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.find(parseResourceName(method)).orElse(null);
        if (Objects.isNull(circuitBreaker)) {
            // 断路器为空则直接调用返回
            return chain.proceed(request);
        }
        StopWatch stopWatch = StopWatch.start();
        try {
            circuitBreaker.acquirePermission();
            Response response = chain.proceed(request);
            circuitBreaker.onResult(stopWatch.stop().toNanos(), TimeUnit.NANOSECONDS, response);
            return response;
        } catch (CallNotPermittedException e) {
            throw new RetrofitBlockException(e);
        } catch (Throwable throwable) {
            circuitBreaker.onError(stopWatch.stop().toNanos(), TimeUnit.NANOSECONDS, throwable);
            throw throwable;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
