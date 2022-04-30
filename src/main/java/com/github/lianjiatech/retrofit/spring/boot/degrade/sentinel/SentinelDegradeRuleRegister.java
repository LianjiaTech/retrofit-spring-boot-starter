package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.degrade.DegradeRuleRegister;
import com.github.lianjiatech.retrofit.spring.boot.degrade.RetrofitBlockException;
import okhttp3.Response;

/**
 * Sentinel 熔断规则注册器
 * @author yukdawn@gmail.com 2022/4/5 23:15
 */
public class SentinelDegradeRuleRegister implements DegradeRuleRegister<DegradeRule> {

    @Override
    public void register(String resourceName, DegradeRule rule) {
        rule.setResource(resourceName);
        List<DegradeRule> rules = DegradeRuleManager.getRules();
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }
    @Override
    public DegradeRule newInstanceByDefault(Map<String, Object> attrMap) {
        return new DegradeRule()
                .setGrade(convertOrDefault(Integer.class, attrMap.get("grade"),
                        SentinelDegrade.DEFAULT_GRADE))
                .setCount(convertOrDefault(Double.class, attrMap.get("count"), SentinelDegrade.DEFAULT_COUNT))
                .setTimeWindow(convertOrDefault(Integer.class, attrMap.get("timeWindow"), SentinelDegrade.DEFAULT_TIME_WINDOW));
    }

    @Override
    public Response exec(String resourceName, DegradeProxyMethod<Response> func) throws IOException {
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.OUT);
            return func.get();
        } catch (BlockException e) {
            throw new RetrofitBlockException(e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
