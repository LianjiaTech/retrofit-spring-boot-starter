package com.github.lianjia.retrofit.plus.core;


import com.github.lianjia.retrofit.plus.util.HttpDataUtils;
import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author 陈添明
 */
public class RequestHolder {

    private final Request request;

    public RequestHolder(Request request) {
        this.request = request;
    }


    /**
     * request基础信息
     *
     * @return request基础信息
     */
    public String basicString() {
        if (request == null) {
            return null;
        }
        return request.toString();
    }

    /**
     * request头信息
     *
     * @return request头信息
     */
    public String headersString() {
        if (request == null) {
            return null;
        }
        Headers headers = request.headers();
        return "requestHeader" + HttpDataUtils.headersString(headers);
    }

    /**
     * request请求体信息
     *
     * @return request请求体信息
     */
    @SneakyThrows
    public String bodyString() {
        if (request == null) {
            return null;
        }
        RequestBody requestBody = request.body();
        if (requestBody == null || HttpDataUtils.bodyHasUnknownEncoding(request.headers())) {
            return null;
        }
        StringBuilder result = new StringBuilder("requestBody");
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if (HttpDataUtils.isPlaintext(buffer)) {
            result.append(buffer.readString(Objects.requireNonNull(charset)))
                    .append("(")
                    .append(requestBody.contentLength())
                    .append("-byte body)");

        } else {
            result.append("(binary ")
                    .append(requestBody.contentLength())
                    .append("-byte body omitted)");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        String basicRequestString = basicString();
        if (StringUtils.hasText(basicRequestString)) {
            buffer.append(basicRequestString).append(", ");
        }
        String requestHeaderString = headersString();
        if (StringUtils.hasText(requestHeaderString)) {
            buffer.append(requestHeaderString).append(", ");
        }

        String requestBodyString = bodyString();
        if (StringUtils.hasText(requestBodyString)) {
            buffer.append(requestBodyString).append(", ");
        }
        return buffer.toString();
    }
}
