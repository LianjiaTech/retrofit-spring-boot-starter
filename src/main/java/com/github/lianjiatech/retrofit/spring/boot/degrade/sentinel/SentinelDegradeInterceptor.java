package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeInterceptor;
import com.github.lianjiatech.retrofit.spring.boot.degrade.ResourceNameParser;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
public class SentinelDegradeInterceptor implements DegradeInterceptor {

    private final ResourceNameParser resourceNameParser;

    public SentinelDegradeInterceptor(ResourceNameParser resourceNameParser) {
        this.resourceNameParser = resourceNameParser;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        if (AnnotationExtendUtils.findAnnotation(method, SentinelDegrade.class) == null) {
            return chain.proceed(request);
        }
        String resourceName = resourceNameParser.extractResourceName(method);
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
