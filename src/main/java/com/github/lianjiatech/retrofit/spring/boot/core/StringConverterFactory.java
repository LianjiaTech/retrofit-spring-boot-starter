package com.github.lianjiatech.retrofit.spring.boot.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author 陈添明
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringConverterFactory extends Converter.Factory {

    public static final StringConverterFactory INSTANCE = new StringConverterFactory();

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations, Retrofit retrofit) {
        return null;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (String.class.getTypeName().equals(type.getTypeName())) {
            return new StringResponseConverter();
        } else {
            return null;
        }
    }

    private static final class StringResponseConverter implements Converter<ResponseBody, String> {

        @Override
        public String convert(ResponseBody value) throws IOException {
            return value.string();
        }
    }
}
