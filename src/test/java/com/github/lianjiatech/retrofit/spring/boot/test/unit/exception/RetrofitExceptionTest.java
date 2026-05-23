package com.github.lianjiatech.retrofit.spring.boot.test.unit.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.SocketTimeoutException;

import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitException;
import com.github.lianjiatech.retrofit.spring.boot.exception.RetrofitIOException;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 单元测试：覆盖 4.0.6 commit 对 {@link RetrofitException} 的改动：
 * <ul>
 *     <li>{@code describe} 在 cause.getMessage() 为 null 时回退到类型名</li>
 *     <li>{@code errorStatus} 关闭响应、message 含 body</li>
 *     <li>{@code errorUnknown} 已是 RetrofitException 时直通</li>
 *     <li>{@code errorExecuting} 始终返回 {@link RetrofitIOException}</li>
 * </ul>
 */
public class RetrofitExceptionTest {

    private static Request request() {
        return new Request.Builder().url("http://unit.test/api").get().build();
    }

    private static Response response(int code, String body) {
        return new Response.Builder()
                .request(request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("X")
                .body(ResponseBody.create(body, MediaType.parse("text/plain")))
                .build();
    }

    /* ---------- errorStatus ---------- */

    @Test
    public void errorStatus_messageIncludesBody() {
        Response r = response(400, "bad-request-body");
        RetrofitException ex = RetrofitException.errorStatus(request(), r);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("invalid Response"));
        assertTrue(ex.getMessage().contains("bad-request-body"));
    }

    @Test
    public void errorStatus_emptyBody_doesNotAppendBodyTag() {
        Response r = response(404, "");
        RetrofitException ex = RetrofitException.errorStatus(request(), r);
        // 空 body 不应附加 ", body=" 段
        assertTrue("empty body should not produce ', body=': " + ex.getMessage(),
                !ex.getMessage().contains(", body="));
    }

    @Test
    public void errorStatus_closesResponse() {
        // 自定义 ResponseBody 跟踪 close()
        boolean[] closed = {false};
        ResponseBody trackingBody = new ResponseBody() {
            @Override public MediaType contentType() { return MediaType.parse("text/plain"); }
            @Override public long contentLength() { return 3; }
            @Override public okio.BufferedSource source() {
                return new okio.Buffer().writeUtf8("xyz");
            }
            @Override public void close() {
                closed[0] = true;
                super.close();
            }
        };
        Response r = new Response.Builder()
                .request(request())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("e")
                .body(trackingBody)
                .build();
        RetrofitException.errorStatus(request(), r);
        assertTrue("errorStatus 必须关闭响应，避免连接泄漏", closed[0]);
    }

    /* ---------- errorExecuting ---------- */

    @Test
    public void errorExecuting_returnsRetrofitIOException_withCause() {
        IOException cause = new SocketTimeoutException("timeout");
        RetrofitException ex = RetrofitException.errorExecuting(request(), cause);
        assertTrue(ex instanceof RetrofitIOException);
        assertSame(cause, ex.getCause());
        // 类型名 + message
        assertTrue(ex.getMessage().contains("SocketTimeoutException"));
        assertTrue(ex.getMessage().contains("timeout"));
        assertTrue(ex.getMessage().contains("request="));
    }

    @Test
    public void errorExecuting_nullMessage_keepsTypeName() {
        // 4.0.6 关键变更：cause.getMessage() 为 null 时退化为类型名
        IOException cause = new IOException();
        RetrofitException ex = RetrofitException.errorExecuting(request(), cause);
        assertTrue(ex.getMessage().contains("IOException"));
        assertSame(cause, ex.getCause());
    }

    /* ---------- errorUnknown ---------- */

    @Test
    public void errorUnknown_passesThroughRetrofitException() {
        RetrofitException original = new RetrofitException("already");
        RetrofitException ex = RetrofitException.errorUnknown(request(), original);
        assertSame("已是 RetrofitException 时不应二次包装", original, ex);
    }

    @Test
    public void errorUnknown_wrapsNonRetrofitException() {
        IllegalStateException cause = new IllegalStateException("oops");
        RetrofitException ex = RetrofitException.errorUnknown(request(), cause);
        assertEquals(RetrofitException.class, ex.getClass());
        assertSame(cause, ex.getCause());
        assertTrue(ex.getMessage().contains("IllegalStateException"));
        assertTrue(ex.getMessage().contains("oops"));
    }

    @Test
    public void errorUnknown_nullMessage_keepsTypeName() {
        IllegalStateException cause = new IllegalStateException();
        RetrofitException ex = RetrofitException.errorUnknown(request(), cause);
        assertTrue(ex.getMessage().contains("IllegalStateException"));
    }
}
