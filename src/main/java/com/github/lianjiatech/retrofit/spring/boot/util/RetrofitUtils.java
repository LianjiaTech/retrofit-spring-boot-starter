package com.github.lianjiatech.retrofit.spring.boot.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
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

    private static final String SUFFIX = "/";
    public static final String HTTP_PREFIX = "http://";

    /**
     * read ResponseBody as String
     *
     * @param response response
     * @return ResponseBody String
     * @throws IOException
     */
    public static String readResponseBody(Response response) throws ReadResponseBodyException {
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

            BufferedSource source = responseBody.source();
            // Buffer the entire body.
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();

            if (GZIP.equalsIgnoreCase(headers.get(CONTENT_ENCODING))) {
                try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                }
            }
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (contentLength != 0) {
                return buffer.clone().readString(charset);
            } else {
                return null;
            }
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

    public static String convertBaseUrl(RetrofitClient retrofitClient, String baseUrl, Environment environment) {
        if (StringUtils.hasText(baseUrl)) {
            baseUrl = environment.resolveRequiredPlaceholders(baseUrl);
            // 解析baseUrl占位符
            if (!baseUrl.endsWith(SUFFIX)) {
                baseUrl += SUFFIX;
            }
        } else {
            String serviceId = retrofitClient.serviceId();
            String path = retrofitClient.path();
            if (!path.endsWith(SUFFIX)) {
                path += SUFFIX;
            }
            baseUrl = HTTP_PREFIX + (serviceId + SUFFIX + path).replaceAll("/+", SUFFIX);
            baseUrl = environment.resolveRequiredPlaceholders(baseUrl);
        }
        return baseUrl;
    }
}
