# 项目：Retrofit与Spring Boot集成Starter

## 技术栈
- mater分支：Java17 + Spring Boot 3.0
- 2.x分支：Java 8 + Spring Boot 1.4.2
- maven构建

## 项目规范
- 提交代码的时候，本文件可以永远跟着一起提交
- 代码注释使用英文，git提交信息使用英文
- 单元测试工具只能使用junit
- 编写集成测试代码即可（参考现在的测试代码编写）
- 编写新功能的时候，一定要同步更新所有语言文档

## 常用命令
- `mvn build` - 构建
- `mvn test` - 测试
- `mvn deploy` - 发布

## Ali P3C 强制要点（写代码前必读）

项目通过 `maven-pmd-plugin` + `p3c-pmd` 强制执行阿里 P3C 规约，Stop hook 会自动跑 `mvn spotless:apply` → `mvn pmd:check` → `mvn test`，违规会阻塞回答。下面是高频规则，写代码时**主动**遵守，避免事后被 PMD 打回：

### 命名
- 类/方法/变量：UpperCamelCase / lowerCamelCase；常量：UPPER_SNAKE_CASE。
- 包名全小写，单数形式。
- 抽象类以 `Abstract` 开头；测试类以 `Test` 结尾；异常类以 `Exception` 结尾。

### 常量与魔法值
- **禁止裸字面量**：HTTP 状态码、端口号、超时时间、缓冲区大小等都要定义为 `private static final` 常量。`0`、`1`、`-1`、空串、`null` 这类不算魔法值。
- 长整型字面量用大写 `L`（如 `1000L`），不用小写 `l`。

### 控制流
- **`if`/`else`/`for`/`while` 必须带大括号**，即使只有一行。
- 多层嵌套时优先用卫语句提前 return。
- `switch` 每个 case 必须有 `break`/`return`/`throw` 或显式 `// fallthrough` 注释，并强制 `default`。

### 并发
- 禁用 `Executors.newFixedThreadPool` / `newCachedThreadPool` / `newSingleThreadExecutor`，统一用 `new ThreadPoolExecutor(...)` 显式声明队列容量与拒绝策略。
- `ThreadLocal` 使用后**必须** `remove()`（尤其在线程池场景）。
- 多线程共享可变状态用 `java.util.concurrent.atomic` 或 `ConcurrentHashMap`，不要 `synchronized` 包 `HashMap`。

### 异常处理
- 禁止 `catch (Throwable t)` / `catch (Error e)`；只 catch 你能处理的具体异常。
- 禁止**吞异常**（空 catch 块），至少要 log，不传播也要写明原因。
- `finally` 块**禁止** `return`，会吃掉 try 中的异常。
- 抛 `RuntimeException` 之前先想：是不是该用受检异常或业务异常类。

### 集合
- `List.subList` 返回的是视图，原 list 改了会抛 `ConcurrentModificationException`，需要复制再用。
- `Arrays.asList(...)` 返回的是不可变定长 list，不能 `add`/`remove`。
- `toArray` 必须传指定大小数组：`list.toArray(new T[0])`，不要 `list.toArray()` 然后强转。
- 遍历删除元素必须用 `Iterator.remove()`，禁止 `for-each` + `list.remove()`。

### OOP
- POJO 类的 boolean 字段**不要**加 `is` 前缀（如 `isSuccess` 写成 `success`），避免序列化框架字段名错乱。
- 重写方法必须加 `@Override`。
- equals/hashCode 必须成对实现。

### 项目注释规范（已豁免 P3C 部分注释规则）
本项目已通过 `.claude/format/ali-p3c-ruleset.xml` 排除以下规则（不要再迁就它们）：
- `ClassMustHaveAuthorRule` — 不强制 `@author`
- `EnumConstantsMustHaveCommentRule` — 自解释枚举字段无需注释
- `AbstractMethodOrInterfaceMethodMustUseJavadocRule` — 接口方法不强制 `@param`/`@return`
- `ClassNamingShouldBeCamelRule` — 允许 `IOException` 风格命名

剩余规则全部生效。**新增类无需写 `@author`，但 public 方法/类应有简明 javadoc**。