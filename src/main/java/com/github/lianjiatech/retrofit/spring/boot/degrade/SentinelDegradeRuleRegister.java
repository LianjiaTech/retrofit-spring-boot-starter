package com.github.lianjiatech.retrofit.spring.boot.degrade;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.github.lianjiatech.retrofit.spring.boot.config.DegradeProperty;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author yukdawn@gmail.com 2022/4/5 23:15
 */
public class SentinelDegradeRuleRegister implements DegradeRuleRegister{

    private final static Logger logger = LoggerFactory.getLogger(SentinelDegradeRuleRegister.class);

    private final DegradeProperty degradeProperty;

    public SentinelDegradeRuleRegister(DegradeProperty degradeProperty) {
        this.degradeProperty = degradeProperty;
    }

    @Override
    public void register(RetrofitDegradeRule retrofitDegradeRule) {
        throw new RetrofitException("sentinel not supported simple register");
    }

    @Override
    public void batchRegister(List<RetrofitDegradeRule> retrofitDegradeRuleList) {
        if (CollectionUtils.isEmpty(retrofitDegradeRuleList)){
            return;
        }
        DegradeRuleManager.loadRules(retrofitDegradeRuleList.stream().map(this::convert).collect(Collectors.toList()));
    }

    @Override
    public <T> T exec(String resourceName, DegradeProxyMethod<T> func) throws IOException {
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
        DegradeStrategy degradeStrategy = retrofitDegradeRule.getDegradeStrategy();
        int grade;
        switch (degradeStrategy) {
            case AVERAGE_RT: {
                grade = 0;
                break;
            }
            case EXCEPTION_RATIO: {
                grade = 1;
                break;
            }
            default: {
                throw new IllegalArgumentException("Not currently supported! degradeStrategy=" + degradeStrategy);
            }
        }
        String resourceName = retrofitDegradeRule.getResourceName();
        // add degrade rule
        DegradeRule rule = new DegradeRule()
                .setGrade(grade)
                // Max allowed response time
                .setCount(retrofitDegradeRule.getCount())
                // Retry timeout (in second)
                .setTimeWindow(retrofitDegradeRule.getTimeWindow());
        rule.setResource(resourceName);
        return rule;
    }
}
