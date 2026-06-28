package com.github.lianjiatech.retrofit.spring.boot.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.lianjiatech.retrofit.spring.boot.exception.ReadResponseBodyException;

import lombok.experimental.UtilityClass;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * @author 陈添明
 */
@UtilityClass
public final class RetrofitUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final String GZIP = "gzip";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String IDENTITY = "identity";

    /**
     * 读取响应体的最大字节数，超出部分会被截断并附加省略标记。
     * <p>
     * 用于错误日志/异常构造，避免大响应体（特别是 binary 或失控的错误页）撑爆堆内存或污染日志。
     */
    public static final int MAX_BODY_BYTES = 8 * 1024;

    private static final String TRUNCATION_SUFFIX = "...[truncated]";

    /**
     * read ResponseBody as String，最多读取 {@link #MAX_BODY_BYTES} 字节，超出部分截断。
     *
     * @param response response
     * @return ResponseBody String
     * @throws ReadResponseBodyException ReadResponseBodyException
     */
    public static String readResponseBody(Response response) throws ReadResponseBodyException {
        return readResponseBody(response, MAX_BODY_BYTES);
    }

    /**
     * read ResponseBody as String，最多读取 {@code maxBytes} 字节。
     *
     * @param response response
     * @param maxBytes 最大读取字节数；&lt;=0 视为不限制（不推荐）。
     * @return ResponseBody String
     * @throws ReadResponseBodyException ReadResponseBodyException
     */
    public static String readResponseBody(Response response, int maxBytes) throws ReadResponseBodyException {
        try {
            Headers headers = response.headers();
            if (bodyHasUnknownEncoding(headers)) {
                return null;
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return null;
            }
            long contentLength = responseBody.contentLength();
            if (contentLength == 0) {
                return null;
            }

            BufferedSource source = responseBody.source();
            // 仅请求 maxBytes，避免把整个 body 拉入内存
            long requestBytes = maxBytes <= 0 ? Long.MAX_VALUE : maxBytes;
            source.request(requestBytes);
            Buffer buffer = source.getBuffer();
            boolean truncated = maxBytes > 0 && buffer.size() > maxBytes;

            if (GZIP.equalsIgnoreCase(headers.get(CONTENT_ENCODING))) {
                try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                    Buffer decoded = new Buffer();
                    if (maxBytes <= 0) {
                        decoded.writeAll(gzippedResponseBody);
                    } else {
                        // 仅解压最多 maxBytes+1 字节，用于检测是否 truncated
                        long limit = (long)maxBytes + 1;
                        long written = 0;
                        long n;
                        while (written < limit
                                && (n = gzippedResponseBody.read(decoded, limit - written)) != -1) {
                            written += n;
                        }
                        truncated = decoded.size() > maxBytes;
                    }
                    buffer = decoded;
                }
            }

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            long readSize = maxBytes <= 0 ? buffer.size() : Math.min(buffer.size(), maxBytes);
            String text = buffer.clone().readString(readSize, charset);
            return truncated ? text + TRUNCATION_SUFFIX : text;
        } catch (Exception e) {
            throw new ReadResponseBodyException(e);
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get(CONTENT_ENCODING);
        return contentEncoding != null
                && !IDENTITY.equalsIgnoreCase(contentEncoding)
                && !GZIP.equalsIgnoreCase(contentEncoding);
    }
}
