package com.github.lianjiatech.retrofit.spring.boot.test.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import com.github.lianjiatech.retrofit.spring.boot.exception.ReadResponseBodyException;
import com.github.lianjiatech.retrofit.spring.boot.util.RetrofitUtils;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 单元测试：覆盖 4.0.6 新增的 8KB 上限截断逻辑（{@link RetrofitUtils#MAX_BODY_BYTES}）以及边界。
 */
public class RetrofitUtilsTest {

    private static Response newResponse(ResponseBody body, String contentEncoding) {
        Request req = new Request.Builder().url("http://unit.test/x").get().build();
        Response.Builder b = new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body);
        if (contentEncoding != null) {
            b.header("Content-Encoding", contentEncoding);
        }
        return b.build();
    }

    @Test
    public void smallBody_returnedAsIs() throws Exception {
        ResponseBody body = ResponseBody.create("hello", MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, null);
        String s = RetrofitUtils.readResponseBody(r);
        assertEquals("hello", s);
    }

    @Test
    public void nullBody_returnsNull() throws Exception {
        Response r = newResponse(null, null);
        assertNull(RetrofitUtils.readResponseBody(r));
    }

    @Test
    public void emptyBody_returnsNull() throws Exception {
        ResponseBody body = ResponseBody.create("", MediaType.parse("text/plain"));
        Response r = newResponse(body, null);
        assertNull(RetrofitUtils.readResponseBody(r));
    }

    @Test
    public void unknownContentEncoding_returnsNull() throws Exception {
        // 非 identity / gzip 的编码：保留字节不解析，返回 null
        ResponseBody body = ResponseBody.create("opaque", MediaType.parse("application/octet-stream"));
        Response r = newResponse(body, "br"); // brotli 不被支持
        assertNull(RetrofitUtils.readResponseBody(r));
    }

    @Test
    public void identityEncoding_isReadNormally() throws Exception {
        ResponseBody body = ResponseBody.create("plain", MediaType.parse("text/plain"));
        Response r = newResponse(body, "identity");
        assertEquals("plain", RetrofitUtils.readResponseBody(r));
    }

    @Test
    public void bodyExceeds8KB_isTruncatedWithSuffix() throws Exception {
        // 构造 12KB 的响应体，确认被截断到 8KB + 截断标记
        int size = 12 * 1024;
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append('a');
        }
        ResponseBody body = ResponseBody.create(sb.toString(), MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, null);

        String s = RetrofitUtils.readResponseBody(r);
        assertNotNull(s);
        assertTrue("应附加截断标记: " + s.substring(Math.max(0, s.length() - 30)),
                s.endsWith("...[truncated]"));
        // 截断点应在 8KB 处
        int payloadLen = s.length() - "...[truncated]".length();
        assertEquals(RetrofitUtils.MAX_BODY_BYTES, payloadLen);
    }

    @Test
    public void bodyExactly8KB_isNotTruncated() throws Exception {
        int size = RetrofitUtils.MAX_BODY_BYTES;
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append('b');
        }
        ResponseBody body = ResponseBody.create(sb.toString(), MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, null);

        String s = RetrofitUtils.readResponseBody(r);
        assertEquals(size, s.length());
        assertTrue(!s.endsWith("...[truncated]"));
    }

    @Test
    public void customMaxBytes_isHonored() throws Exception {
        ResponseBody body = ResponseBody.create("0123456789", MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, null);

        String s = RetrofitUtils.readResponseBody(r, 4);
        assertTrue("自定义 maxBytes 应被截断: " + s, s.endsWith("...[truncated]"));
        int payloadLen = s.length() - "...[truncated]".length();
        assertEquals(4, payloadLen);
    }

    @Test
    public void unlimitedMaxBytes_returnsFullBody() throws Exception {
        String big = "0123456789".repeat(2000); // 20KB
        ResponseBody body = ResponseBody.create(big, MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, null);

        // maxBytes <= 0 表示无限制
        String s = RetrofitUtils.readResponseBody(r, 0);
        assertEquals(big.length(), s.length());
        assertTrue(!s.endsWith("...[truncated]"));
    }

    @Test
    public void gzipEncoding_isDecodedAndTruncatedIfNeeded() throws Exception {
        // 准备一段大内容并 gzip 压缩，再断言解压 + 截断后含 "...[truncated]"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12 * 1024; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String original = sb.toString();
        byte[] gz;
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
             GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
            gzOut.write(original.getBytes(StandardCharsets.UTF_8));
            gzOut.close();
            gz = bos.toByteArray();
        }
        ResponseBody body = ResponseBody.create(gz, MediaType.parse("text/plain; charset=UTF-8"));
        Response r = newResponse(body, "gzip");

        String s = RetrofitUtils.readResponseBody(r);
        assertNotNull(s);
        assertTrue("gzip 解压后应被截断: " + s.substring(Math.max(0, s.length() - 30)),
                s.endsWith("...[truncated]"));
    }

    @Test
    public void readFailure_isWrappedAsReadResponseBodyException() {
        // 构造一个 ResponseBody，其 source() 在 read 时抛 IOException
        ResponseBody throwingBody = new ResponseBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("text/plain");
            }

            @Override
            public long contentLength() {
                return 10L;
            }

            @Override
            public BufferedSource source() {
                okio.Source throwingSource = new okio.Source() {
                    @Override
                    public long read(Buffer sink, long byteCount) throws IOException {
                        throw new IOException("boom");
                    }

                    @Override
                    public okio.Timeout timeout() {
                        return okio.Timeout.NONE;
                    }

                    @Override
                    public void close() {
                    }
                };
                return okio.Okio.buffer(throwingSource);
            }
        };
        Response r = newResponse(throwingBody, null);
        try {
            RetrofitUtils.readResponseBody(r);
            fail("expected ReadResponseBodyException");
        } catch (ReadResponseBodyException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IOException);
        }
    }
}
