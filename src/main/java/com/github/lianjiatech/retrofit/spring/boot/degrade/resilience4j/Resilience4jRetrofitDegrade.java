package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.AnnotatedElementUtils;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.StopWatch;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 * @since 2022/5/1 8:02 下午
 */
@Slf4j
public class Resilience4jRetrofitDegrade extends BaseRetrofitDegrade {

    protected final CircuitBreakerRegistry circuitBreakerRegistry;
    protected final GlobalResilience4jDegradeProperty globalResilience4jDegradeProperty;
    protected final CircuitBreakerConfigRegistry circuitBreakerConfigRegistry;

    public Resilience4jRetrofitDegrade(CircuitBreakerRegistry circuitBreakerRegistry,
            GlobalResilience4jDegradeProperty globalResilience4jDegradeProperty,
            CircuitBreakerConfigRegistry circuitBreakerConfigRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.globalResilience4jDegradeProperty = globalResilience4jDegradeProperty;
        this.circuitBreakerConfigRegistry = circuitBreakerConfigRegistry;
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
    public void loadDegradeRules(Class<?> retrofitInterface, String baseUrl) {
        for (Method method : retrofitInterface.getMethods()) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            Resilience4jDegrade resilience4jDegrade =
                    AnnotationExtendUtils.findMergedAnnotation(method, retrofitInterface, Resilience4jDegrade.class);
            if (!needDegrade(resilience4jDegrade)) {
                continue;
            }
            String circuitBreakerConfigName =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getCircuitBreakerConfigName()
                            : resilience4jDegrade.circuitBreakerConfigName();
            circuitBreakerRegistry.circuitBreaker(parseResourceName(method, baseUrl),
                    circuitBreakerConfigRegistry.get(circuitBreakerConfigName));
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
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        Class<?> service = invocation.service();
        String baseUrl = RetrofitFactoryBean.BASE_URL_MAP.get(service);
        if (baseUrl == null) {
            log.error("can't find find baseUrl, might hava a bug! service={}", service);
        }
        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.find(parseResourceName(invocation.method(), baseUrl)).orElse(null);
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
}
