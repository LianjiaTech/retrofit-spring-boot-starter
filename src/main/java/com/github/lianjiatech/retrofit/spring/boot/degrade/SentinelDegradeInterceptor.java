package com.github.lianjiatech.retrofit.spring.boot.degrade;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author 陈添明
 */
public class SentinelDegradeInterceptor extends BaseDegradeInterceptor {

    /**
     * 熔断拦截处理
     *
     * @param chain 请求执行链
     * @return 请求响应
     * @throws RetrofitBlockException 如果触发熔断，抛出RetrofitBlockException异常！
     */
    @Override
    protected Response degradeIntercept(String resourceName, Chain chain) throws RetrofitBlockException, IOException {
        Request request = chain.request();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return chain.proceed(request);
        } catch (BlockException e) {
            throw new RetrofitBlockException(e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
