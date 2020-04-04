package com.github.lianjia.retrofit.plus.core;

import com.github.lianjia.retrofit.plus.util.HttpDataUtils;
import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author 陈添明
 */
public class ResponseHolder {

    public static final String GZIP = "gzip";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    private final Response response;

    private final ResponseBody responseBody;

    public ResponseHolder(Response response, ResponseBody responseBody) {
        this.response = response;
        this.responseBody = responseBody;
    }

    /**
     * response基础信息
     *
     * @return response基础信息
     */
    public String basicString(Long tookMs) {
        if (response == null) {
            return null;
        }
        StringBuilder result = new StringBuilder("response{protocol=")
                .append(response.protocol())
                .append(", code=")
                .append(response.code())
                .append(", message=")
                .append((response.message().isEmpty() ? " " : ' ' + response.message()));
        if (tookMs != null) {
            result.append(", time=").append(tookMs).append("ms");
        }
        result.append("}");
        return result.toString();
    }

    /**
     * response头信息
     *
     * @return response头信息
     */
    public String headersString() {
        if (response == null) {
            return null;
        }
        Headers headers = response.headers();
        return "responseHeader" + HttpDataUtils.headersString(headers);
    }

    /**
     * response请求体信息
     *
     * @return response请求体信息
     */
    @SneakyThrows
    public String bodyString() {
        if (response == null) {
            return null;
        }
        if (HttpDataUtils.bodyHasUnknownEncoding(response.headers())) {
            return null;
        }
        StringBuilder result = new StringBuilder("responseBody");
        Headers headers = response.headers();
        BufferedSource source = responseBody.source();
        long contentLength = responseBody.contentLength();
        // Buffer the entire body.
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();
        Long gzippedLength = null;
        if (GZIP.equalsIgnoreCase(headers.get(CONTENT_ENCODING))) {
            gzippedLength = buffer.size();
            GzipSource gzippedResponseBody = null;
            try {
                gzippedResponseBody = new GzipSource(buffer.clone());
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            } finally {
                if (gzippedResponseBody != null) {
                    gzippedResponseBody.close();
                }
            }
        }
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if (!HttpDataUtils.isPlaintext(buffer)) {
            result.append("(binary ").append(buffer.size()).append("-byte body omitted)");
        } else if (contentLength != 0) {
            result.append(buffer.clone().readString(Objects.requireNonNull(charset)));
        }

        if (gzippedLength != null) {
            result.append("(").append(buffer.size()).append("-byte, ").append(gzippedLength).append("-gzipped-byte body)");
        } else {
            result.append("(").append(buffer.size()).append("-byte body)");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        String basicResponseString = basicString(null);
        if (StringUtils.hasText(basicResponseString)) {
            buffer.append(basicResponseString).append(", ");
        }

        String responseHeaderString = headersString();
        if (StringUtils.hasText(responseHeaderString)) {
            buffer.append(responseHeaderString).append(", ");
        }

        String responseBodyString = bodyString();
        if (StringUtils.hasText(responseBodyString)) {
            buffer.append(responseBodyString).append(", ");
        }
        return buffer.toString();
    }
}
