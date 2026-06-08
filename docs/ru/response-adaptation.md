# Автоматическая адаптация результатов HTTP-ответа
[English](../en/response-adaptation.md) | [简体中文](../cn/response-adaptation.md) | [繁體中文](../tw/response-adaptation.md) | [日本語](../ja/response-adaptation.md) | [한국어](../ko/response-adaptation.md) | [Español](../es/response-adaptation.md) | [Türkçe](../tr/response-adaptation.md) | **Русский**

Компонент автоматически адаптирует результаты HTTP-ответа к типу возврата, определённому в Java-интерфейсе. В настоящее время поддерживаются следующие типы возврата:

- `Call<T>`: не выполняет адаптацию, напрямую возвращает объект `Call<T>`
- `String`: адаптирует тело ответа (Response Body) в `String`
  - По умолчанию использует JSON Converter для преобразования bytes Response Body в String; если нужно напрямую получить String из Response Body, можно указать `Converter.Factory` как `com.github.lianjiatech.retrofit.spring.boot.core.StringConverterFactory`
- Базовые типы (`Long`/`Integer`/`Boolean`/`Float`/`Double`): адаптирует тело ответа в соответствующий базовый тип
- `CompletableFuture<T>`: адаптирует тело ответа в объект `CompletableFuture<T>`
- `Void`: используется, когда тип возврата не важен
- `Response<T>`: адаптирует ответ в объект `Retrofit.Response<T>`
- `Mono<T>`: реактивный тип возврата Project Reactor
- `Single<T>`: реактивный тип возврата RxJava (поддерживает RxJava2/RxJava3)
- `Completable`: реактивный тип возврата RxJava, для сценариев HTTP-запросов без тела ответа (поддерживает RxJava2/RxJava3)
- Любой POJO-тип: адаптирует тело ответа в соответствующий POJO-объект

## Метод реализации адаптации

Retrofit на нижнем уровне через `CallAdapterFactory` адаптирует объект `Call<T>` к типу возврата метода интерфейса. Компонент расширяет следующие реализации `CallAdapterFactory`:

- **BodyCallAdapterFactory**
  - Синхронно выполняет HTTP-запрос, адаптирует содержимое тела ответа к типу возврата метода
  - Может использоваться для любого типа возврата метода, имеет самый низкий приоритет

- **ResponseCallAdapterFactory**
  - Синхронно выполняет HTTP-запрос, адаптирует содержимое тела ответа в `Retrofit.Response<T>`
  - Действует только когда тип возврата метода — `Retrofit.Response<T>`

- **CallAdapterFactory для реактивного программирования**
  - Поддерживает реактивные типы такие как `Mono<T>`, `Single<T>`, `Completable`

Через наследование `CallAdapter.Factory` можно реализовать адаптацию HTTP-ответа к типу возврата Java-интерфейса любым способом. Компонент поддерживает конфигурацию глобальной фабрики адаптеров вызовов через `retrofit.global-call-adapter-factories`:

```yaml
retrofit:
  # Глобальные фабрики адаптеров (расширённые CallAdapterFactory компонента уже встроены, не добавляйте повторно)
  global-call-adapter-factories:
    # ...
```

Для каждого Java-интерфейса можно также указать `CallAdapter.Factory`, используемый текущим интерфейсом, через `@RetrofitClient.callAdapterFactories`.

---

[Назад к списку функций](../../README.md) | Следующий: [Пользовательский конвертер данных](converter.md)