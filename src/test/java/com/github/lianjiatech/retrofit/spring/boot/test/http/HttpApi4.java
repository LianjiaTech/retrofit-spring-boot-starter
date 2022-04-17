package com.github.lianjiatech.retrofit.spring.boot.test.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.degrade.Degrade;
import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", fallbackFactory = HttpApi4.HttpDegradeFallbackFactory.class)
@Degrade(count = 0.5F)
public interface HttpApi4 {

    /**
     * .
     *
     * @param id .
     * @return .
     */
    @GET("degrade/person")
    Result<Person> getPerson(@Query("id") Long id);

    @Service
    public class HttpDegradeFallbackFactory implements FallbackFactory<HttpApi4> {
        Logger log = LoggerFactory.getLogger(HttpDegradeFallbackFactory.class);
        /**
         * Returns an instance of the fallback appropriate for the given cause
         *
         * @param cause fallback cause
         * @return 实现了retrofit接口的实例。an instance that implements the retrofit interface.
         */
        @Override
        public HttpApi4 create(Throwable cause) {
            log.error("触发熔断了! ", cause.getMessage(), cause);
            return id -> {
                Result<Person> fallback = new Result<>();
                fallback.setCode(100)
                        .setMsg("fallback")
                        .setData(new Person());
                return fallback;
            };
        }
    }

}
