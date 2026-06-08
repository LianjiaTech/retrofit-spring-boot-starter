# 메서드 수준 타임아웃 설정
[English](../en/timeout.md) | [简体中文](../cn/timeout.md) | [繁體中文](../tw/timeout.md) | [日本語](../ja/timeout.md) | **한국어** | [Español](../es/timeout.md) | [Türkçe](../tr/timeout.md) | [Русский](../ru/timeout.md)

컴포넌트는 `@Timeout` 어노테이션을 사용하여 메서드 또는 클래스 수준에서 타임아웃 매개변수를 설정하고 글로벌 타임아웃 설정을 오버라이드하는 것을 지원합니다.

## 우선순위 체인

```
메서드 @Timeout → 클래스 @Timeout → 글로벌 설정 (GlobalTimeoutProperty)
```

- `@Timeout` 속성 기본값 `-1`은 "설정 안됨, 상위 우선순위 체인 상속"을 의미합니다
- `0`은 "타임아웃 없음"을 의미합니다
- 양수는 특정 타임아웃 시간(밀리초)을 의미합니다
- `-1`은 OkHttp 타임아웃의 불법 값 범위(OkHttp는 0과 양수만 허용)이며, "설정 안됨" 마크로 사용해도 합법적 타임아웃 값과 충돌하지 않습니다

## 4가지 타임아웃 디멘션

| 속성 | 의미 | 기본값 |
|------|------|--------|
| `connectTimeoutMs` | 연결 타임아웃 (밀리초) | `-1` (상위 상속) |
| `readTimeoutMs` | 읽기 타임아웃 (밀리초) | `-1` (상위 상속) |
| `writeTimeoutMs` | 쓰기 타임아웃 (밀리초) | `-1` (상위 상속) |
| `callTimeoutMs` | 전체 호출 타임아웃 (밀리초) | `-1` (상위 상속) |

## 클래스 수준 @Timeout

인터페이스에 `@Timeout`을 선언하면, 해당 인터페이스의 모든 메서드에 타임아웃을 설정합니다:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    @GET("getUser")
    User getUser(@Query("id") Long id);
}
```

## 메서드 수준 @Timeout

특정 메서드에 `@Timeout`을 선언하면 클래스 수준 또는 글로벌 타임아웃 설정을 오버라이드합니다:

```java
@Timeout(readTimeoutMs = 5000)
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 클래스 수준 readTimeoutMs = 5000 상속
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 메서드 수준 오버라이드: 느린 쿼리 인터페이스는 더 긴 타임아웃 사용
    @Timeout(readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 메서드 수준 @Timeout만 (클래스 수준 어노테이션 없이)

```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface UserService {

    // 글로벌 타임아웃 설정 사용
    @GET("getUser")
    User getUser(@Query("id") Long id);

    // 이 메서드만 커스텀 타임아웃 사용
    @Timeout(connectTimeoutMs = 3000, readTimeoutMs = 30000)
    @GET("search")
    List<User> searchUsers(@Query("q") String query);
}
```

## 구현 메커니즘

- 클래스 수준 `@Timeout`: OkHttpClient 생성 시 처리. 런타임 오버헤드 없음
- 메서드 수준 `@Timeout`: `TimeoutCallFactory`가 per-method OkHttpClient clone을 미리 생성. 런타임에서 Invocation tag로 검색. 어노테이션 없는 인터페이스는 추가 오버헤드 없음

---

[이전 섹션: OkHttpClient 커스터마이징](okhttp-client.md) | [다음 섹션: 로그 출력](logging.md)