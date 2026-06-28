package com.github.lianjiatech.retrofit.spring.boot.test.integration.appcontext;

import com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder;

/**
 * 故意不加 {@code @Component}，让其只能通过 {@code AppContextUtils.getBeanOrNew} 的反射 fallback 创建。
 *
 * @author 陈添明
 */
public class ReflectiveOnlyErrorDecoder implements ErrorDecoder {}
