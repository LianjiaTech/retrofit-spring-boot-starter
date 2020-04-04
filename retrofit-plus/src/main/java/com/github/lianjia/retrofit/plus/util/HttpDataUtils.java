package com.github.lianjia.retrofit.plus.util;

import lombok.experimental.UtilityClass;
import okhttp3.Headers;
import okio.Buffer;

import java.io.EOFException;

/**
 * @author 陈添明
 */
@UtilityClass
public class HttpDataUtils {


    private static final int INT_16 = 16;

    public static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < INT_16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            // Truncated UTF-8 sequence.
            return false;
        }
    }

    public static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !"identity".equalsIgnoreCase(contentEncoding)
                && !"gzip".equalsIgnoreCase(contentEncoding);
    }


    public static String headersString(Headers headers) {
        StringBuilder result = new StringBuilder("{");
        for (int i = 0, size = headers.size(); i < size; i++) {
            String name = headers.name(i);
            result.append(name).append(":").append(headers.value(i)).append("; ");
        }
        result.append("}");
        return result.toString();
    }
}
