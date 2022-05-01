package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
public class SentinelRetrofitDegrade extends BaseRetrofitDegrade {

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        // 类或者方法上存在@SentinelDegrade -> 允许降级
        return AnnotationExtendUtils.isAnnotationPresentIncludeMethod(retrofitInterface, SentinelDegrade.class);
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface) {
        Method[] methods = retrofitInterface.getMethods();
        List<DegradeRule> rules = new ArrayList<>();
        for (Method method : methods) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            // 获取熔断配置
            SentinelDegrade sentinelDegrade =
                    AnnotationExtendUtils.findAnnotationIncludeClass(method, SentinelDegrade.class);
            if (sentinelDegrade == null) {
                continue;
            }
            DegradeRule degradeRule = new DegradeRule()
                    .setCount(sentinelDegrade.count())
                    .setTimeWindow(sentinelDegrade.timeWindow())
                    .setGrade(sentinelDegrade.grade());
            degradeRule.setResource(parseResourceName(method));
            rules.add(degradeRule);
        }
        DegradeRuleManager.loadRules(rules);
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Method method = Objects.requireNonNull(request.tag(Invocation.class)).method();
        if (AnnotationExtendUtils.findAnnotationIncludeClass(method, SentinelDegrade.class) == null) {
            return chain.proceed(request);
        }
        String resourceName = parseResourceName(method);
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
