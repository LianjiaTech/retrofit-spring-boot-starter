---
name: pmd-check
description: 对整个项目或指定模块批量执行 PMD 静态检查（ali-p3c 全集）。支持传入模块根、模块内子目录或 .java 文件路径；输出报告后询问用户是否修复。
disable-model-invocation: true
---

# /pmd-check Skill

对 Java 代码批量执行 PMD 静态检查（ali-p3c 全集规则）。

## 触发场景

用户输入：
- `/pmd-check`            —— 对整个项目跑 `mvn pmd:pmd`
- `/pmd-check <path>`     —— 定位 path 所属的 Maven 模块，对**该整个模块**执行 pmd:pmd

## path 参数语义

- path 可以是模块根、模块内任意子目录、单个 `.java` 文件，绝对路径或相对项目根的路径均可
- 无论 path 指向多深，实际执行粒度都是**模块级**：向上找最近的 `pom.xml` 定位所属模块，然后对该模块整体扫描一遍
- 若 path 指向项目聚合根，等同 `/pmd-check`（不带参数）
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
     mvn -q -B pmd:pmd -DincludeTests=false
     SCAN_ROOT="$PROJECT_ROOT"
   else
     if [[ ! -e "$ARG" ]]; then
       echo "错误：路径不存在：$ARG"
       echo "提示：/pmd-check 只支持传入模块根、模块内的子目录或 .java 文件；"
       echo "     若想扫描整个项目，请直接使用 /pmd-check（不带参数）。"
       exit 1
     fi
     MOD="$(find_module_root "$ARG")" || {
       echo "错误：$ARG 不属于任何 Maven 模块（未找到 pom.xml）。"
       echo "提示：/pmd-check 只支持传入模块根、模块内的子目录或 .java 文件；"
       echo "     若想扫描整个项目，请直接使用 /pmd-check（不带参数）。"
       exit 1
     }
     if [[ "$MOD" == "$PROJECT_ROOT" ]]; then
       mvn -q -B pmd:pmd -DincludeTests=false
       SCAN_ROOT="$PROJECT_ROOT"
     else
       REL="${MOD#$PROJECT_ROOT/}"
       mvn -q -B -pl "$REL" pmd:pmd -DincludeTests=false
       SCAN_ROOT="$MOD"
     fi
   fi
   ```
   `pmd:pmd` 不因违规而 fail build，报告输出到各模块 `target/pmd.xml`

2. 收集所涉模块的 `target/pmd.xml`（`$SCAN_ROOT` 为模块根或项目根）：
   ```bash
   find "$SCAN_ROOT" -type f -path "*/target/pmd.xml" -not -path "*/node_modules/*"
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

回合结束时 Stop hook 会自动跑 `spotless:apply + pmd:pmd`。本 skill 用于**主动全量或按模块扫描**，不受 hook 阻断计数影响。

## 注意

- **必须**先输出报告再询问是否修复，不要默认修复
- 用户回复"修复"后，逐文件调 Edit；不要尝试批量并发
- 传子目录/单文件时，实际是对**整个所属模块**扫描，不做子目录粒度过滤
- 如果失败，提示用户检查 pom.xml 是否已包含 maven-pmd-plugin + p3c-pmd 依赖
