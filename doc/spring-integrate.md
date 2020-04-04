## 与spring集成
> 支持spring4.2以上版本，目前暂不支持xml配置方式

### 引入依赖
```xml
<dependency>
    <groupId>com.github.lianjiaTech</groupId>
    <artifactId>retrofit-plus</artifactId>
    <version>1.1.1</version>
</dependency>
```

### 配置`@RetrofitScan`注解
你可以给带有 `@Configuration` 的类配置`@RetrofitScan`，配合`RetrofitHelper`bean进行配置：
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
**@RetrofitScan字段含义：[@RetrofitScan](https://github.com/lianjiaTech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjia/retrofit/plus/annotation/RetrofitScan.java)**

### 定义http调用接口
**接口必须使用`@RetrofitClient`注解标记！**
```java
@RetrofitClient(baseUrl = "${test.baseUrl}")
public interface HttpApi {

    @GET("person")
    Result<Person> getPerson(@Query("id") Long id);
}
```
**@RetrofitClient字段含义：[@RetrofitClient](https://github.com/lianjiaTech/retrofit-plus/blob/master/retrofit-plus/src/main/java/com/github/lianjia/retrofit/plus/annotation/RetrofitClient.java)**


### 注入使用
**将接口注入到其它bean中即可使用！**
```java
@Autowired
private HttpApi httpApi;

@Test
public void test() {
    Result<Person> person = httpApi.getPerson(1L);
    Person data = person.getData();
    Assert.assertNotNull(data);
    Assert.assertEquals(1L,data.getId().longValue());
    Assert.assertEquals("test",data.getName());
    Assert.assertEquals(10,data.getAge().intValue());
}
```

