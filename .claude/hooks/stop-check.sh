#!/usr/bin/env bash
# Claude Code Stop hook: 回合结束时统一跑 spotless + pmd
# 协议：
#   - stdout JSON {"decision":"block","reason":"..."} → Claude 继续响应并把 reason 作为上下文
#   - stdout 无内容 / exit 0 → 正常结束
# 失败不阻断（异常时打印 stderr 警告，正常 exit 0）

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

# 读列表并分组；若为空说明本轮没改 .java，直接放行
GROUPED="$(edited_files_grouped)"
if [[ -z "$GROUPED" ]]; then
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 本轮确实要跑 PMD 并输出 block JSON，此时才强依赖 jq
# 缺 jq 时输出静态 JSON（reason 内容固定，无用户数据，无需转义）响亮告知用户
if ! command -v jq >/dev/null 2>&1; then
  cat <<'JSON'
{"decision":"block","reason":"Stop hook 缺少依赖 jq，PMD 检查未执行。请安装后重试：\n  macOS: brew install jq\n  Ubuntu/Debian: apt install jq\n  Windows: winget install jqlang.jq"}
JSON
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 收集所有涉及的模块（相对项目根的路径），供 -pl 使用
MODULES=()
declare -A MODULE_FILES
while IFS=$'\t' read -r mod files; do
  [[ -z "$mod" ]] && continue
  rel="${mod#$PROJECT_ROOT/}"
  if [[ "$rel" == "$mod" ]]; then rel="."; fi
  MODULES+=("$rel")
  MODULE_FILES["$mod"]="$files"
done <<< "$GROUPED"

# 拼 -pl a,b,c
PL_ARG="$(IFS=','; echo "${MODULES[*]}")"

# 一次 mvn 双 goal：先 spotless:apply 改写，再 pmd:pmd 生成 target/pmd.xml
# 使用 pmd:pmd 而不是 pmd:check：不因违规而 fail build，我们自己读 XML 决定是否阻断
# -fae：fail-at-end，保证所有模块都跑完
MVN_STDERR="$(mktemp)"
trap 'rm -f "$MVN_STDERR"' EXIT
MVN_RC=0
(
  cd "$PROJECT_ROOT" && \
  mvn -pl "$PL_ARG" -q -B -fae spotless:apply pmd:pmd
) 2>"$MVN_STDERR" || MVN_RC=$?

if (( MVN_RC != 0 )); then
  ERR_TAIL="$(tail -n 40 "$MVN_STDERR")"
  REASON=$(printf 'Stop hook 执行 mvn 失败（退出码 %s）。请在最终回复中提醒用户检查 pom.xml 是否已注入 spotless-maven-plugin / maven-pmd-plugin。\n\nmvn stderr 尾部：\n%s' "$MVN_RC" "$ERR_TAIL")
  jq -n --arg r "$REASON" '{decision:"block", reason:$r}'
  # 出错也清列表，避免下轮反复卡在同一批文件上
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 汇总各模块的 target/pmd.xml，仅保留本轮编辑文件的违规
ALL_VIOLATIONS_XML=""
TOTAL_VIOLATIONS=0
for mod in "${!MODULE_FILES[@]}"; do
  PMD_XML="$mod/target/pmd.xml"
  [[ -f "$PMD_XML" ]] || continue

  # 提取属于本模块 edited files 的 <file> 段
  # 简单做法：把整份 XML 塞进去；同时统计违规数
  # 违规数 = <violation 出现次数
  vio_count="$(grep -c '<violation' "$PMD_XML" || true)"
  vio_count="${vio_count:-0}"
  if (( vio_count > 0 )); then
    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + vio_count))
    ALL_VIOLATIONS_XML+=$'\n===== '"${mod#$PROJECT_ROOT/}"$'/target/pmd.xml =====\n'
    ALL_VIOLATIONS_XML+="$(cat "$PMD_XML")"
    ALL_VIOLATIONS_XML+=$'\n'
  fi
done

# 无违规：清计数、清列表，放行
if (( TOTAL_VIOLATIONS == 0 )); then
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 有违规：先累加阻断计数
ATTEMPT="$(stop_block_inc)"

if (( ATTEMPT > STOP_MAX_ATTEMPTS )); then
  # 达到上限：本轮不阻断，重置计数、清列表，附一段警告 reason
  # Stop hook 若已允许结束，reason 仅作为最终回复附带上下文
  REASON=$(printf 'PMD 在 %s 轮连续阻断后仍有 %s 处违规，已停止自动修复循环。请在最终回复中：\n1. 汇报卡住的违规规则\n2. 建议用户手动处理或调整 pom 中的 pmd ruleset 排除条目\n\n最后一次报告：\n%s' \
    "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
  jq -n --arg r "$REASON" '{decision:"block", reason:$r}'
  clear_edited_list
  stop_block_reset
  exit 0
fi

# 未达上限：阻断，把违规反馈给 Claude 继续修
REASON=$(printf 'PMD 检查未通过（第 %s/%s 轮），本轮编辑触发 %s 处违规，请逐条修复后再结束响应。修复完成后请在最终回复中简述修改了哪些规则项。\n\n完整 PMD 报告：\n%s' \
  "$ATTEMPT" "$STOP_MAX_ATTEMPTS" "$TOTAL_VIOLATIONS" "$ALL_VIOLATIONS_XML")
jq -n --arg r "$REASON" '{decision:"block", reason:$r}'

# 阻断时保留 edited-files 列表，让下轮 AI 修复后 Stop 再次读同一批文件复查
# 不清 stop-block-count，让 STOP_MAX_ATTEMPTS 生效
exit 0
