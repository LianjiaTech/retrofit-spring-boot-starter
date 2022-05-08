package com.github.lianjiatech.retrofit.spring.boot.test.degrade;

import org.springframework.stereotype.Service;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel.SentinelDegrade;
import com.github.lianjiatech.retrofit.spring.boot.retry.Retry;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import lombok.extern.slf4j.Slf4j;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallback = DegradeSentinelApi.DegradeSentinelApiFallback.class)
@Retry(enable = false)
public interface DegradeSentinelApi {

    /**
     * @param id .
     * @return .
     */
    @GET("degrade/person1")
    Result<Person> getPerson1(@Query("id") Long id);

    @GET("degrade/person2")
    @SentinelDegrade(enable = false)
    Result<Person> getPerson2(@Query("id") Long id);

    @Service
    @Slf4j
    class DegradeSentinelApiFallback implements DegradeSentinelApi {
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
}