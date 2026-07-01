---
name: format
description: 对整个项目或指定模块批量格式化 Java 代码（spotless + eclipse-formatter）。支持传入模块根、模块内子目录或 .java 文件路径。
disable-model-invocation: true
---

# /format Skill

对 Java 代码批量执行格式化（spotless + eclipse P3C-CodeStyle）。

## 触发场景

用户输入：
- `/format`              —— 对整个项目跑 `mvn spotless:apply`
- `/format <path>`       —— 定位 path 所属的 Maven 模块，对**该整个模块**执行 spotless

## path 参数语义

- path 可以是模块根、模块内任意子目录、单个 `.java` 文件，绝对路径或相对项目根的路径均可
- 无论 path 指向多深，实际执行粒度都是**模块级**：向上找最近的 `pom.xml` 定位所属模块，然后对该模块整体跑一遍。spotless 有增量缓存，未变更文件近乎零成本
- 若 path 指向项目聚合根，等同 `/format`（不带参数）
- 若 path 不存在、或向上找不到 `pom.xml` → 报错并结束

## 执行步骤

1. 使用 Bash 工具在项目根加载 helper 并解析参数：
   ```bash
   source .claude/hooks/lib/common.sh
   ensure_java_home || { warn_missing_java_home; exit 1; }

   PROJECT_ROOT="$(find_project_root)"
   cd "$PROJECT_ROOT"

   ARG="<用户传入的 path，可能为空>"
   if [[ -z "$ARG" ]]; then
     mvn -q -B spotless:apply
   else
     # 路径校验
     if [[ ! -e "$ARG" ]]; then
       echo "错误：路径不存在：$ARG"
       echo "提示：/format 只支持传入模块根、模块内的子目录或 .java 文件；"
       echo "     若想格式化整个项目，请直接使用 /format（不带参数）。"
       exit 1
     fi
     MOD="$(find_module_root "$ARG")" || {
       echo "错误：$ARG 不属于任何 Maven 模块（未找到 pom.xml）。"
       echo "提示：/format 只支持传入模块根、模块内的子目录或 .java 文件；"
       echo "     若想格式化整个项目，请直接使用 /format（不带参数）。"
       exit 1
     }
     # 若定位到的模块就是项目根，退化为全量（避免 -pl . 的行为差异）
     if [[ "$MOD" == "$PROJECT_ROOT" ]]; then
       mvn -q -B spotless:apply
     else
       REL="${MOD#$PROJECT_ROOT/}"
       mvn -q -B -pl "$REL" spotless:apply
     fi
   fi
   ```
2. 简要汇报是否成功；spotless 无违规时无输出，视为通过

## 与 hook 的关系

回合结束时 Stop hook 会自动跑 `spotless:apply + pmd:pmd`。本 skill 用于用户主动想批量格式化（如刚 rebase 完、贴了外部代码，或想只格式化某个模块）时使用。

## 注意

- 不要询问用户确认。格式化是修正性、确定性操作，无副作用
- spotless 直接写文件，不需要 Edit 工具介入
- 传子目录/单文件时，实际是对**整个所属模块**格式化，不做子目录粒度过滤
- 如果失败，提示用户检查 pom.xml 是否已包含 spotless-maven-plugin
