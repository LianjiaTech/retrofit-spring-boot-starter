package com.github.lianjiatech.retrofit.spring.boot.test.integration.log;

import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import com.github.lianjiatech.retrofit.spring.boot.log.LogStrategy;
import com.github.lianjiatech.retrofit.spring.boot.log.Logging;
import com.github.lianjiatech.retrofit.spring.boot.test.integration.entity.User;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 测试 LogStrategy 的四档输出量与 redactHeaders 的实际遮蔽行为。
 * <p>
 * 各方法绑定不同的 logger 名称，方便测试通过 {@code TestAppender} 收集到对应日志。
 *
 * @author 陈添明
 */
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface LogStrategyUserService {

    String LOGGER_NONE = "log.strategy.none";
    String LOGGER_BASIC = "log.strategy.basic";
    String LOGGER_HEADERS = "log.strategy.headers";
    String LOGGER_BODY = "log.strategy.body";
    String LOGGER_REDACT = "log.strategy.redact";
    String LOGGER_REDACT_METHOD = "log.strategy.redact.method";

    @GET("getUser")
    @Logging(logName = LOGGER_NONE, logStrategy = LogStrategy.NONE)
    User getUserNone(@Query("id") Long id);

    @GET("getUser")
    @Logging(logName = LOGGER_BASIC, logStrategy = LogStrategy.BASIC)
    User getUserBasic(@Query("id") Long id);

    @GET("getUser")
    @Logging(logName = LOGGER_HEADERS, logStrategy = LogStrategy.HEADERS)
    User getUserHeaders(@Query("id") Long id, @Header("X-Custom-Trace") String traceId);

    @GET("getUser")
    @Logging(logName = LOGGER_BODY, logStrategy = LogStrategy.BODY)
    User getUserBody(@Query("id") Long id);

    /** 全局 redactHeaders（在 application.yml 里配了 Authorization） */
    @POST("getName")
    @Logging(logName = LOGGER_REDACT, logStrategy = LogStrategy.HEADERS)
    String getNameWithGlobalRedact(@Query("id") Long id, @Header("Authorization") String auth);

    /** 方法级 redactHeaders 叠加全局 */
    @POST("getName")
    @Logging(logName = LOGGER_REDACT_METHOD, logStrategy = LogStrategy.HEADERS,
            redactHeaders = {"X-Secret"})
    String getNameWithMethodRedact(@Query("id") Long id,
            @Header("Authorization") String auth,
            @Header("X-Secret") String secret);
}
