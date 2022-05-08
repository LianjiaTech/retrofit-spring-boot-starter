package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
public class Resilience4jRetrofitDegrade extends BaseRetrofitDegrade {

    protected final CircuitBreakerRegistry circuitBreakerRegistry;
    protected final GlobalResilience4jDegradeProperty globalResilience4jDegradeProperty;

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

            CircuitBreakerConfig.SlidingWindowType slidingWindowType =
                    resilience4jDegrade == null
                            ? CircuitBreakerConfig.SlidingWindowType
                                    .valueOf(globalResilience4jDegradeProperty.getSlidingWindowType().name())
                            : resilience4jDegrade.slidingWindowType();
            int slidingWindowSize =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getSlidingWindowSize()
                            : resilience4jDegrade.slidingWindowSize();

            int minimumNumberOfCalls =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getMinimumNumberOfCalls()
                            : resilience4jDegrade.minimumNumberOfCalls();

            float failureRateThreshold =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getFailureRateThreshold()
                            : resilience4jDegrade.failureRateThreshold();

            boolean enableAutomaticTransitionFromOpenToHalfOpen = resilience4jDegrade == null
                    ? globalResilience4jDegradeProperty.isEnableAutomaticTransitionFromOpenToHalfOpen()
                    : resilience4jDegrade.enableAutomaticTransitionFromOpenToHalfOpen();

            int permittedNumberOfCallsInHalfOpenState = resilience4jDegrade == null
                    ? globalResilience4jDegradeProperty.getPermittedNumberOfCallsInHalfOpenState()
                    : resilience4jDegrade.permittedNumberOfCallsInHalfOpenState();

            int waitDurationInOpenStateSeconds =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getWaitDurationInOpenStateSeconds()
                            : resilience4jDegrade.waitDurationInOpenStateSeconds();

            int maxWaitDurationInHalfOpenStateSeconds = resilience4jDegrade == null
                    ? globalResilience4jDegradeProperty.getMaxWaitDurationInHalfOpenStateSeconds()
                    : resilience4jDegrade.maxWaitDurationInHalfOpenStateSeconds();

            Class<? extends Throwable>[] ignoreExceptions =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getIgnoreExceptions()
                            : resilience4jDegrade.ignoreExceptions();

            Class<? extends Throwable>[] recordExceptions =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getRecordExceptions()
                            : resilience4jDegrade.recordExceptions();

            float slowCallRateThreshold =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.getSlowCallRateThreshold()
                            : resilience4jDegrade.slowCallRateThreshold();

            int slowCallDurationThresholdSeconds = resilience4jDegrade == null
                    ? globalResilience4jDegradeProperty.getSlowCallDurationThresholdSeconds()
                    : resilience4jDegrade.slowCallDurationThresholdSeconds();

            boolean writableStackTraceEnabled =
                    resilience4jDegrade == null ? globalResilience4jDegradeProperty.isWritableStackTraceEnabled()
                            : resilience4jDegrade.writableStackTraceEnabled();

            // 断路器配置
            CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom()
                    .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenStateSeconds))
                    .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                    .slidingWindowSize(slidingWindowSize)
                    .slidingWindowType(slidingWindowType)
                    .minimumNumberOfCalls(minimumNumberOfCalls)
                    .failureRateThreshold(failureRateThreshold)
                    .ignoreExceptions(ignoreExceptions)
                    .recordExceptions(recordExceptions)
                    .automaticTransitionFromOpenToHalfOpenEnabled(enableAutomaticTransitionFromOpenToHalfOpen)
                    .slowCallRateThreshold(slowCallRateThreshold)
                    .slowCallDurationThreshold(Duration.ofSeconds(slowCallDurationThresholdSeconds))
                    .writableStackTraceEnabled(writableStackTraceEnabled);

            if (maxWaitDurationInHalfOpenStateSeconds > 0) {
                builder.maxWaitDurationInHalfOpenState(
                        Duration.ofSeconds(maxWaitDurationInHalfOpenStateSeconds));
            }
            circuitBreakerRegistry.circuitBreaker(parseResourceName(method), builder.build());
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
}
