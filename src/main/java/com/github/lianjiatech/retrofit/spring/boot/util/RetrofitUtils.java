package com.github.lianjiatech.retrofit.spring.boot.util;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author 陈添明
 */
public final class RetrofitUtils {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private RetrofitUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }


    /**
     * read ResponseBody as String
     *
     * @param response response
     * @return ResponseBody String
     * @throws IOException
     */
    public static String readResponseBody(Response response) throws IOException {
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

        if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
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
    }


    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }
}

