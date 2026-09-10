#!/usr/bin/env bash
# SessionStart / sessionStart hook（Claude / Cursor 共用）：清理上次会话残留的 edited-files 列表和 stop 阻断计数
# 防止用户 Ctrl+C 打断上次会话后，本次 Stop 处理到过期文件路径

set -uo pipefail

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "$HOOK_DIR/lib/common.sh"

# 消费 stdin
cat >/dev/null || true

clear_edited_list
stop_block_reset
clear_fingerprints
exit 0
