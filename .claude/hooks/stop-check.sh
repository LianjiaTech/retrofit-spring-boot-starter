#!/usr/bin/env bash
# Claude Code Stop hook: 回合结束时对本轮 AI 改过的 .java 文件做 spotless + pmd
# 协议：
#   - stdout JSON {"decision":"block","reason":"..."} → Claude 继续响应并把 reason 作为上下文
#   - stdout 无内容 / exit 0 → 正常结束
# 失败不阻断（异常时打印 stderr 警告，正常 exit 0）
#
# 与旧实现相比的关键行为差异：
#   - spotless:apply 通过 -DspotlessFiles=<正则列表> 精确到本轮编辑的文件
#     避免格式化历史代码造成大量无关 diff
#   - pmd:pmd 仍按模块整跑，但事后按 edited files 过滤 target/pmd.xml
#     只把本轮文件的违规反馈给 AI，历史违规不参与阻断循环、也不消耗 token

set -uo pipefail

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$HOOK_DIR/lib/common.sh"
# shellcheck source=lib/config.sh
source "$HOOK_DIR/lib/config.sh"

# 消费 stdin（Stop hook 有 payload，但当前实现不需要）
cat >/dev/null || true

if ! ensure_java_home; then
  warn_missing_java_home
  echo "  (跳过 stop-check)" >&2
  exit 0
fi

PROJECT_ROOT="$(find_project_root)" || exit 0
# 规范化项目根路径，避免 macOS 上 /var 与 /private/var 之类的 symlink 差异
# 导致后续 "${mod#$PROJECT_ROOT/}" 前缀剥离失败
PROJECT_ROOT="$(cd "$PROJECT_ROOT" && pwd -P)"

# 读列表并分组；若为空说明本轮没改 .java，直接放行
GROUPED="$(edited_files_grouped)"
if [[ -z "$GROUPED" ]]; then
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 本轮确实要跑 PMD 并输出 block JSON，此时才强依赖 jq
if ! command -v jq >/dev/null 2>&1; then
  cat <<'JSON'
{"decision":"block","reason":"Stop hook 缺少依赖 jq，PMD 检查未执行。请安装后重试：\n  macOS: brew install jq\n  Ubuntu/Debian: apt install jq\n  Windows: winget install jqlang.jq"}
JSON
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 按模块聚合：MODULES 用于 -pl；ALL_FILES 用于 -DspotlessFiles 及 PMD 过滤白名单
MODULES=()
declare -A MODULE_FILES
ALL_FILES=()
while IFS=$'\t' read -r mod files; do
  [[ -z "$mod" ]] && continue
  # 规范化 mod 路径与 PROJECT_ROOT 一致（-P 解析 symlink），保证前缀剥离生效
  mod="$(cd "$mod" && pwd -P)"
  rel="${mod#$PROJECT_ROOT/}"
  if [[ "$rel" == "$mod" ]]; then rel="."; fi
  MODULES+=("$rel")
  MODULE_FILES["$mod"]="$files"
  # files 是逗号分隔的绝对路径
  IFS=',' read -ra parts <<< "$files"
  for p in "${parts[@]}"; do
    [[ -n "$p" ]] && ALL_FILES+=("$p")
  done
done <<< "$GROUPED"

# 拼 -pl a,b,c
PL_ARG="$(IFS=','; echo "${MODULES[*]}")"

