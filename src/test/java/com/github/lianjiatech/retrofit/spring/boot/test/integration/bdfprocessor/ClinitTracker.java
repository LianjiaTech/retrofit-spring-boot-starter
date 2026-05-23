package com.github.lianjiatech.retrofit.spring.boot.test.integration.bdfprocessor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单独的跟踪类。把"interceptor 是否被初始化"的状态外置，避免测试读取
 * {@link ClinitTrackingInterceptor} 自身的静态字段时触发其 {@code <clinit>}。
 *
 * @author 陈添明
 */
public final class ClinitTracker {

    public static final AtomicBoolean INTERCEPTOR_INITIALIZED = new AtomicBoolean(false);

    private ClinitTracker() {}
}
