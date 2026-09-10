#!/usr/bin/env bash
# Stop hook（Claude Stop / Cursor stop 共用）：回合结束时对本轮 AI 改过的 .java 做 spotless + pmd
# 协议：
#   - stdout JSON {"decision":"block","reason":"...","followup_message":"..."}
#     Claude 读 decision/reason；Cursor 原生 hooks.json 读 followup_message
#   - stdout 无内容 / exit 0 → 正常结束
#   - Cursor status 为 aborted/error 时跳过检查
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

# 读 stdin：Cursor stop 带 status；Claude Stop 通常无 status，忽略即可
INPUT="$(cat || true)"
if [[ -n "$INPUT" ]] && command -v jq >/dev/null 2>&1; then
  STOP_STATUS="$(printf '%s' "$INPUT" | jq -r '.status // empty' 2>/dev/null || true)"
  if [[ "$STOP_STATUS" == "aborted" || "$STOP_STATUS" == "error" ]]; then
    exit 0
  fi
fi

emit_stop_block() {
  local reason="$1"
  jq -n --arg r "$reason" '{decision:"block", reason:$r, followup_message:$r}'
}

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
{"decision":"block","reason":"Stop hook 缺少依赖 jq，PMD 检查未执行。请安装后重试：\n  macOS: brew install jq\n  Ubuntu/Debian: apt install jq\n  Windows: winget install jqlang.jq","followup_message":"Stop hook 缺少依赖 jq，PMD 检查未执行。请安装后重试：\n  macOS: brew install jq\n  Ubuntu/Debian: apt install jq\n  Windows: winget install jqlang.jq"}
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
  emit_stop_block "$REASON"
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 汇总各模块的 target/pmd.xml，按 edited files 过滤后统计违规 + 提取指纹
ALL_VIOLATIONS_XML=""
TOTAL_VIOLATIONS=0
CURRENT_FINGERPRINTS=""
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

    # 提取违规指纹用于停滞检测（基于过滤后的内容，只含本轮编辑文件的违规）
    mod_fps="$(printf '%s' "$MOD_BODY" | extract_violation_fingerprints - "$mod")"
    if [[ -n "$mod_fps" ]]; then
      if [[ -n "$CURRENT_FINGERPRINTS" ]]; then
        CURRENT_FINGERPRINTS+=$'\n'
      fi
      CURRENT_FINGERPRINTS+="$mod_fps"
    fi
  fi
done

# 对当前指纹排序去重
if [[ -n "$CURRENT_FINGERPRINTS" ]]; then
  CURRENT_FINGERPRINTS="$(printf '%s\n' "$CURRENT_FINGERPRINTS" | sort -u)"
fi

# 无违规：清计数、清列表、清指纹，放行
if (( TOTAL_VIOLATIONS == 0 )); then
  if (( SPOTLESS_FAILED == 1 )); then
    REASON="${SPOTLESS_WARN}本轮未发现 PMD 违规。"
    emit_stop_block "$REASON"
  fi
  clear_edited_list
  stop_block_reset
  clear_fingerprints
  exit 0
fi

# 有违规：加载上一轮指纹，做停滞检测分类
PREV_FINGERPRINTS="$(load_prev_fingerprints)"

# 分类：对比当前指纹与上一轮指纹
# 停滞 = 本轮存在 & 上轮也存在（AI 修过一轮仍没消除）
# 新增 = 本轮存在 & 上轮不存在
# 已修 = 上轮存在 & 本轮不存在（本轮已消除，不参与阻断）
STAGNANT_FINGERPRINTS=""
NEW_FINGERPRINTS=""
STAGNANT_COUNT=0
NEW_COUNT=0

if [[ -n "$PREV_FINGERPRINTS" ]]; then
  # 上一轮有历史 → 做对比
  # 停滞：current ∩ prev
  STAGNANT_FINGERPRINTS="$(comm -12 <(printf '%s\n' "$CURRENT_FINGERPRINTS") <(printf '%s\n' "$PREV_FINGERPRINTS"))"
  # 新增：current - prev
  NEW_FINGERPRINTS="$(comm -23 <(printf '%s\n' "$CURRENT_FINGERPRINTS") <(printf '%s\n' "$PREV_FINGERPRINTS"))"

  STAGNANT_COUNT="$(echo "$STAGNANT_FINGERPRINTS" | grep -c . || true)"
  STAGNANT_COUNT="${STAGNANT_COUNT:-0}"
  NEW_COUNT="$(echo "$NEW_FINGERPRINTS" | grep -c . || true)"
  NEW_COUNT="${NEW_COUNT:-0}"
