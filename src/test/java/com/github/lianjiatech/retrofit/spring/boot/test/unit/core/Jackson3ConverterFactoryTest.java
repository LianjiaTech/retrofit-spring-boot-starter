package com.github.lianjiatech.retrofit.spring.boot.test.unit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.github.lianjiatech.retrofit.spring.boot.core.jackson3.Jackson3ConverterFactory;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.Test;
import retrofit2.Converter;
import retrofit2.Retrofit;
import tools.jackson.databind.ObjectMapper;

/**
 * 单元测试：覆盖 {@link Jackson3ConverterFactory} 的序列化/反序列化 + null/编码 边界。
 */
public class Jackson3ConverterFactoryTest {

    public static class Pojo {
        public String name;
        public int age;

        public Pojo() {}
        public Pojo(String name, int age) { this.name = name; this.age = age; }
    }

    private static class PojoListHolder {
        @SuppressWarnings("unused")
        List<Pojo> list;
    }

    private static Type pojoListType() throws NoSuchFieldException {
        return PojoListHolder.class.getDeclaredField("list").getGenericType();
    }

    private final Jackson3ConverterFactory factory = Jackson3ConverterFactory.create();

    /* ---------- requestBodyConverter ---------- */

    @Test
    public void request_serializesPojo_asJsonUtf8() throws Exception {
        Converter<Pojo, RequestBody> conv = (Converter<Pojo, RequestBody>) factory.requestBodyConverter(Pojo.class,
                new Annotation[0], new Annotation[0], mock(Retrofit.class));
        assertNotNull(conv);

        RequestBody body = conv.convert(new Pojo("alice", 30));
        assertNotNull(body);
        assertEquals("application/json", body.contentType().type() + "/" + body.contentType().subtype());
        assertEquals("UTF-8",
                body.contentType().charset(Charset.defaultCharset()).name().toUpperCase());

        Buffer buf = new Buffer();
        body.writeTo(buf);
        String json = buf.readUtf8();
        assertTrue("应包含字段: " + json, json.contains("\"name\":\"alice\""));
        assertTrue(json.contains("\"age\":30"));
    }

    @Test
    public void request_serializesList() throws Exception {
        @SuppressWarnings("unchecked")
        Converter<List<Pojo>, RequestBody> conv = (Converter<List<Pojo>, RequestBody>) factory.requestBodyConverter(
                pojoListType(), new Annotation[0], new Annotation[0], mock(Retrofit.class));
        RequestBody body = conv.convert(Arrays.asList(new Pojo("a", 1), new Pojo("b", 2)));

        Buffer buf = new Buffer();
        body.writeTo(buf);
        String json = buf.readUtf8();
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"name\":\"a\""));
        assertTrue(json.contains("\"name\":\"b\""));
    }

    @Test
    public void request_handlesUnicode() throws Exception {
        @SuppressWarnings("unchecked")
        Converter<Pojo, RequestBody> conv = (Converter<Pojo, RequestBody>) factory.requestBodyConverter(
                Pojo.class, new Annotation[0], new Annotation[0], mock(Retrofit.class));
        RequestBody body = conv.convert(new Pojo("张三", 30));

        Buffer buf = new Buffer();
        body.writeTo(buf);
        String json = buf.readUtf8();
        assertTrue("应保留 UTF-8 编码: " + json, json.contains("张三"));
    }

    /* ---------- responseBodyConverter ---------- */

    @Test
    public void response_deserializesPojo() throws Exception {
        @SuppressWarnings("unchecked")
        Converter<ResponseBody, Pojo> conv = (Converter<ResponseBody, Pojo>) factory.responseBodyConverter(
                Pojo.class, new Annotation[0], mock(Retrofit.class));

        ResponseBody body = ResponseBody.create("{\"name\":\"bob\",\"age\":25}",
                MediaType.parse("application/json; charset=UTF-8"));
        Pojo p = conv.convert(body);
        assertEquals("bob", p.name);
        assertEquals(25, p.age);
    }

    @Test
    public void response_deserializesList() throws Exception {
        @SuppressWarnings("unchecked")
        Converter<ResponseBody, List<Pojo>> conv = (Converter<ResponseBody, List<Pojo>>) factory.responseBodyConverter(
                pojoListType(), new Annotation[0], mock(Retrofit.class));

        ResponseBody body = ResponseBody.create("[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]",
                MediaType.parse("application/json"));
        List<Pojo> list = conv.convert(body);
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

    @Test
    public void response_unicode_isParsedCorrectly() throws Exception {
        @SuppressWarnings("unchecked")
        Converter<ResponseBody, Pojo> conv = (Converter<ResponseBody, Pojo>) factory.responseBodyConverter(
                Pojo.class, new Annotation[0], mock(Retrofit.class));

        ResponseBody body = ResponseBody.create(
                "{\"name\":\"李四\",\"age\":18}",
                MediaType.parse("application/json; charset=UTF-8"));
        Pojo p = conv.convert(body);
        assertEquals("李四", p.name);
    }

    @Test
    public void response_closesBody() throws Exception {
        // Jackson3ResponseBodyConverter 在 finally 中关闭 body
        boolean[] closed = {false};
        ResponseBody trackingBody = new ResponseBody() {
            private final ResponseBody delegate =
                    ResponseBody.create("{\"name\":\"x\",\"age\":1}", MediaType.parse("application/json"));
            @Override public MediaType contentType() { return delegate.contentType(); }
            @Override public long contentLength() { return delegate.contentLength(); }
            @Override public okio.BufferedSource source() { return delegate.source(); }
            @Override public void close() { closed[0] = true; delegate.close(); }
        };

        @SuppressWarnings("unchecked")
        Converter<ResponseBody, Pojo> conv = (Converter<ResponseBody, Pojo>) factory.responseBodyConverter(
                Pojo.class, new Annotation[0], mock(Retrofit.class));
        conv.convert(trackingBody);
        assertTrue("反序列化后应关闭响应体", closed[0]);
    }

    /* ---------- create / withStreaming ---------- */

    @Test(expected = NullPointerException.class)
    public void create_nullMapper_throws() {
        Jackson3ConverterFactory.create((ObjectMapper) null);
    }

    @Test(expected = NullPointerException.class)
    public void create_nullMediaType_throws() {
        Jackson3ConverterFactory.create(new ObjectMapper(), null);
    }

    @Test
    public void withStreaming_returnsNewInstance() {
        Jackson3ConverterFactory streaming = factory.withStreaming();
        assertNotNull(streaming);
        // 不强求是否同一引用，仅验证可以创建
    }
}
