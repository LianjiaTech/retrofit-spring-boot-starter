package com.github.lianjiatech.retrofit.spring.boot.test.http;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.interceptor.Intercept;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Person;
import com.github.lianjiatech.retrofit.spring.boot.test.entity.Result;
import com.github.lianjiatech.retrofit.spring.boot.test.ex.TestErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.Sign;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.TimeStamp2Interceptor;
import com.github.lianjiatech.retrofit.spring.boot.test.interceptor.TimeStampInterceptor;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}", errorDecoder = TestErrorDecoder.class)
@Sign(accessKeyId = "${test.accessKeyId}", accessKeySecret = "${test.accessKeySecret}", exclude = {"/api/test/query"})
@Intercept(handler = TimeStampInterceptor.class)
@Intercept(handler = TimeStamp2Interceptor.class)
public interface HttpApi {

    /**
     * 基础类型(`String`/`Long`/`Integer`/`Boolean`/`Float`/`Double`)：直接将响应内容转换为上述基础类型。
     */
    @POST("getString")
    String getString(@Body Person person);

    /**
     * 其它任意POJO类型： 将响应体内容适配成一个对应的POJO类型对象返回，如果http状态码不是2xx，直接抛错！
     */
    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);

    /**
     * `CompletableFuture<T>` ：将响应体内容适配成CompletableFuture<T>对象返回，异步调用
     */
    @GET("person")
    CompletableFuture<Result<Person>> getPersonCompletableFuture(@Query("id") Long id);

    /**
     * `Void`: 不关注返回类型可以使用`Void`，如果http状态码不是2xx，直接抛错！
     */
    @POST("savePerson")
    Void savePersonVoid(@Body Person person);

    /**
     * `Response<T>`：将响应内容适配成Response<T>对象返回
     */
    @GET("person")
    Response<Result<Person>> getPersonResponse(@Query("id") Long id);

    /**
     * `Call<T>`：不执行适配处理，直接返回Call<T>对象
     */
    @GET("person")
    Call<Result<Person>> getPersonCall(@Query("id") Long id);


    /**
     * `Mono<T>` : project-reactor响应式返回类型
     */
    @GET("person")
    Mono<Result<Person>> monoPerson(@Query("id") Long id);

    /**
     * `Single<T>`：rxjava响应式返回类型（支持rxjava2/rxjava3）
     */
    @GET("person")
    Single<Result<Person>> singlePerson(@Query("id") Long id);

    /**
     * `Completable`：rxjava响应式返回类型，http请求没有响应体（支持rxjava2/rxjava3）
     */
    @GET("ping")
    Completable ping();

    @POST("savePerson")
    Result<Void> savePerson(@Body Person person);

    @POST("error")
    Person error(@Body Person person);

    @POST("savePersonList")
    Result<Void> savePersonList(@Body List<Person> personList);

    @POST("getBoolean")
    Boolean getBoolean(@Body Person person);
}