else
  # 首轮（无历史）→ 全部视为新增
  NEW_FINGERPRINTS="$CURRENT_FINGERPRINTS"
  NEW_COUNT="$(echo "$NEW_FINGERPRINTS" | grep -c . || true)"
  NEW_COUNT="${NEW_COUNT:-0}"
  STAGNANT_COUNT=0
fi

# 保存本轮指纹（无论后续阻断还是释放，都需要记录）
save_fingerprints "$CURRENT_FINGERPRINTS"

# 累加阻断计数
ATTEMPT="$(stop_block_inc)"

# ---- 决策逻辑 ----

# 仅剩停滞违规 → 释放，报告给用户
if (( NEW_COUNT == 0 && STAGNANT_COUNT > 0 )); then
  STAGNANT_LIST="$(printf '%s\n' "$STAGNANT_FINGERPRINTS" | sed 's/^/  - /')"
  REASON=$(printf '%sPMD 检查完成：已修复可自动处理的违规，剩余 %s 处 AI 无法自动修复的违规：\n%s\n\n请在最终回复中向用户汇报以上无法修复的违规，并说明原因。建议用户采取以下措施之一：\n  - 手动修复代码\n  - 调整 pom 中 pmd ruleset 排除条目\n  - 由用户决定是否添加 @SuppressWarnings（AI 不得自行添加）' \
    "$SPOTLESS_WARN" "$STAGNANT_COUNT" "$STAGNANT_LIST")
  emit_stop_block "$REASON"
  clear_edited_list
  stop_block_reset
  clear_fingerprints
  exit 0
fi

# 达到阻断上限 → 释放，报告全部违规给用户
if (( ATTEMPT > STOP_MAX_ATTEMPTS )); then
  REASON=$(printf '%sPMD 在 %s 轮阻断后仍有 %s 处违规，已停止自动修复循环。请在最终回复中向用户汇报：\n1. 哪些规则项无法自动修复\n2. 建议用户手动处理、调整 pmd ruleset 排除条目、或由用户决定是否添加 @SuppressWarnings\n\n最后一次报告：\n%s' \
    "$SPOTLESS_WARN" "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
  emit_stop_block "$REASON"
  clear_edited_list
  stop_block_reset
  clear_fingerprints
  exit 0
fi

# 有新增违规 → 阻断，让 AI 专注修新增的，跳过停滞的
if (( NEW_COUNT > 0 )); then
  if (( STAGNANT_COUNT > 0 )); then
    STAGNANT_LIST="$(printf '%s\n' "$STAGNANT_FINGERPRINTS" | sed 's/^/  - /')"
    REASON=$(printf '%sPMD 检查未通过（第 %s/%s 轮）。本轮 %s 处新增违规需修复，%s 处停滞违规已跳过（AI 上轮未能修复）。\n\n停滞违规（请跳过，不要修这些）：\n%s\n\n新增违规（请逐条修复）：\n%s\n\n注意：禁止添加 @SuppressWarnings 压制违规。修不了的跳过，不要强行压制。' \
      "$SPOTLESS_WARN" "$ATTEMPT" "$STOP_MAX_ATTEMPTS" "$NEW_COUNT" "$STAGNANT_COUNT" "$STAGNANT_LIST" "$ALL_VIOLATIONS_XML")
  else
    # 首轮或无停滞 → 全部是新增，正常阻断
    REASON=$(printf '%sPMD 检查未通过（第 %s/%s 轮），本轮编辑触发 %s 处违规，请逐条修复后再结束响应。\n\n注意：禁止添加 @SuppressWarnings 压制违规。修不了的跳过，最后会交给用户决策。\n\n完整 PMD 报告（已过滤到本轮编辑的文件）：\n%s' \
      "$SPOTLESS_WARN" "$ATTEMPT" "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
  fi
  emit_stop_block "$REASON"
  # 阻断时保留 edited-files 列表，让下轮 AI 修复后 Stop 再次读同一批文件复查
  exit 0
fi
