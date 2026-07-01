---
name: pmd-check
description: 对整个项目批量执行 PMD 静态检查（ali-p3c 全集）；输出报告后询问用户是否修复。
disable-model-invocation: true
---

# /pmd-check Skill

对 Java 代码批量执行 PMD 静态检查（ali-p3c 全集规则）。

## 触发场景

用户输入：
- `/pmd-check`            —— 对整个项目跑 `mvn pmd:pmd`
- `/pmd-check <path>`     —— 目前忽略 path，直接跑整仓（未来可按模块细化）

## 执行步骤

1. 使用 Bash 工具在项目根执行：
   ```bash
   mvn -q -B pmd:pmd -DincludeTests=false
   ```
   `pmd:pmd` 不因违规而 fail build，报告输出到各模块 `target/pmd.xml`

2. 收集所有模块的 `target/pmd.xml`：
   ```bash
   find . -type f -path "*/target/pmd.xml" -not -path "*/node_modules/*"
   ```

3. 解析 XML（每个 `<violation>` 元素含 `beginline`、`rule`、`ruleset` 属性和文本描述），按文件分组输出报告：
   ```
   扫描完成：N 个文件有违规，共 M 处。

   <文件 1 相对路径>：
     L42  [RuleName1]  描述
     L58  [RuleName2]  描述
   ...

   是否要逐文件自动修复？
     - 回复 "修复" / "好的" → 我会逐个文件调用 Edit 修复
     - 回复 "只看报告" / "不修复" → 结束
   ```

   若所有 `target/pmd.xml` 中都没有 `<violation>` → 向用户输出："扫描完成：全部通过 ✓"

4. 等待用户响应：
   - 若确认修复 → 对每个有违规的文件，使用 Edit 工具修复违规
     - 修复完成后，Stop hook 会自动跑 `spotless:apply + pmd:pmd` 复查；未通过会阻断并反馈
   - 若拒绝 → 直接结束

## 与 hook 的关系

回合结束时 Stop hook 会自动跑 `spotless:apply + pmd:pmd`。本 skill 用于**主动全量扫描**，不受 hook 阻断计数影响。

## 注意

- **必须**先输出报告再询问是否修复，不要默认修复
- 用户回复"修复"后，逐文件调 Edit；不要尝试批量并发
- 如果失败，提示用户检查 pom.xml 是否已包含 maven-pmd-plugin + p3c-pmd 依赖
