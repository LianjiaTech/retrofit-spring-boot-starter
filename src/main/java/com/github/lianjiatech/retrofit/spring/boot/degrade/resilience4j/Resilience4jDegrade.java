package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_FAILURE_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_MINIMUM_NUMBER_OF_CALLS;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLIDING_WINDOW_SIZE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_DURATION_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_SLOW_CALL_RATE_THRESHOLD;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WAIT_DURATION_IN_OPEN_STATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yukdawn@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Resilience4jDegrade {

    /**
     * 滑动窗口的类型
     */
    SlidingWindowType slidingWindowType() default SlidingWindowType.COUNT_BASED;

    /**
     * 窗口的大小
     */
    int slidingWindowSize() default DEFAULT_SLIDING_WINDOW_SIZE;

    /**
     * 在单位窗口内最少需要几次调用才能开始进行统计计算
     */
    int minimumNumberOfCalls() default DEFAULT_MINIMUM_NUMBER_OF_CALLS;

    /**
     * 单位时间窗口内调用失败率达到多少后会启动断路器
     */
    float failureRateThreshold() default DEFAULT_FAILURE_RATE_THRESHOLD;

    /**
     * 允许断路器自动由打开状态转换为半开状态
     */
    boolean enableAutomaticTransitionFromOpenToHalfOpen() default true;

    /**
     * 在半开状态下允许进行正常调用的次数
     */
    int permittedNumberOfCallsInHalfOpenState() default DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN_STATE;

    /**
     * 断路器打开状态转换为半开状态需要等待秒数
     */
    int waitDurationInOpenStateSeconds() default DEFAULT_WAIT_DURATION_IN_OPEN_STATE;

    /**
     * 指定断路器应保持半开多长时间的等待持续时间，可选配置，大于1才是有效配置。
     */
    int maxWaitDurationInHalfOpenStateSeconds() default DEFAULT_WAIT_DURATION_IN_HALF_OPEN_STATE;

    /**
     * 忽略的异常类列表，只有配置值之后才会加载。
     */
    Class<? extends Throwable>[] ignoreExceptions() default {};

    /**
    * 记录的异常类列表，只有配置值之后才会加载。
    */
    Class<? extends Throwable>[] recordExceptions() default {};

    /**
     * 慢调用比例阈值
     */
    float slowCallRateThreshold() default DEFAULT_SLOW_CALL_RATE_THRESHOLD;

    /**
     * 慢调用阈值秒数，超过该秒数视为慢调用
     */
    int slowCallDurationThresholdSeconds() default DEFAULT_SLOW_CALL_DURATION_THRESHOLD;

    /**
     * 启用可写堆栈跟踪的标志
     */
    boolean writableStackTraceEnabled() default DEFAULT_WRITABLE_STACK_TRACE_ENABLED;
}