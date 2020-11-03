package com.github.lianjiatech.retrofit.spring.boot.degrade;

/**
 * @author 陈添明
 */
public interface FallbackFactory<T> {


    /**
     * Returns an instance of the fallback appropriate for the given cause
     *
     * @param cause fallback cause
     */
    T create(Throwable cause);
}
