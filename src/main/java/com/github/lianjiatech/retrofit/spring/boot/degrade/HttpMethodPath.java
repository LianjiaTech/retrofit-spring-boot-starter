package com.github.lianjiatech.retrofit.spring.boot.degrade;

import lombok.Data;

/**
 * @author 陈添明
 */
@Data
public class HttpMethodPath {
    /**
     * request method. such as GET, POST, PUT etc.
     */
    private final String method;

    /**
     * request path
     */
    private final String path;

    public HttpMethodPath(String method, String path) {
        this.method = method;
        this.path = path;
    }
}
