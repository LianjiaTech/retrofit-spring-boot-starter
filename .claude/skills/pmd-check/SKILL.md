---
name: pmd-check
description: 对整个项目或指定模块批量执行 PMD 静态检查（PMD 7 内置规则集）。支持传入模块根、模块内子目录或 .java 文件路径；输出报告后询问用户是否修复。
disable-model-invocation: true
---

# /pmd-check Skill

对 Java 代码批量执行 PMD 静态检查（PMD 7 内置规则集）。

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

**所有步骤必须在同一个 `bash -c` 中执行**（common.sh 使用 bash 关联数组 `declare -A`，不能在 zsh 中直接 source；且 `$SCAN_ROOT` 等变量需在同一 bash 进程中传递）。

使用 Bash 工具执行以下完整脚本（`<ARG>` 替换为用户传入的 path，可能为空）：

```bash
bash -c '
source .claude/hooks/lib/common.sh
ensure_java_home || { warn_missing_java_home; exit 1; }

PROJECT_ROOT="$(find_project_root)"
cd "$PROJECT_ROOT"

# ---- 步骤 1：运行 PMD 扫描 ----
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
# pmd:pmd 不因违规而 fail build，报告输出到各模块 target/pmd.xml

# ---- 步骤 2+3：收集报告并解析 ----
# 用 mapfile 将 find 输出读入 bash 数组，再用 "${PMD_ARR[@]}" 展开
# 这样每个文件路径作为独立参数传给 parse_pmd_report（内部 awk 通过 "$@" 接收）
# 不能用 xargs（parse_pmd_report 是 bash 函数，xargs 只能调用外部命令）
# 不能用 parse_pmd_report $PMD_FILES（多个路径会被拼成单个字符串，awk 找不到文件）
mapfile -t PMD_ARR < <(find "$SCAN_ROOT" -type f -path "*/target/pmd.xml" -not -path "*/node_modules/*")
if (( ${#PMD_ARR[@]} == 0 )); then
  echo "FILE_COUNT=0"
  echo "TOTAL_COUNT=0"
  exit 0
fi

REPORT_FILE="$(mktemp)"
parse_pmd_report "${PMD_ARR[@]}" > "$REPORT_FILE"

SUMMARY="$(head -1 "$REPORT_FILE")"
FILE_COUNT="$(echo "$SUMMARY" | cut -d" " -f1)"
TOTAL_COUNT="$(echo "$SUMMARY" | cut -d" " -f2)"
FORMAT="$(format_pmd_report_cn "$REPORT_FILE" "$PROJECT_ROOT")"
rm "$REPORT_FILE"

# 输出供 Claude 读取的结果
echo "FILE_COUNT=$FILE_COUNT"
echo "TOTAL_COUNT=$TOTAL_COUNT"
echo "---FORMAT_START---"
echo "$FORMAT"
echo "---FORMAT_END---"
'
```

从 Bash 工具输出中提取：
- `FILE_COUNT` 为有违规的文件数，`TOTAL_COUNT` 为总违规数
- `---FORMAT_START---` 和 `---FORMAT_END---` 之间的内容为中文分类表格格式，按规则类别分组展示

   输出报告：
   ```
   扫描完成：N 个文件有违规，共 M 处。

   $FORMAT 的内容（直接输出）

   请选择后续操作：
     A. 修复 → 我会逐个文件调用 Edit 修复违规
     B. 只看报告 → 结束，不做任何修改
     C. 给出修复方案 → 逐文件给出具体代码修改建议，但不实际修改代码
   ```

   若 `$FILE_COUNT` 为 0 → 向用户输出："扫描完成：全部通过 ✓"

4. 等待用户响应：
   - A（修复） → 对每个有违规的文件，使用 Edit 工具修复违规
     - 修复完成后，Stop hook 会自动跑 `spotless:apply + pmd:pmd` 复查；未通过会阻断并反馈
   - B（只看报告） → 直接结束
   - C（给出修复方案） → 对每个有违规的文件，读取源码并给出具体修复建议（指出应删/改的代码位置和修改方式），但不调用 Edit/Write 修改代码

## 与 hook 的关系

回合结束时 Stop hook 会自动跑 `spotless:apply + pmd:pmd`。本 skill 用于**主动全量或按模块扫描**，不受 hook 阻断计数影响。

## 注意

- **必须**先输出报告再询问是否修复，不要默认修复
- 用户回复"修复"后，逐文件调 Edit；不要尝试批量并发
- 传子目录/单文件时，实际是对**整个所属模块**扫描，不做子目录粒度过滤
- 如果失败，提示用户检查 pom.xml 是否已包含 maven-pmd-plugin（PMD 7 规则集）
