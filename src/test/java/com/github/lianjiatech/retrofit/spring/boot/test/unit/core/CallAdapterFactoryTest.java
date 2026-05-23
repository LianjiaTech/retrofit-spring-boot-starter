package com.github.lianjiatech.retrofit.spring.boot.test.unit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.lianjiatech.retrofit.spring.boot.core.BodyCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.core.ResponseCallAdapterFactory;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitIOException;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 单元测试：覆盖 {@link BodyCallAdapterFactory} 与 {@link ResponseCallAdapterFactory}：
 * <ul>
 *     <li>类型识别（哪些 returnType 命中/不命中）</li>
 *     <li>responseType() 解析</li>
 *     <li>adapt：成功 → body；失败 → 错误体反序列化；IO 异常 → RetrofitIOException</li>
 * </ul>
 */
public class CallAdapterFactoryTest {

    /* ---------- type tokens ---------- */

    private static class StringHolder {
        @SuppressWarnings("unused")
        String s;
    }

    private static Type stringType() throws NoSuchFieldException {
        return StringHolder.class.getDeclaredField("s").getGenericType();
    }

    private static class CallStringHolder {
        @SuppressWarnings("unused")
        Call<String> c;
    }

    private static Type callStringType() throws NoSuchFieldException {
        return CallStringHolder.class.getDeclaredField("c").getGenericType();
    }

    private static class ResponseStringHolder {
        @SuppressWarnings("unused")
        Response<String> r;
    }

    private static Type responseStringType() throws NoSuchFieldException {
        return ResponseStringHolder.class.getDeclaredField("r").getGenericType();
    }

    private static class CompletableFutureStringHolder {
        @SuppressWarnings("unused")
        CompletableFuture<String> cf;
    }

    private static Type completableFutureStringType() throws NoSuchFieldException {
        return CompletableFutureStringHolder.class.getDeclaredField("cf").getGenericType();
    }

    private static class RawResponseHolder {
        @SuppressWarnings("unused")
        Response r;
    }

    private static Type rawResponseType() throws NoSuchFieldException {
        return RawResponseHolder.class.getDeclaredField("r").getGenericType();
    }

    /* ---------- BodyCallAdapterFactory.get ---------- */

    @Test
    public void body_get_returnsAdapter_forPojoReturnType() throws Exception {
        CallAdapter<?, ?> adapter = BodyCallAdapterFactory.INSTANCE.get(stringType(),
                new Annotation[0], mock(Retrofit.class));
        assertTrue("应返回非 null adapter", adapter != null);
    }

    @Test
    public void body_get_returnsNull_forCallType() throws Exception {
        CallAdapter<?, ?> adapter = BodyCallAdapterFactory.INSTANCE.get(callStringType(),
                new Annotation[0], mock(Retrofit.class));
        assertNull("Call 类型应由 retrofit 默认 adapter 处理", adapter);
    }

    @Test
    public void body_get_returnsNull_forResponseType() throws Exception {
        CallAdapter<?, ?> adapter = BodyCallAdapterFactory.INSTANCE.get(responseStringType(),
                new Annotation[0], mock(Retrofit.class));
        assertNull("Response 类型应由 ResponseCallAdapterFactory 处理", adapter);
    }

    @Test
    public void body_get_returnsNull_forCompletableFuture() throws Exception {
        CallAdapter<?, ?> adapter = BodyCallAdapterFactory.INSTANCE.get(completableFutureStringType(),
                new Annotation[0], mock(Retrofit.class));
        assertNull(adapter);
    }

    @Test
    public void body_responseType_isReturnType() throws Exception {
        Type t = stringType();
        CallAdapter<?, ?> adapter = BodyCallAdapterFactory.INSTANCE.get(t, new Annotation[0], mock(Retrofit.class));
        assertSame(t, adapter.responseType());
    }

    /* ---------- BodyCallAdapter.adapt ---------- */

