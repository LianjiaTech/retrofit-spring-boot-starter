package com.github.lianjiatech.retrofit.spring.boot.test.unit.errordecoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitIOException;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 单元测试：覆盖 {@link ErrorDecoder.DefaultErrorDecoder} 的三个分支：
 * <ul>
 *     <li>{@code invalidRespDecode}：2xx 返回 null；非 2xx 抛 {@link RetrofitException}（含 body）并关闭响应</li>
 *     <li>{@code ioExceptionDecode}：包装为 {@link RetrofitIOException}</li>
 *     <li>{@code exceptionDecode}：包装为 {@link RetrofitException}；已有 RetrofitException 直通</li>
 * </ul>
 */
public class DefaultErrorDecoderTest {

    private final ErrorDecoder decoder = new ErrorDecoder.DefaultErrorDecoder();

    private static Request request() {
        return new Request.Builder().url("http://unit.test/api").get().build();
    }

    private static Response responseWithBody(Request req, int code, String body) {
        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(code < 400 ? "OK" : "Err")
                .body(ResponseBody.create(body == null ? "" : body, MediaType.parse("application/json")))
                .build();
    }

    @Test
    public void invalidRespDecode_2xx_returnsNull() {
        Request req = request();
        Response ok = responseWithBody(req, 200, "{}");
        assertNull(decoder.invalidRespDecode(req, ok));
    }

    @Test
    public void invalidRespDecode_4xx_throwsErrorStatus_withBody() {
        Request req = request();
        Response bad = responseWithBody(req, 400, "{\"err\":\"bad\"}");
        try {
            decoder.invalidRespDecode(req, bad);
        } catch (RetrofitException e) {
            assertTrue("异常 message 应包含响应体内容", e.getMessage().contains("bad"));
            assertTrue(e.getMessage().contains("invalid Response"));
            return;
        }
        // 默认实现应抛
        org.junit.Assert.fail("expected RetrofitException");
    }

    @Test
    public void invalidRespDecode_5xx_throwsErrorStatus() {
        Request req = request();
        Response bad = responseWithBody(req, 503, "service down");
        try {
            decoder.invalidRespDecode(req, bad);
        } catch (RetrofitException e) {
            assertTrue(e.getMessage().contains("service down"));
            return;
        }
        org.junit.Assert.fail("expected RetrofitException");
    }

    @Test
    public void invalidRespDecode_emptyBody_doesNotAppendBodySection() {
        Request req = request();
        Response bad = responseWithBody(req, 404, "");
        try {
            decoder.invalidRespDecode(req, bad);
        } catch (RetrofitException e) {
            // 当 body 为空时，message 不应出现 ", body=..."
            assertTrue("空 body 不应附加 body 描述", !e.getMessage().contains(", body="));
            return;
        }
        org.junit.Assert.fail("expected RetrofitException");
    }

    @Test
    public void ioExceptionDecode_wrapsAsRetrofitIOException() {
        Request req = request();
        IOException cause = new IOException("connect refused");
        RuntimeException result = decoder.ioExceptionDecode(req, cause);
        assertNotNull(result);
        assertTrue("应是 RetrofitIOException", result instanceof RetrofitIOException);
        assertSame(cause, result.getCause());
        assertTrue(result.getMessage().contains("connect refused"));
        assertTrue(result.getMessage().contains("IOException"));
    }

    @Test
    public void ioExceptionDecode_nullMessage_keepsTypeName() {
        Request req = request();
        IOException cause = new IOException(); // message=null
        RuntimeException result = decoder.ioExceptionDecode(req, cause);
        assertSame(cause, result.getCause());
        // describe(): message 为 null 时退化为类型名
        assertTrue(result.getMessage().contains("IOException"));
    }

    @Test
    public void exceptionDecode_wrapsGenericException() {
        Request req = request();
        IllegalStateException cause = new IllegalStateException("boom");
        RuntimeException result = decoder.exceptionDecode(req, cause);
        assertNotNull(result);
        assertEquals(RetrofitException.class, result.getClass());
        assertSame(cause, result.getCause());
        assertTrue(result.getMessage().contains("IllegalStateException"));
        assertTrue(result.getMessage().contains("boom"));
    }

    @Test
    public void exceptionDecode_existingRetrofitException_passesThrough() {
        Request req = request();
        RetrofitException original = new RetrofitException("already decoded");
        RuntimeException result = decoder.exceptionDecode(req, original);
        // 已是 RetrofitException 时直接返回，不做二次包装
        assertSame(original, result);
    }
}
