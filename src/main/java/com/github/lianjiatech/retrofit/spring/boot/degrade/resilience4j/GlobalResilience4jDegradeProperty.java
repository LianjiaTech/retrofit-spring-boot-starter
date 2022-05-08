package com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j;

import lombok.Data;

/**
 * 全局Resilience4j降级配置
 * @author 陈添明
 * @since 2022/5/8 10:46 上午
 */
@Data
public class GlobalResilience4jDegradeProperty {

    /**
     * 是否开启
     */
    private boolean enable = false;

    /**
     * 滑动窗口的类型
     */
    private SlidingWindowType slidingWindowType = SlidingWindowType.COUNT_BASED;

    /**
     * 窗口的大小
     */
    private int slidingWindowSize = 100;

    /**
     * 在单位窗口内最少需要几次调用才能开始进行统计计算
     */
    private int minimumNumberOfCalls = 100;

    /**
     * 单位时间窗口内调用失败率达到多少后会启动断路器
     */
    private float failureRateThreshold = 50;

    /**
     * 允许断路器自动由打开状态转换为半开状态
     */
    private boolean enableAutomaticTransitionFromOpenToHalfOpen = true;

    /**
     * 在半开状态下允许进行正常调用的次数
     */
    private int permittedNumberOfCallsInHalfOpenState = 10;

    /**
     * 断路器打开状态转换为半开状态需要等待秒数
     */
    private int waitDurationInOpenStateSeconds = 60;

    /**
     * 指定断路器应保持半开多长时间的等待持续时间，可选配置，大于1才是有效配置。
     */
    private int maxWaitDurationInHalfOpenStateSeconds = 0;

    /**
     * 忽略的异常类列表，只有配置值之后才会加载。
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] ignoreExceptions = new Class[0];

    /**
     * 记录的异常类列表，只有配置值之后才会加载。
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] recordExceptions = new Class[0];

    /**
     * 慢调用比例阈值
     */
    private float slowCallRateThreshold = 100;

    /**
     * 慢调用阈值秒数，超过该秒数视为慢调用
     */
    private int slowCallDurationThresholdSeconds = 60;

    /**
     * 启用可写堆栈跟踪的标志
     */
    private boolean writableStackTraceEnabled = true;
}
