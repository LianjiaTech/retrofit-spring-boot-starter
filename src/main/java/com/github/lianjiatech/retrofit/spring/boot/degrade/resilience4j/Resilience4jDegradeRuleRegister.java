package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeRuleRegister;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.StopWatch;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resilience4j 熔断规则注册器
 * @author yukdawn@gmail.com 2022/4/5 23:15
 */
public class Resilience4jDegradeRuleRegister implements DegradeRuleRegister<CircuitBreakerConfig> {

    public static final Logger log = LoggerFactory.getLogger(Resilience4jDegradeRuleRegister.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jDegradeRuleRegister(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public void register(String resourceName, CircuitBreakerConfig rule) {
        circuitBreakerRegistry.circuitBreaker(resourceName, rule);
    }

    @Override
    public CircuitBreakerConfig newInstanceByDefault(Map<String, Object> attrMap){
        // 断路器配置
        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom()
                // 滑动窗口的类型为时间窗口
                .slidingWindowType(convertOrDefault(CircuitBreakerConfig.SlidingWindowType.class,
                        attrMap.get("slidingWindowType"), CircuitBreakerConfig.SlidingWindowType.TIME_BASED))
                // 时间窗口的大小为60秒
                .slidingWindowSize(convertOrDefault(Integer.class,
                        attrMap.get("slidingWindowSize"), Resilience4jDegrade.DEFAULT_SLIDING_WINDOW_SIZE))
                // 在单位时间窗口内最少需要10次调用才能开始进行统计计算
                .minimumNumberOfCalls(convertOrDefault(Integer.class,
                        attrMap.get("minimumNumberOfCalls"), Resilience4jDegrade.DEFAULT_MINIMUM_NUMBER_OF_CALLS))
                // 在单位时间窗口内调用失败率达到60%后会启动断路器
                .failureRateThreshold(convertOrDefault(Float.class,
                        attrMap.get("failureRateThreshold"), 60F))
                // 在半开状态下允许进行正常调用的次数
                .permittedNumberOfCallsInHalfOpenState(convertOrDefault(Integer.class,
                        attrMap.get("permittedNumberOfCallsInHalfOpenState"),
                        Resilience4jDegrade.DEFAULT_PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE))
                // 断路器打开状态转换为半开状态需要等待60秒
                .waitDurationInOpenState(Duration.ofSeconds(convertOrDefault(Integer.class,
                        attrMap.get("waitDurationInOpenState"),
                        Resilience4jDegrade.DEFAULT_WAIT_DURATION_IN_OPEN_STATE)));
        if (convertOrDefault(Boolean.class,
                attrMap.get("enableAutomaticTransitionFromOpenToHalfOpen"), Resilience4jDegrade.DEFAULT_ENABLE_AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN)){
            // 允许断路器自动由打开状态转换为半开状态
            builder.enableAutomaticTransitionFromOpenToHalfOpen();
        }
        return builder.build();
    }

    @Override
    public Response exec(String resourceName, DegradeProxyMethod<Response> func) throws IOException {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.find(resourceName).orElse(null);
        if (Objects.isNull(circuitBreaker)){
            // 断路器为空则直接调用返回
            return func.get();
        }
        log.debug("当前断路器状态:[{}]", circuitBreaker.getState());

        final StopWatch stopWatch = StopWatch.start();
        try {
            circuitBreaker.acquirePermission();
            final Response response = func.get();
            circuitBreaker.onResult(stopWatch.stop().toNanos(), TimeUnit.NANOSECONDS, response);
            return response;
        }catch (CallNotPermittedException e){
            throw new RetrofitBlockException(e);
        } catch (Exception exception) {
            circuitBreaker.onError(stopWatch.stop().toNanos(), TimeUnit.NANOSECONDS, exception);
            throw exception;
        }
    }
}
