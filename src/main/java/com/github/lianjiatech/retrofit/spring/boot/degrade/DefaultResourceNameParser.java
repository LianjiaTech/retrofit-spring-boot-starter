package com.github.lianjiatech.retrofit.spring.boot.degrade;

/**
 * @author 陈添明
 */
public class DefaultResourceNameParser extends BaseResourceNameParser {

    private static String PREFIX = "HTTP_OUT";

    /**
     * define resource name.
     *
     * @param baseUrl        baseUrl
     * @param httpMethodPath httpMethodPath
     * @return resource name.
     */
    @Override
    protected String defineResourceName(String baseUrl, HttpMethodPath httpMethodPath) {

        return String.format("%s:%s:%s", PREFIX, httpMethodPath.getMethod(), baseUrl + httpMethodPath.getPath());
    }
}