# spotless 分批：按字符预算切分 -DspotlessFiles 参数值
# macOS 的 /var 是 /private/var 的 symlink；spotless 会以 canonical 绝对路径
# （通常带 /private 前缀）与 spotlessFiles 正则做匹配，因此为 /var/... 的路径
# 同时插入 /private/var/... 的孪生转义正则，保证匹配到位。
ESC_FILES=()
for f in "${ALL_FILES[@]}"; do
  ESC_FILES+=("$(escape_regex "$f")")
  if [[ "$f" == /var/* ]]; then
    ESC_FILES+=("$(escape_regex "/private$f")")
  elif [[ "$f" == /private/var/* ]]; then
    ESC_FILES+=("$(escape_regex "${f#/private}")")
  fi
done

SPOTLESS_BATCHES=()
while IFS= read -r batch; do
  [[ -n "$batch" ]] && SPOTLESS_BATCHES+=("$batch")
done < <(printf '%s\n' "${ESC_FILES[@]}" | build_spotless_batches "$SPOTLESS_FILES_BUDGET")

MVN_STDERR="$(mktemp)"
trap 'rm -f "$MVN_STDERR"' EXIT

SPOTLESS_WARN=""
SPOTLESS_FAILED=0

# 逐批跑 spotless:apply；某批失败降级为"跳过 spotless，附警告，仍跑 PMD"
for batch in "${SPOTLESS_BATCHES[@]}"; do
  RC=0
  (
    cd "$PROJECT_ROOT" && \
    mvn -pl "$PL_ARG" -q -B -fae -DspotlessFiles="$batch" spotless:apply
  ) 2>>"$MVN_STDERR" || RC=$?
  if (( RC != 0 )); then
    SPOTLESS_FAILED=1
    SPOTLESS_WARN=$'spotless:apply 执行失败（已降级跳过，仍继续 PMD 检查）。请检查 pom 是否已注入 spotless-maven-plugin。\n'
    break
  fi
done

# 单独跑 pmd:pmd，覆盖涉及的所有模块
PMD_RC=0
(
  cd "$PROJECT_ROOT" && \
  mvn -pl "$PL_ARG" -q -B -fae pmd:pmd
) 2>>"$MVN_STDERR" || PMD_RC=$?

if (( PMD_RC != 0 )); then
  ERR_TAIL="$(tail -n 40 "$MVN_STDERR")"
  REASON=$(printf '%sStop hook 执行 mvn pmd:pmd 失败（退出码 %s）。请在最终回复中提醒用户检查 pom.xml 是否已注入 maven-pmd-plugin。\n\nmvn stderr 尾部：\n%s' "$SPOTLESS_WARN" "$PMD_RC" "$ERR_TAIL")
  jq -n --arg r "$REASON" '{decision:"block", reason:$r}'
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 汇总各模块的 target/pmd.xml，按 edited files 过滤后统计违规
ALL_VIOLATIONS_XML=""
TOTAL_VIOLATIONS=0
for mod in "${!MODULE_FILES[@]}"; do
  PMD_XML="$mod/target/pmd.xml"
  [[ -f "$PMD_XML" ]] || continue

  IFS=',' read -ra mod_files <<< "${MODULE_FILES[$mod]}"

  # filter_pmd_xml 首行是违规数，之后是 XML 段
  FILTERED="$(filter_pmd_xml "$PMD_XML" "$mod" "${mod_files[@]}")"
  MOD_COUNT="$(printf '%s' "$FILTERED" | head -n1)"
  MOD_BODY="$(printf '%s' "$FILTERED" | tail -n +2)"

  MOD_COUNT="${MOD_COUNT:-0}"
  if (( MOD_COUNT > 0 )); then
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + MOD_COUNT))
    ALL_VIOLATIONS_XML+=$'\n===== '"${mod#$PROJECT_ROOT/}"$'/target/pmd.xml (filtered) =====\n'
    ALL_VIOLATIONS_XML+="$MOD_BODY"
    ALL_VIOLATIONS_XML+=$'\n'
  fi
done

# 无违规：清计数、清列表，放行（即便 spotless 分批曾失败，只要 PMD 无本轮违规也放行）
if (( TOTAL_VIOLATIONS == 0 )); then
  if (( SPOTLESS_FAILED == 1 )); then
    # 附警告给 AI 作最终回复的上下文，但不阻断本轮 Stop
    REASON="${SPOTLESS_WARN}本轮未发现 PMD 违规。"
    jq -n --arg r "$REASON" '{decision:"block", reason:$r}'
  fi
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 有违规：累加阻断计数
ATTEMPT="$(stop_block_inc)"

if (( ATTEMPT > STOP_MAX_ATTEMPTS )); then
  REASON=$(printf '%sPMD 在 %s 轮连续阻断后仍有 %s 处违规，已停止自动修复循环。请在最终回复中：\n1. 汇报卡住的违规规则\n2. 建议用户手动处理或调整 pom 中的 pmd ruleset 排除条目\n\n最后一次报告：\n%s' \
    "$SPOTLESS_WARN" "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
  jq -n --arg r "$REASON" '{decision:"block", reason:$r}'
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 未达上限：阻断，把违规反馈给 Claude 继续修
REASON=$(printf '%sPMD 检查未通过（第 %s/%s 轮），本轮编辑触发 %s 处违规，请逐条修复后再结束响应。修复完成后请在最终回复中简述修改了哪些规则项。\n\n完整 PMD 报告（已过滤到本轮编辑的文件）：\n%s' \
  "$SPOTLESS_WARN" "$ATTEMPT" "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
jq -n --arg r "$REASON" '{decision:"block", reason:$r}'

# 阻断时保留 edited-files 列表，让下轮 AI 修复后 Stop 再次读同一批文件复查
exit 0
