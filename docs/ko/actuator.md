# Actuator Endpoint (RetrofitClient 메타 정보 공개)
[English](../en/actuator.md) | [简体中文](../cn/actuator.md) | [繁體中文](../tw/actuator.md) | [日本語](../ja/actuator.md) | **한국어** | [Español](../es/actuator.md) | [Türkçe](../tr/actuator.md) | [Русский](../ru/actuator.md)

컴포넌트는 Spring Boot Actuator 기반의 읽기 전용 Endpoint를 제공하여, `/actuator/retrofit`에서 애플리케션 내 모든 `@RetrofitClient` 인터페이스의 완전한 설정 메타 정보를 공개합니다. "특정 인터페이스에서 실제로 유효한 baseUrl / 타임아웃 / 로그 / 재시도 / 서킷브레이커 설정이 무엇인지"를 조사하기 쉽게 합니다.

> **옵션 의존성, 필요시 활성화**: 사용자가 actuator를 도입한 경우만 Endpoint가 구축됩니다(`@ConditionalOnClass`). actuator를 도입하지 않은 SpringBoot 3 프로젝트에는 영향이 없으며, 정상적으로 시작합니다. Endpoint의 공개와 온/오프는 Spring Boot의 표준 management 설정(`@ConditionalOnAvailableEndpoint`)에 완전히 위임되며, 자체 스위치를 생성하지 않습니다.

## 활성화 방법

1. actuator 도입:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2. retrofit endpoint 공개 (기본적으로 actuator는 `health`만 공개. 명시적으로 추가 필요):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,retrofit
```

## 접근 방법

| 요청 | 설명 |
|---|---|
| `GET /actuator/retrofit` | 모든 client 목록 + `global` 글로벌 설정 섹션 + `count` |
| `GET /actuator/retrofit/{인터페이스 전체限定名}` | 인터페이스 전체限定名으로 단일 client 검색. 일치하지 않으면 404 |

## 응답 구조 예

```json
{
  "count": 2,
  "global": {
    "enableErrorDecoder": true,
    "globalConverterFactories": ["retrofit2.converter.jackson.JacksonConverterFactory"],
    "timeout": { "connectMs": 10000, "readMs": 10000, "writeMs": 10000, "callMs": 0 },
    "connectionPool": { "maxIdleConnections": 5, "keepAliveDurationMs": 300000 },
    "log":     { "enable": false, "logLevel": "INFO", "logStrategy": "BASIC", "aggregate": true },
    "retry":   { "enable": false, "maxRetries": 2, "intervalMs": 100,
                 "backoffStrategy": "FIXED", "maxIntervalMs": 30000, "jitter": 0.0,
                 "retryStatusCodes": [], "retryExceptionClasses": [],
                 "retryRules": ["RESPONSE_STATUS_NOT_2XX", "OCCUR_IO_EXCEPTION"] },
    "degrade": { "degradeType": "none",
                 "sentinel":     { "enable": false, "ruleCount": 0 },
                 "resilience4j": { "enable": false, "circuitBreakerConfigName": "defaultCircuitBreakerConfig" } },
    "metrics": { "enable": false, "metricNamePrefix": "retrofit.client", "tagHost": false, "tagUri": true }
  },
  "clients": [{
    "beanName": "userService",
    "interfaceName": "com.example.UserService",
    "baseUrl": "${test.baseUrl}",
    "resolvedBaseUrl": "http://localhost:8080/api/user/",
    "serviceId": null,
    "path": null,
    "converterFactories": [],
    "callAdapterFactories": [],
    "fallback": null,
    "fallbackFactory": null,
    "errorDecoder": "com.github.lianjiatech.retrofit.spring.boot.core.ErrorDecoder$DefaultErrorDecoder",
    "validateEagerly": false,
    "sourceOkHttpClient": null,
    "timeoutEffective": true,
    "timeout": { "connectMs": 3000, "readMs": 3000, "writeMs": 10000, "callMs": 0,
                 "inheritedFields": ["writeMs", "callMs"] },
    "pool":    { "maxIdleConnections": 5, "keepAliveDurationMs": 300000,
                 "inheritedFields": ["maxIdleConnections", "keepAliveDurationMs"] },
    "logging": { "source": "interface", "enable": true, "logLevel": "DEBUG",
                 "logStrategy": "BODY", "aggregate": true },
    "retry":   { "source": "global" },
    "degrade": { "enabled": false, "type": "none" }
  }]
}
```

## 필드 의미 설명

- **`resolvedBaseUrl`**: 해결된 최종 baseUrl. 인터페이스가 주입使用済(인스턴스화 트리거済)인 경우만 값이 있으며, 그 외는 `null` (baseUrl은 lazy 해석, 트리거되지 않은 경우 미리 해석하지 않음).
- **`timeout` / `pool`의 `inheritedFields`**: `@RetrofitClient`의 해당 필드 설정值 `-1`(기본值)은 "글로벌 설정 재사용"을 의미합니다. Endpoint는 실제 구축과 일치하는 규칙으로 `-1`을 글로벌 폴백值으로 해석하고, 이 필드名을 `inheritedFields`에 기록하여 "인터페이스 명시 설정"과 "글로벌 상속"을 구분합니다.
- **`timeoutEffective`**: 인터페이스가 `sourceOkHttpClient`로 커스텀 OkHttpClient를 지정한 경우 `false` (이 경우 타임아웃/연결 풀은 소스 클라이언트에서 결정, `timeout`/`pool`은 표시되지 않음).
- **`logging` / `retry`의 `source`**:
  - `"interface"`: 인터페이스에 `@Logging` / `@Retry` 어노테이션이 있으며, 다른 필드는 어노테이션 전개值;
  - `"global"`: 인터페이스에 해당 어노테이션이 없고, 런타임에서 글로벌 설정으로 폴백. 이 경우 값을 중복 전개하지 않으므로 최상위 `global` 섹션을 참조하세요.
  - 주의: 메서드 수준 `@Logging` / `@Retry`는 여기에서 drill-down 표시되지 않습니다 (런타임에서 메서드 어노테이션이 인터페이스, 인터페이스가 글로벌에 우선).
- **`degrade.enabled`**: `RetrofitDegrade.isEnableDegrade(인터페이스)`에서 가져옴; `type`은 글로벌 `degrade.degrade-type` (`none` / `sentinel` / `resilience4j`).
- **`fallback` / `fallbackFactory`**: 설정되지 않은 경우(기본 `void.class`) `null`.

---

[이전 섹션: 메트릭 모니터링 (Micrometer)](metrics.md) | [다음 섹션: GraalVM Native Image / AOT 지원](aot.md)