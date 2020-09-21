package com.github.lianjiatech.retrofit.spring.boot.interceptor;


/**
 * @author 陈添明
 */

public enum LogStrategy {

    /**
     * No logs.
     */
    NONE,

    /**
     * Logs request and response lines.
     */
    BASIC,

    /**
     * Logs request and response lines and their respective headers.
     */
    HEADERS,

    /**
     * Logs request and response lines and their respective headers and bodies (if present).
     */
    BODY
}