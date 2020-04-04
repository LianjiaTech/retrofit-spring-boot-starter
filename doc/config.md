## spring-boot配置
### yml配置

```yml
retrofit-plus:
  # 是否启用 BodyCallAdapter适配器
  enable-body-call-adapter: true
  # 是否启用 ResponseCallAdapter适配器
  enable-response-call-adapter: true
  # 是否启用 Retrofit2Converter转码器
  enable-fast-json-converter: true
  # 启用日志打印
  enable-log: true
  # 连接池配置
  pool:
    test1:
      max-idle-connections: 3
      keep-alive-second: 100
    test2:
      max-idle-connections: 5
      keep-alive-second: 50
```

### properties配置

```properties
# 是否启用 BodyCallAdapter适配器
retrofit-plus.enable-body-call-adapter=true
# 是否启用 ResponseCallAdapter适配器
retrofit-plus.enable-response-call-adapter=true
# 是否启用 Retrofit2Converter转码器
retrofit-plus.enable-fast-json-converter=true
# 是否启用日志打印
retrofit-plus.enable-log=true
# 连接池配置
retrofit-plus.pool.test1.max-idle-connections=3
retrofit-plus.pool.test1.keep-alive-second=100
retrofit-plus.pool.test2.max-idle-connections=2
retrofit-plus.pool.test2.keep-alive-second=200
```

## spring项目配置
spring项目中，使用retrofitHelper的Bean进行属性配置的
```java
@RetrofitScan(value = "扫描包", retrofitHelperRef = "retrofitHelper")
public class RetrofitRefBeanConfig {
    /**
     * 使用RetrofitHelper进行配置
     *
     * @return
     */
    @Bean
    public RetrofitHelper retrofitHelper() {
        // 连接池配置
        PoolConfig test1 = new PoolConfig(5, 300);
        PoolConfig test2 = new PoolConfig(2, 100);
        Map<String, PoolConfig> pool = new HashMap<>();
        pool.put("test1", test1);
        pool.put("test2", test2);
        // 配置对象
        Config config = new Config();
        config.setPool(pool);
        // 是否启用 BodyCallAdapter适配器
        config.setEnableBodyCallAdapter(true);
        // 是否启用 ResponseCallAdapter适配器
        config.setEnableResponseCallAdapter(true);
        // 是否启用 Retrofit2Converter转码器
        config.setEnableFastJsonConverter(true);
        // 启用日志打印
        config.setEnableLog(true);
        // retrofitHelper bean
        RetrofitHelper retrofitHelper = new RetrofitHelper(config);

        // 配置其他属性 这些配置也可以配在spring其他的配置文件中
        retrofitHelper.addProperty("test.baseUrl", "http://localhost:8080/api/test/");
        retrofitHelper.addProperty("test.accessKeyId", "2523453463456");
        retrofitHelper.addProperty("test.accessKeySecret", "sdjfsdfasdfdg");
        return retrofitHelper;
    }
}
```