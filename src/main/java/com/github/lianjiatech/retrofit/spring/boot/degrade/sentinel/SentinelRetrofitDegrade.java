package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.annotation.AnnotatedElementUtils;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author 陈添明
 */
public class SentinelRetrofitDegrade extends BaseRetrofitDegrade {

    protected final GlobalSentinelDegradeProperty globalSentinelDegradeProperty;

    public SentinelRetrofitDegrade(GlobalSentinelDegradeProperty globalSentinelDegradeProperty) {
        this.globalSentinelDegradeProperty = globalSentinelDegradeProperty;
    }

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        if (globalSentinelDegradeProperty.isEnable()) {
            SentinelDegrade sentinelDegrade =
                    AnnotatedElementUtils.findMergedAnnotation(retrofitInterface, SentinelDegrade.class);
            if (sentinelDegrade == null) {
                return true;
            }
            return sentinelDegrade.enable();
        } else {
            return AnnotationExtendUtils.isAnnotationPresentIncludeMethod(retrofitInterface, SentinelDegrade.class);
        }
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface) {
        Method[] methods = retrofitInterface.getMethods();
        for (Method method : methods) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            // 获取熔断配置
            SentinelDegrade sentinelDegrade =
                    AnnotationExtendUtils.findMergedAnnotation(method, retrofitInterface,
                            SentinelDegrade.class);

            if (!needDegrade(sentinelDegrade)) {
                continue;
            }
            DegradeRule degradeRule = new DegradeRule()
                    .setCount(sentinelDegrade == null ? globalSentinelDegradeProperty.getCount()
                            : sentinelDegrade.count())
                    .setTimeWindow(sentinelDegrade == null ? globalSentinelDegradeProperty.getTimeWindow()
                            : sentinelDegrade.timeWindow())
                    .setGrade(sentinelDegrade == null ? globalSentinelDegradeProperty.getGrade()
                            : sentinelDegrade.grade());
            String resourceName = parseResourceName(method, retrofitInterface);
            degradeRule.setResource(resourceName);
            DegradeRuleManager.setRulesForResource(resourceName, Collections.singleton(degradeRule));
        }
    }

    protected boolean needDegrade(SentinelDegrade sentinelDegrade) {
        if (globalSentinelDegradeProperty.isEnable()) {
            if (sentinelDegrade == null) {
                return true;
            }
            return sentinelDegrade.enable();
        } else {
            return sentinelDegrade != null && sentinelDegrade.enable();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation == null) {
            return chain.proceed(request);
        }
        Method method = invocation.method();
        Class<?> service = invocation.service();
        SentinelDegrade sentinelDegrade = AnnotationExtendUtils.findMergedAnnotation(method, service,
                SentinelDegrade.class);
        if (!needDegrade(sentinelDegrade)) {
            return chain.proceed(request);
        }
        String resourceName = parseResourceName(method, service);
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return chain.proceed(request);
        } catch (BlockException e) {
            throw new RetrofitBlockException(e);
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
