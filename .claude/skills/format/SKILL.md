---
name: format
description: 对指定路径下所有 Java 文件批量格式化（spotless + eclipse-formatter）。不指定 path 时默认整个项目。
disable-model-invocation: true
---

# /format Skill

对 Java 代码批量执行格式化（spotless + eclipse P3C-CodeStyle）。

## 触发场景

用户输入：
- `/format`              —— 对整个项目跑 `mvn spotless:apply`
- `/format <path>`       —— 目前忽略 path 参数，直接跑整仓（spotless 内置增量缓存，重复跑近乎零成本；未来可按模块细化）

## 执行步骤

1. 使用 Bash 工具在项目根执行（先加载 hooks/lib/common.sh 中的 `ensure_java_home`，保证 mvn 用 JDK 17+）：
   ```bash
   source .claude/hooks/lib/common.sh
   ensure_java_home || { warn_missing_java_home; exit 1; }
   mvn -q -B spotless:apply
   ```
2. 简要汇报是否成功；spotless 无违规时无输出，视为通过

## 与 hook 的关系

回合结束时 Stop hook 会自动跑 `spotless:apply + pmd:pmd`。本 skill 仅在用户主动想批量格式化（如刚 rebase 完、贴了外部代码）时使用。

## 注意

- 不要询问用户确认。格式化是修正性、确定性操作，无副作用
- spotless 直接写文件，不需要 Edit 工具介入
- 如果失败，提示用户检查 pom.xml 是否已包含 spotless-maven-plugin
