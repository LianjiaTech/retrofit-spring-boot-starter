package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:15
 */
public class SentinelDegradeRuleRegister implements DegradeRuleRegister{

    @Override
    public void batchRegister(List<RetrofitDegradeRule> retrofitDegradeRuleList) {
        if (CollectionUtils.isEmpty(retrofitDegradeRuleList)){
            return;
        }
        DegradeRuleManager.loadRules(retrofitDegradeRuleList.stream().map(this::convert).collect(Collectors.toList()));
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

    private DegradeRule convert(RetrofitDegradeRule retrofitDegradeRule){
        // add degrade rule
        DegradeRule rule = defaultRuleNewInstance();
        if (Objects.nonNull(retrofitDegradeRule.getTimeWindow())){
            rule.setTimeWindow(retrofitDegradeRule.getTimeWindow());
        }
        rule.setCount(rule.getTimeWindow() *
                Optional.ofNullable(retrofitDegradeRule.getCount()).orElse(0.6F));
        rule.setResource(retrofitDegradeRule.getResourceName());
        return rule;
    }

    public DegradeRule defaultRuleNewInstance(){
        return new DegradeRule()
                // 使用异常数量策略，做个中转实现异常比例，异常比例策略在sentinel的实现为每秒检测，不是根据时间窗口来
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                // 异常比例
                .setCount(60*0.6)
                // 时间窗口
                .setTimeWindow(60);
    }
}
