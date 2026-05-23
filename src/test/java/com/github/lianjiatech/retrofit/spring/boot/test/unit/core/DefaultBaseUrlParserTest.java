package com.github.lianjiatech.retrofit.spring.boot.test.unit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.lianjiatech.retrofit.spring.boot.core.DefaultBaseUrlParser;
import com.github.lianjiatech.retrofit.spring.boot.core.RetrofitClient;
import org.junit.Test;
import org.springframework.core.env.Environment;

/**
 * 单元测试：覆盖 {@link DefaultBaseUrlParser} 的解析行为
 * <ul>
 *     <li>baseUrl 优先于 serviceId</li>
 *     <li>${propertyKey} 占位符解析</li>
 *     <li>baseUrl 自动追加结尾 '/'</li>
 *     <li>serviceId + path 拼接，多余 '/' 折叠</li>
 * </ul>
 */
public class DefaultBaseUrlParserTest {

    private final DefaultBaseUrlParser parser = new DefaultBaseUrlParser();

    private static Environment passthroughEnv() {
        Environment env = mock(Environment.class);
        when(env.resolvePlaceholders(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
        return env;
    }

    private static Environment envWithMapping(String key, String value) {
        Environment env = mock(Environment.class);
        when(env.resolvePlaceholders(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(inv -> {
                    String s = inv.getArgument(0);
                    return s.replace("${" + key + "}", value);
                });
        return env;
    }

    private static RetrofitClient annotation(String baseUrl, String serviceId, String path) {
        RetrofitClient stub = mock(RetrofitClient.class);
        when(stub.baseUrl()).thenReturn(baseUrl);
        when(stub.serviceId()).thenReturn(serviceId);
        when(stub.path()).thenReturn(path);
        return stub;
    }

    @Test
    public void baseUrl_isUsedPreferentially_andSlashAppended() {
        RetrofitClient ann = annotation("http://example.com/api", "should-ignore", "/foo");
        String result = parser.parse(ann, passthroughEnv());
        assertEquals("http://example.com/api/", result);
    }

    @Test
    public void baseUrl_alreadyEndsWithSlash_isUnchanged() {
        RetrofitClient ann = annotation("http://example.com/api/", "", "");
        assertEquals("http://example.com/api/", parser.parse(ann, passthroughEnv()));
    }

    @Test
    public void baseUrl_resolvesPlaceholder() {
        RetrofitClient ann = annotation("${test.host}/api", "", "");
        String result = parser.parse(ann, envWithMapping("test.host", "http://h.local"));
        assertEquals("http://h.local/api/", result);
    }

    @Test
    public void serviceIdAndPath_areJoinedWithHttpPrefix() {
        // 没有 baseUrl 时，使用 http:// + serviceId + path
        RetrofitClient ann = annotation("", "user-service", "/users");
        String result = parser.parse(ann, passthroughEnv());
        assertEquals("http://user-service/users/", result);
    }

    @Test
    public void serviceId_pathWithoutLeadingSlash_isHandled() {
        RetrofitClient ann = annotation("", "svc", "v1");
        String result = parser.parse(ann, passthroughEnv());
        // serviceId + '/' + 'v1' + '/' => svc/v1/
        assertEquals("http://svc/v1/", result);
    }

    @Test
    public void serviceId_collapsesRedundantSlashes() {
        RetrofitClient ann = annotation("", "svc/", "/users/");
        String result = parser.parse(ann, passthroughEnv());
        // 多 '/' 应被折叠成单个
        assertTrue("不应出现连续多个 '/' (除 http:// 外): " + result,
                !result.replaceFirst("http://", "").contains("//"));
        assertEquals("http://svc/users/", result);
    }

    @Test
    public void serviceId_resolvesPlaceholder() {
        RetrofitClient ann = annotation("", "${svc.id}", "/api");
        String result = parser.parse(ann, envWithMapping("svc.id", "real-svc"));
        assertEquals("http://real-svc/api/", result);
    }
}
