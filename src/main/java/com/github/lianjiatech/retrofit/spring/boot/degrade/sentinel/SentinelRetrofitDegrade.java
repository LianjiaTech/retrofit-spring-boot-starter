package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitFactoryBean;
import com.github.lianjiatech.retrofit.spring.boot.degrade.BaseRetrofitDegrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import com.github.lianjiatech.retrofit.spring.boot.util.AnnotationExtendUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * @author 陈添明
 */
@Slf4j
public class SentinelRetrofitDegrade extends BaseRetrofitDegrade {

    protected final GlobalSentinelDegradeProperty globalSentinelDegradeProperty;
    protected final Set<String> degradeResources = new HashSet<>();

    public SentinelRetrofitDegrade(GlobalSentinelDegradeProperty globalSentinelDegradeProperty) {
        this.globalSentinelDegradeProperty = globalSentinelDegradeProperty;
    }

    @Override
    public boolean isEnableDegrade(Class<?> retrofitInterface) {
        if (globalSentinelDegradeProperty.isEnable()) {
            return true;
        }
        return AnnotationExtendUtils.isAnnotationPresentIncludeMethod(retrofitInterface, SentinelDegrade.class);
    }

    @Override
    public void loadDegradeRules(Class<?> retrofitInterface, String baseUrl) {
        Method[] methods = retrofitInterface.getMethods();
        for (Method method : methods) {
            if (isDefaultOrStatic(method)) {
                continue;
            }
            // 获取熔断配置
            String resourceName = parseResourceName(method, baseUrl);
            Set<DegradeRule> degradeRuleSet = parseDegradeRules(retrofitInterface, method, resourceName);
            if (!CollectionUtils.isEmpty(degradeRuleSet)) {
                DegradeRuleManager.setRulesForResource(resourceName, degradeRuleSet);
                degradeResources.add(resourceName);
            }
        }
    }

    @NotNull
    private Set<DegradeRule> parseDegradeRules(Class<?> retrofitInterface, Method method, String resourceName) {
        Set<DegradeRule> degradeRuleSet = new HashSet<>();
        // 方法上有降级注解，以方法注解上的降级配置为准
        SentinelDegrade sentinelDegrade = AnnotationExtendUtils.findMergedAnnotation(method, retrofitInterface,
                SentinelDegrade.class);
        if (sentinelDegrade != null) {
            if (sentinelDegrade.enable()) {
                SentinelDegradeRule[] rules = sentinelDegrade.rules();
                for (SentinelDegradeRule rule : rules) {
                    DegradeRule degradeRule = new DegradeRule()
                            .setCount(rule.count())
                            .setTimeWindow(rule.timeWindow())
                            .setGrade(rule.grade())
                            .setMinRequestAmount(rule.minRequestAmount())
                            .setSlowRatioThreshold(rule.slowRatioThreshold())
                            .setStatIntervalMs(rule.statIntervalMs());
                    degradeRule.setResource(resourceName);
                    degradeRuleSet.add(degradeRule);
                }
            }
            return degradeRuleSet;
        }
        // 否则以全局降级配置为准
        if (globalSentinelDegradeProperty.isEnable()) {
            SentinelDegradeRuleProperty[] sentinelDegradeRules = globalSentinelDegradeProperty.getRules();
            for (SentinelDegradeRuleProperty sentinelDegradeRule : sentinelDegradeRules) {
                DegradeRule degradeRule = new DegradeRule()
                        .setCount(sentinelDegradeRule.getCount())
                        .setTimeWindow(sentinelDegradeRule.getTimeWindow())
                        .setGrade(sentinelDegradeRule.getGrade())
                        .setMinRequestAmount(sentinelDegradeRule.getMinRequestAmount())
                        .setSlowRatioThreshold(sentinelDegradeRule.getSlowRatioThreshold())
                        .setStatIntervalMs(sentinelDegradeRule.getStatIntervalMs());
                degradeRule.setResource(resourceName);
                degradeRuleSet.add(degradeRule);
            }
        }
        return degradeRuleSet;
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
        String baseUrl = RetrofitFactoryBean.BASE_URL_MAP.get(service);
        if (baseUrl == null) {
            log.error("can't find find baseUrl, might hava a bug! service={}", service);
        }
        String resourceName = parseResourceName(method, baseUrl);
        if (!degradeResources.contains(resourceName)) {
            // 非熔断降级资源
            return chain.proceed(request);
        }
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
