package com.github.lianjiatech.retrofit.spring.boot.degrade;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.env.Environment;
import retrofit2.Invocation;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author 陈添明
 */
public abstract class BaseDegradeInterceptor implements Interceptor {

    private Environment environment;

    private BaseResourceNameParser resourceNameParser;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setResourceNameParser(BaseResourceNameParser resourceNameParser) {
        this.resourceNameParser = resourceNameParser;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        assert invocation != null;
        Method method = invocation.method();
        String resourceName = resourceNameParser.parseResourceName(method, environment);
        return degradeIntercept(resourceName, chain);
    }


    /**
     * 熔断拦截处理
     *
     * @param resourceName 资源名称
     * @param chain 请求执行链
     * @return 请求响应
     * @throws RetrofitBlockException 如果触发熔断，抛出RetrofitBlockException异常！
     * @throws IOException IOException
     *
     */
    protected abstract Response degradeIntercept(String resourceName, Chain chain) throws RetrofitBlockException, IOException;
}
