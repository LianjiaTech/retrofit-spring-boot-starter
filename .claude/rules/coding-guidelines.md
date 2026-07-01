# PMD 代码规范要点（由 team-ai-coding-toolkit 提供）

> 完整规范以 PMD ali-p3c 全集规则为准，以下仅列出 Claude 容易忽略的高频要点。
> 本文件由 toolkit 维护，不要手动编辑。

## Hook 协作约定

本项目有 Claude Code hook 自动运行：
- **PostToolUse**（每次 Edit/Write/MultiEdit 后）仅记录被触及的 `.java` 文件路径到 `.claude/hooks/.edited-files.txt`，不做任何检查、不阻塞
- **Stop**（回合结束时）对本轮编辑过的 `.java` 一次性跑 `mvn spotless:apply pmd:pmd`；如有 PMD 违规则阻断本轮响应，把整份 `target/pmd.xml` 作为 reason 反馈

当 Stop hook 阻断并反馈失败信息时：

1. 逐条修复违规，不要绕过、不要禁用规则、不要修改 hook 配置去回避问题
2. 修复完成后，在最终回复中向用户简要汇报：
   - 触发了哪些 PMD 规则
   - 各自修复了什么
3. Stop 阻断有循环上限（默认 3 轮/回合）。若 hook 反馈"已停止自动修复循环"：
   - **停止继续修改**
   - 在回复中清晰说明哪些规则项卡住、推测原因
   - 由用户决定是手动修改、调整 pom 中的 pmd ruleset 排除条目，还是暂时接受违规
4. 如果某条规则确实不合理需要团队讨论调整，**先完成当前任务**，
   再单独向用户提出"建议调整 ruleset 的 X 条规则，原因是 Y"

## Skill 使用

项目提供两个 skill 用于用户主动触发批量检查：

- `/format` —— 对整个项目跑 `mvn spotless:apply`
- `/pmd-check` —— 对整个项目跑 `mvn pmd:pmd`，输出报告，询问是否修复

- `/format` 直接执行，无需用户确认
- `/pmd-check` 仅输出违规报告，**不自动修复**；询问用户是否修复，得到肯定后再逐文件 Edit
  Edit 后 Stop hook 会自动复查

## 编码要点

- 命名：变量 camelCase，常量 UPPER_SNAKE_CASE
- Map 取值：禁止 `containsKey() + get()` 组合，一次 `get()` + null 判断即可
- 异常：禁止吞异常（catch 后必须 log 或抛出），禁止 catch Throwable / Exception 顶级类型
- 日志：使用 slf4j 占位符（`log.info("xxx={}", value)`），不用字符串拼接
- 注释：业务关键路径必须注释"为什么"，不写"做了什么"

## 单元测试约定

修改业务代码时同步补充 / 更新单元测试，覆盖：

- 新增的 public 方法
- 修改的核心分支逻辑
- 修复的 bug（回归测试）

测试类放在对应模块的 `src/test/java`，包路径与被测类一致，类名 `<ClassName>Test`。

### 何时建议用户运行测试

**本地 hook 不自动跑单测和编译**（编译/单测由 CI 兜底）。
当完成以下场景时，请在最终回复末尾建议用户运行特定测试命令进行本地验证：

- 新增或修改了 public 方法的业务逻辑
- 修复了 bug
- 改动了核心业务路径

建议命令格式：

    mvn -pl <模块> -am test -Dtest=<TestClass>

不要主动调用 Bash 跑测试（避免长时间等待），把时机决策权交给用户。

## Git 提交

- 提交信息使用英文，遵循 conventional commits（feat / fix / refactor / test / chore）
- 不在提交中混入无关改动（格式化大批量已有文件请单独提交）
