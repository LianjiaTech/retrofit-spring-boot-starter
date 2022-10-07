package com.github.lianjiatech.retrofit.spring.boot.test.degrade;

import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Order(value =  Ordered.HIGHEST_PRECEDENCE)
public class DegradeSentinelApiFallback implements DegradeSentinelApi {
    @Override
    public Result<Person> getPerson1(Long id) {
        log.info("触发熔断了");
        Result<Person> fallback = new Result<>();
        fallback.setCode(-1)
                .setMsg("熔断Person1")
                .setData(new Person());
        return fallback;
    }

    @Override
    public Result<Person> getPerson2(Long id) {
        log.info("触发熔断了");
        Result<Person> fallback = new Result<>();
        fallback.setCode(-1)
                .setMsg("熔断Person2")
                .setData(new Person());
        return fallback;
    }
}