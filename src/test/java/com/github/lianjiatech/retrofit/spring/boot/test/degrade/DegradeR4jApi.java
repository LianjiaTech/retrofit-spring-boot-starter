package com.github.lianjiatech.retrofit.spring.boot.test.degrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import com.github.lianjiatech.retrofit.spring.boot.degrade.resilience4j.Resilience4jDegrade;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = DegradeR4jApi.HttpDegradeFallbackFactory.class)
@Resilience4jDegrade(slidingWindowType = CircuitBreakerConfig.SlidingWindowType.TIME_BASED,
        failureRateThreshold = 30, minimumNumberOfCalls = 10, permittedNumberOfCallsInHalfOpenState = 5)
public interface DegradeR4jApi {

    @GET("degrade/person1")
    Result<Person> getPerson1(@Query("id") Long id);

    @Resilience4jDegrade(slidingWindowType = CircuitBreakerConfig.SlidingWindowType.TIME_BASED,
            failureRateThreshold = 30, minimumNumberOfCalls = 10, permittedNumberOfCallsInHalfOpenState = 5)
    @GET("degrade/person2")
    Result<Person> getPerson2(@Query("id") Long id);

    @Service
    class HttpDegradeFallbackFactory implements FallbackFactory<DegradeR4jApi> {
        Logger log = LoggerFactory.getLogger(HttpDegradeFallbackFactory.class);

        /**
         * Returns an instance of the fallback appropriate for the given cause
         *
         * @param cause fallback cause
         * @return 实现了retrofit接口的实例。an instance that implements the retrofit interface.
         */
        @Override
        public DegradeR4jApi create(Throwable cause) {
            log.error("触发熔断了! ", cause.getMessage(), cause);
            return new DegradeR4jApi() {
                @Override
                public Result<Person> getPerson1(Long id) {
                    Result<Person> fallback = new Result<>();
                    fallback.setCode(-1)
                            .setMsg("熔断Person1")
                            .setData(new Person());
                    return fallback;
                }

                @Override
                public Result<Person> getPerson2(Long id) {
                    Result<Person> fallback = new Result<>();
                    fallback.setCode(-1)
                            .setMsg("熔断Person2")
                            .setData(new Person());
                    return fallback;
                }
            };
        }
    }

}