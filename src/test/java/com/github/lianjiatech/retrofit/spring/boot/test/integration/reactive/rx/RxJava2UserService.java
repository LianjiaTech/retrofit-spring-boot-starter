package com.github.lianjiatech.retrofit.spring.boot.test.integration.reactive.rx;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;
import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 * @since 2023/12/17 12:09 上午
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface RxJava2UserService {


    /**
     * 保存用户，返回Rx-Java流式对象Completable（http请求没有响应体返回该对象）
     */
    @POST("saveUserReturnCompletableForRx2")
    Completable saveUserReturnCompletableForRx2(@Body User user);

    /**
     * 根据id查询用户信息，返回Rx-Java流式对象Single
     */
    @GET("getUserReturnSingleForRx2")
    Single<User> getUserReturnSingleForRx2(@Query("id") Long id);
}
