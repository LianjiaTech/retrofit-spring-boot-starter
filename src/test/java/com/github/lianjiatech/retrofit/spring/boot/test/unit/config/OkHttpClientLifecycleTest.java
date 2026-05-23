package com.github.lianjiatech.retrofit.spring.boot.test.unit.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitAutoConfiguration;
import com.github.lianjiatech.retrofit.spring.boot.config.RetrofitProperties;
import org.junit.Test;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * 单元测试：覆盖 4.0.6 commit 的关键变更
 * <ul>
 *     <li>{@code retrofitBaseOkHttpClient} bean 按全局超时/连接池配置构建</li>
 *     <li>{@code OkHttpClientLifecycle.destroy()}：shutdown dispatcher、evict 连接池、关闭 cache</li>
 *     <li>destroy 单步异常不影响其它步骤（容错），全部步骤都尝试执行</li>
 *     <li>无 cache 时不抛 NPE</li>
 *     <li>{@link OkHttpClient#newBuilder()} 派生的子 client 与 base 共享 dispatcher / connectionPool —
 *         证明只关一次根 client 即可释放所有派生 client 的资源</li>
 * </ul>
 */
public class OkHttpClientLifecycleTest {

    /* ---------- retrofitBaseOkHttpClient ---------- */

    @Test
    public void baseOkHttpClient_appliesGlobalTimeoutAndPool() throws Exception {
        RetrofitProperties props = new RetrofitProperties();
        props.getGlobalTimeout().setConnectTimeoutMs(1100);
        props.getGlobalTimeout().setReadTimeoutMs(1200);
        props.getGlobalTimeout().setWriteTimeoutMs(1300);
        props.getGlobalTimeout().setCallTimeoutMs(1400);
        props.getGlobalConnectionPool().setMaxIdleConnections(7);
        props.getGlobalConnectionPool().setKeepAliveDurationMs(60_000L);

        RetrofitAutoConfiguration cfg = newAutoConfiguration(props);
        OkHttpClient client = cfg.retrofitBaseOkHttpClient();

        assertEquals(1100, client.connectTimeoutMillis());
        assertEquals(1200, client.readTimeoutMillis());
        assertEquals(1300, client.writeTimeoutMillis());
        assertEquals(1400, client.callTimeoutMillis());
        // ConnectionPool 没有暴露 max/keepAlive 的 getter，确保至少存在一个池实例即可
        assertNotNull(client.connectionPool());
    }

    @Test
    public void derivedClient_sharesDispatcherAndConnectionPool() throws Exception {
        RetrofitProperties props = new RetrofitProperties();
        OkHttpClient base = newAutoConfiguration(props).retrofitBaseOkHttpClient();

        OkHttpClient derived = base.newBuilder().build();

        // 关键：派生 client 应与 base 共享 dispatcher + connectionPool
        // 这样 destroy() 只需作用于 base 即可释放全部派生资源
        assertSame("dispatcher 应共享", base.dispatcher(), derived.dispatcher());
        assertSame("connectionPool 应共享", base.connectionPool(), derived.connectionPool());
    }

    /* ---------- OkHttpClientLifecycle.destroy ---------- */

    @Test
    public void destroy_shutdownsDispatcher_evictsPool_andClosesCacheWhenPresent() throws Exception {
        Dispatcher dispatcher = mock(Dispatcher.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(dispatcher.executorService()).thenReturn(executor);

        ConnectionPool pool = mock(ConnectionPool.class);
        Cache cache = mock(Cache.class);

        OkHttpClient client = mock(OkHttpClient.class);
        when(client.dispatcher()).thenReturn(dispatcher);
        when(client.connectionPool()).thenReturn(pool);
        when(client.cache()).thenReturn(cache);

        invokeDestroy(client);

        verify(executor, times(1)).shutdown();
        verify(pool, times(1)).evictAll();
        verify(cache, times(1)).close();
    }

    @Test
    public void destroy_isNullSafeWhenNoCache() throws Exception {
        Dispatcher dispatcher = mock(Dispatcher.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(dispatcher.executorService()).thenReturn(executor);
        ConnectionPool pool = mock(ConnectionPool.class);

        OkHttpClient client = mock(OkHttpClient.class);
        when(client.dispatcher()).thenReturn(dispatcher);
        when(client.connectionPool()).thenReturn(pool);
        when(client.cache()).thenReturn(null);

        invokeDestroy(client); // 不应 NPE

        verify(executor).shutdown();
        verify(pool).evictAll();
    }

    @Test
    public void destroy_continuesWhenDispatcherShutdownFails() throws Exception {
        Dispatcher dispatcher = mock(Dispatcher.class);
        ExecutorService executor = mock(ExecutorService.class);
        // 模拟 dispatcher 关闭异常
        doThrow(new RuntimeException("dispatcher boom")).when(executor).shutdown();
        when(dispatcher.executorService()).thenReturn(executor);

        ConnectionPool pool = mock(ConnectionPool.class);
        Cache cache = mock(Cache.class);

        OkHttpClient client = mock(OkHttpClient.class);
        when(client.dispatcher()).thenReturn(dispatcher);
        when(client.connectionPool()).thenReturn(pool);
        when(client.cache()).thenReturn(cache);

        invokeDestroy(client); // 不应抛出

        // 关键：dispatcher 失败不能阻止后续 evict + cache.close
        verify(pool, times(1)).evictAll();
        verify(cache, times(1)).close();
    }

    @Test
    public void destroy_continuesWhenPoolEvictFails() throws Exception {
        Dispatcher dispatcher = mock(Dispatcher.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(dispatcher.executorService()).thenReturn(executor);

        ConnectionPool pool = mock(ConnectionPool.class);
        doThrow(new RuntimeException("pool boom")).when(pool).evictAll();

        Cache cache = mock(Cache.class);

        OkHttpClient client = mock(OkHttpClient.class);
        when(client.dispatcher()).thenReturn(dispatcher);
        when(client.connectionPool()).thenReturn(pool);
        when(client.cache()).thenReturn(cache);

        invokeDestroy(client);

        verify(executor).shutdown();
        verify(cache, times(1)).close();
    }

    @Test
    public void destroy_swallowsCacheCloseIOException() throws Exception {
        Dispatcher dispatcher = mock(Dispatcher.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(dispatcher.executorService()).thenReturn(executor);
        ConnectionPool pool = mock(ConnectionPool.class);

        Cache cache = mock(Cache.class);
        doThrow(new IOException("cache boom")).when(cache).close();

        OkHttpClient client = mock(OkHttpClient.class);
        when(client.dispatcher()).thenReturn(dispatcher);
        when(client.connectionPool()).thenReturn(pool);
        when(client.cache()).thenReturn(cache);

        try {
            invokeDestroy(client);
        } catch (Throwable t) {
            fail("destroy 应吞掉 cache.close 的 IOException，实际抛出: " + t);
        }
    }

    /* ---------- 整体集成：真实 OkHttpClient 的 destroy ---------- */

    @Test
    public void realClient_destroy_evictsPool_dispatcherShutdown() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(2, 1, TimeUnit.MINUTES))
                .build();

        // 派生 client 共享资源
        OkHttpClient derived = client.newBuilder().build();
        assertSame(client.connectionPool(), derived.connectionPool());

        // 关闭前 dispatcher 未 shutdown
        assertTrue(!client.dispatcher().executorService().isShutdown());

        invokeDestroy(client);

        assertTrue("dispatcher executor 应已 shutdown", client.dispatcher().executorService().isShutdown());
        assertEquals(0, client.connectionPool().idleConnectionCount());
    }

    /* ---------- helpers ---------- */

    private static RetrofitAutoConfiguration newAutoConfiguration(RetrofitProperties props) throws Exception {
        Constructor<RetrofitAutoConfiguration> ctor =
                RetrofitAutoConfiguration.class.getDeclaredConstructor(RetrofitProperties.class);
        ctor.setAccessible(true);
        return ctor.newInstance(props);
    }

    /**
     * OkHttpClientLifecycle 是 RetrofitAutoConfiguration 的 package-private 静态内部类，
     * 这里用反射构造并调用 destroy()，避免改动主代码可见性。
     */
    private static void invokeDestroy(OkHttpClient client) throws Exception {
        Class<?> lifecycleCls = Class.forName(
                "com.github.lianjiatech.retrofit.spring.boot.config.RetrofitAutoConfiguration$OkHttpClientLifecycle");
        Constructor<?> ctor = lifecycleCls.getDeclaredConstructor(OkHttpClient.class);
        ctor.setAccessible(true);
        Object lifecycle = ctor.newInstance(client);
        // 反射 destroy()。Method.setAccessible 是必需的：内部类是 package-private，
        // 即使继承的 destroy() 是 public，也会受外部类可见性约束 (JDK 模块/反射访问检查)
        Method destroy = lifecycleCls.getMethod("destroy");
        destroy.setAccessible(true);
        try {
            destroy.invoke(lifecycle);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof Exception) {
                throw (Exception) target;
            }
            throw new RuntimeException(target);
        }
    }
}
