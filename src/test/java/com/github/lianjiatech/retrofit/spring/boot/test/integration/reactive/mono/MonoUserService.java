package com.github.lianjiatech.retrofit.spring.boot.test.integration.reactive.mono;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import reactor.core.publisher.Mono;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:12 上午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface MonoUserService {

    /**
     * 根据id查询用户信息，返回Project-Reactor流式对象Mono
     */
    @GET("getUserReturnMono")
    Mono<User> getUserReturnMono(@Query("id") Long id);
}
