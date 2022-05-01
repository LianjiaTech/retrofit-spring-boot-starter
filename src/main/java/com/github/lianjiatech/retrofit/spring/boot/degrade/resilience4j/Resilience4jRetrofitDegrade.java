package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
public class Resilience4jRetrofitDegrade extends BaseRetrofitDegrade {

    protected final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jRetrofitDegrade(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        return AnnotationExtendUtils.isAnnotationPresentIncludeMethod(retrofitInterface, Resilience4jDegrade.class);
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface) {
        for (Method method : retrofitInterface.getMethods()) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            Resilience4jDegrade resilience4jDegrade =
                    AnnotationExtendUtils.findAnnotationIncludeClass(method, Resilience4jDegrade.class);
            if (resilience4jDegrade == null) {
                continue;
            }
            // 断路器配置
            CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom()
                    .waitDurationInOpenState(Duration.ofSeconds(resilience4jDegrade.waitDurationInOpenStateSeconds()))
                    .permittedNumberOfCallsInHalfOpenState(resilience4jDegrade.permittedNumberOfCallsInHalfOpenState())
                    .slidingWindowSize(resilience4jDegrade.slidingWindowSize())
                    .slidingWindowType(resilience4jDegrade.slidingWindowType())
                    .minimumNumberOfCalls(resilience4jDegrade.minimumNumberOfCalls())
                    .failureRateThreshold(resilience4jDegrade.failureRateThreshold())
                    .ignoreExceptions(resilience4jDegrade.ignoreExceptions())
                    .recordExceptions(resilience4jDegrade.recordExceptions())
                    .automaticTransitionFromOpenToHalfOpenEnabled(
                            resilience4jDegrade.enableAutomaticTransitionFromOpenToHalfOpen())
                    .slowCallRateThreshold(resilience4jDegrade.slowCallRateThreshold())
                    .slowCallDurationThreshold(
                            Duration.ofSeconds(resilience4jDegrade.slowCallDurationThresholdSeconds()))
                    .writableStackTraceEnabled(resilience4jDegrade.writableStackTraceEnabled());

            if (resilience4jDegrade.maxWaitDurationInHalfOpenStateSeconds() > 0) {
                builder.maxWaitDurationInHalfOpenState(
                        Duration.ofSeconds(resilience4jDegrade.maxWaitDurationInHalfOpenStateSeconds()));
            }
            circuitBreakerRegistry.circuitBreaker(parseResourceName(method), builder.build());
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
}
