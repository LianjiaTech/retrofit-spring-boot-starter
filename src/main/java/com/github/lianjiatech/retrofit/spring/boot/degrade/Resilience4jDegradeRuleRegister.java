package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.StopWatch;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:15
 */
public class Resilience4jDegradeRuleRegister implements DegradeRuleRegister{

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jDegradeRuleRegister(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public void batchRegister(List<RetrofitDegradeRule> ruleList) {
        if (CollectionUtils.isEmpty(ruleList)){
            return;
        }
        for (RetrofitDegradeRule rule : ruleList) {
            circuitBreakerRegistry.circuitBreaker(rule.getResourceName(), this.convert(rule));
        }

    }

    @Override
    public Response exec(String resourceName, DegradeProxyMethod<Response> func) throws IOException {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(resourceName);
        System.out.println("当前断路器状态: "+ circuitBreaker.getState());

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

    private CircuitBreakerConfig convert(RetrofitDegradeRule rule){
        // add degrade rule
        CircuitBreakerConfig.Builder circuitBreakerBuilder = CircuitBreakerConfig.from(newInstanceByDefault());
        if (Objects.nonNull(rule.getCount())){
            circuitBreakerBuilder.failureRateThreshold(rule.getCount());
        }
        if (Objects.nonNull(rule.getTimeWindow())){
            circuitBreakerBuilder.slidingWindowSize(rule.getTimeWindow());
        }
        return circuitBreakerBuilder.build();
    }

    public CircuitBreakerConfig newInstanceByDefault(){
        // 断路器配置
        return CircuitBreakerConfig.custom()
                // 滑动窗口的类型为时间窗口
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                // 时间窗口的大小为60秒
                .slidingWindowSize(60)
                // 在单位时间窗口内最少需要10次调用才能开始进行统计计算
                .minimumNumberOfCalls(10)
                // 在单位时间窗口内调用失败率达到60%后会启动断路器
                .failureRateThreshold(60)
                // 允许断路器自动由打开状态转换为半开状态
                .enableAutomaticTransitionFromOpenToHalfOpen()
                // 在半开状态下允许进行正常调用的次数
                .permittedNumberOfCallsInHalfOpenState(5)
                // 断路器打开状态转换为半开状态需要等待60秒
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .build();
    }
}
