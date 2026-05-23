package com.github.lianjiatech.retrofit.spring.boot.test.integration.bdfprocessor;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;

import okhttp3.Response;

/**
 * 用于验证 {@code PathMatchInterceptorBdfProcessor} 在容器启动期间 <b>不会触发</b>
 * 该类的 {@code <clinit>}（因为 BdfProcessor 仅做类型匹配，应当走
 * {@code ClassUtils.resolveClassName}（initialize=false）而非
 * {@code Class.forName(name)}）。
 * <p>
 * 同时它声明为 {@code @Component}，让 BdfProcessor 真的会扫描到这个 BeanDefinition
 * 并尝试解析其 className。
 *
 * @author 陈添明
 */
@Component
public class ClinitTrackingInterceptor extends BasePathMatchInterceptor {

    static {
        // 仅当类被真正初始化时（active use）才会执行
        ClinitTracker.INTERCEPTOR_INITIALIZED.set(true);
    }

    @Override
    protected Response doIntercept(Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
