#!/usr/bin/env bash
# Claude Code PostToolUse hook: 仅记录被 Edit/Write/MultiEdit 触及的 .java 文件路径
# stdin: {"tool_name":"Edit","tool_input":{"file_path":"/abs/path/Foo.java",...}, ...}
# 行为：写入 .claude/hooks/.edited-files.txt（追加，不去重，Stop hook 时去重）
#      始终 exit 0，不阻塞后续 tool 调用

set -uo pipefail

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$HOOK_DIR/lib/common.sh"

require_cmd jq >&2 || exit 0

INPUT="$(cat || true)"
[[ -z "$INPUT" ]] && exit 0

FILE_PATH="$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')"
[[ -z "$FILE_PATH" ]] && exit 0
[[ "$FILE_PATH" != *.java ]] && exit 0

PROJECT_ROOT="$(find_project_root)" || exit 0
LIST_FILE="$PROJECT_ROOT/.claude/hooks/.edited-files.txt"
mkdir -p "$(dirname "$LIST_FILE")"
printf '%s\n' "$FILE_PATH" >> "$LIST_FILE"
exit 0
