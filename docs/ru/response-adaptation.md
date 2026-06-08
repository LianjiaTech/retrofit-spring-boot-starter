# Автоматическая адаптация HTTP-ответов
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | **Русский**

Данный компонент автоматически адаптирует HTTP-ответы к типам возврата, определенным в Java-интерфейсах. В настоящее время поддерживаются следующие типы возврата:

- `Call<T>`: Без адаптации, возвращается объект `Call<T>` напрямую
- `String`: Response Body адаптируется в `String`
  - По умолчанию используется JSON Converter для преобразования bytes Response Body в String; если нужно получить String напрямую из Response Body, можно указать `Converter.Factory` как `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Примитивные типы (`Long`/`Integer`/`Boolean`/`Float`/`Double`): Response Body адаптируется в соответствующий примитивный тип
- `CompletableFuture<T>`: Response Body адаптируется в объект `CompletableFuture<T>`
- `Void`: Используется, когда тип возврата не важен
- `Response<T>`: Ответ адаптируется в объект `Retrofit.Response<T>`
- `Mono<T>`: Реактивный тип возврата Project Reactor
- `Single<T>`: Реактивный тип возврата RxJava (поддерживает RxJava2/RxJava3)
- `Completable`: Реактивный тип возврата RxJava для HTTP-запросов без тела ответа (поддерживает RxJava2/RxJava3)
- Любой POJO-тип: Response Body адаптируется в соответствующий POJO-объект

## Механизм адаптации

Retrofit на нижнем уровне использует `CallAdapterFactory` для адаптации объектов `Call<T>` к типам возврата методов интерфейса. Данный компонент расширяет следующие реализации `CallAdapterFactory`:

- **BodyCallAdapterFactory**
  - Синхронно выполняет HTTP-запрос и адаптирует содержимое тела ответа к типу возврата метода
  - Может использоваться с любым типом возврата метода, имеет самый низкий приоритет

- **ResponseCallAdapterFactory**
  - Синхронно выполняет HTTP-запрос и адаптирует содержимое тела ответа к `Retrofit.Response<T>`
  - Действует только когда тип возврата метода - `Retrofit.Response<T>`

- **CallAdapterFactory для реактивного программирования**
  - Поддерживает реактивные типы, такие как `Mono<T>`, `Single<T>`, `Completable` и др.

Через наследование `CallAdapter.Factory` можно реализовать любой способ адаптации HTTP-ответа к типу возврата Java-интерфейса. Компонент поддерживает конфигурацию глобальных фабрик адаптеров вызовов через `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Глобальные фабрики адаптеров (расширения CallAdapterFactory компонента уже встроены, не конфигурируйте повторно)
  global-call-adapter-factories:
    # ...
```

Для каждого Java-интерфейса можно также указать используемые `CallAdapter.Factory` через `@RetrofitClient.callAdapterFactories`.

---

[Индекс функций](../../README.md) | [Следующая: Конвертер данных](converter.md)