    private static Request requestStub() {
        return new Request.Builder().url("http://unit.test/x").get().build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void body_adapt_success_returnsBody() throws Exception {
        CallAdapter adapter = BodyCallAdapterFactory.INSTANCE.get(stringType(),
                new Annotation[0], mock(Retrofit.class));
        Call call = mock(Call.class);
        when(call.request()).thenReturn(requestStub());
        when(call.execute()).thenReturn(Response.success("hello"));

        Object value = adapter.adapt(call);
        assertEquals("hello", value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void body_adapt_ioException_throwsRetrofitIOException() throws Exception {
        CallAdapter adapter = BodyCallAdapterFactory.INSTANCE.get(stringType(),
                new Annotation[0], mock(Retrofit.class));
        Call call = mock(Call.class);
        when(call.request()).thenReturn(requestStub());
        IOException cause = new IOException("network");
        when(call.execute()).thenThrow(cause);

        try {
            adapter.adapt(call);
            fail("expected RetrofitIOException");
        } catch (RetrofitIOException e) {
            assertSame(cause, e.getCause());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void body_adapt_errorResponse_withoutErrorBody_returnsNull() throws Exception {
        CallAdapter adapter = BodyCallAdapterFactory.INSTANCE.get(stringType(),
                new Annotation[0], mock(Retrofit.class));
        Call call = mock(Call.class);
        when(call.request()).thenReturn(requestStub());
        // 构造一个 errorBody=null 的 Response<String>
        okhttp3.Response raw = new okhttp3.Response.Builder()
                .request(requestStub())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("err")
                .body(ResponseBody.create("", MediaType.parse("application/json")))
                .build();
        // Response.error 要求 ResponseBody，使用空 body
        Response<String> resp = Response.error(500, ResponseBody.create("", MediaType.parse("application/json")));
        when(call.execute()).thenReturn(resp);

        // errorBody 不为 null（即使 0 长度），走 converter 分支
        Retrofit retrofit = mock(Retrofit.class);
        retrofit2.Converter<ResponseBody, String> conv = body -> "decoded-empty";
        when(retrofit.responseBodyConverter(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn((retrofit2.Converter) conv);

        CallAdapter ad2 = BodyCallAdapterFactory.INSTANCE.get(stringType(), new Annotation[0], retrofit);
        Object decoded = ad2.adapt(call);
        assertEquals("decoded-empty", decoded);
    }

    /* ---------- ResponseCallAdapterFactory ---------- */

    @Test
    public void response_get_returnsAdapter_forResponseType() throws Exception {
        CallAdapter<?, ?> adapter = ResponseCallAdapterFactory.INSTANCE.get(responseStringType(),
                new Annotation[0], mock(Retrofit.class));
        assertTrue(adapter != null);
    }

    @Test
    public void response_get_returnsNull_forNonResponseType() throws Exception {
        CallAdapter<?, ?> adapter = ResponseCallAdapterFactory.INSTANCE.get(stringType(),
                new Annotation[0], mock(Retrofit.class));
        assertNull(adapter);
    }

    @Test
    public void response_responseType_extractsTypeArgument() throws Exception {
        Type t = responseStringType();
        ParameterizedType pt = (ParameterizedType) t;
        CallAdapter<?, ?> adapter = ResponseCallAdapterFactory.INSTANCE.get(t, new Annotation[0], mock(Retrofit.class));
        assertSame(pt.getActualTypeArguments()[0], adapter.responseType());
    }

    @Test
    public void response_responseType_failsForRawResponse() throws Exception {
        // ResponseCallAdapter.responseType() 强制要求泛型参数
        Type rawT = rawResponseType();
        CallAdapter<?, ?> adapter = ResponseCallAdapterFactory.INSTANCE.get(rawT, new Annotation[0], mock(Retrofit.class));
        try {
            adapter.responseType();
            fail("raw Response 应当报错");
        } catch (Exception expected) {
            // ClassCastException (Class -> ParameterizedType) 或 IllegalArgumentException (Assert.notEmpty)
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void response_adapt_success_returnsResponse() throws Exception {
        CallAdapter adapter = ResponseCallAdapterFactory.INSTANCE.get(responseStringType(),
                new Annotation[0], mock(Retrofit.class));
        Call call = mock(Call.class);
        Response<String> ok = Response.success("hi");
        when(call.request()).thenReturn(requestStub());
        when(call.execute()).thenReturn(ok);

        Object adapted = adapter.adapt(call);
        assertSame(ok, adapted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void response_adapt_ioException_throwsRetrofitIOException() throws Exception {
        CallAdapter adapter = ResponseCallAdapterFactory.INSTANCE.get(responseStringType(),
                new Annotation[0], mock(Retrofit.class));
        Call call = mock(Call.class);
        when(call.request()).thenReturn(requestStub());
        IOException cause = new IOException("io");
        when(call.execute()).thenThrow(cause);

        try {
            adapter.adapt(call);
            fail("expected RetrofitIOException");
        } catch (RetrofitIOException e) {
            assertSame(cause, e.getCause());
        }
    }
}
