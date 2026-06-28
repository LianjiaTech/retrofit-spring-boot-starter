#!/bin/bash
# format-and-test.sh — AI修改代码后自动格式化(P3C) + 运行单元测试
# 适用分支：master（固定 JDK 17）
#
# 输出约定（Claude Code Stop hook）：
# - stdout（无 JSON）+ exit 0     → 测试通过，对话正常结束
# - stdout {"decision":"block","reason":"..."} + exit 0 → 阻止结束，模型根据 reason 自动修复
# - stdout {"continue":false,"stopReason":"..."} + exit 0 → 致命错误，完全停止
# - stderr 用于打印用户可见的辅助信息，不影响框架解析

set -u

# ============================================================
# JSON 输出辅助函数（避免 bash 拼接 JSON 的转义陷阱）
# ============================================================
emit_block() {  # 测试失败：阻止结束，模型自动修复
    python3 - "$1" <<'PY'
import json, sys
print(json.dumps({"decision": "block", "reason": sys.argv[1]}, ensure_ascii=False))
PY
}

emit_stop() {  # 致命错误：完全停止
    python3 - "$1" <<'PY'
import json, sys
print(json.dumps({"continue": False, "stopReason": sys.argv[1]}, ensure_ascii=False))
PY
}

# ============================================================
# 1. 强制使用 JDK 17（master 分支固定）
# ============================================================
if [ -x /usr/libexec/java_home ]; then
    JDK17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)
fi
if [ -z "${JDK17_HOME:-}" ] || [ ! -x "$JDK17_HOME/bin/java" ]; then
    emit_stop "未找到 JDK 17（master 分支需要 JDK 17 编译）。请安装 JDK 17。"
    exit 0
fi
export JAVA_HOME="$JDK17_HOME"
echo "ℹ️ 使用 JDK 17：$JAVA_HOME" >&2

# ============================================================
# 2. Ali P3C 格式化
# ============================================================
SPOTLESS_OUTPUT=$(mvn spotless:apply -q 2>&1)
if [ $? -ne 0 ]; then
    echo '⚠️ Spotless 格式化失败，详情如下：' >&2
    echo "$SPOTLESS_OUTPUT" | tail -30 >&2
    echo '⚠️ 跳过格式化，继续运行测试。' >&2
fi

# ============================================================
# 3. 运行单元测试
# ============================================================
TEST_OUTPUT=$(mvn test 2>&1)

# ============================================================
# 4. 判断测试结果
# ============================================================
TEST_SUMMARY=$(echo "$TEST_OUTPUT" | grep -E "Tests run:|Failures:|Errors:|BUILD" | tail -8)

if echo "$TEST_SUMMARY" | grep -q "BUILD SUCCESS"; then
    # 测试通过：仅输出纯文本到 stdout，无 JSON
    echo '✅ 格式化完成 + 单元测试全部通过。'
    echo "$TEST_SUMMARY"
    exit 0
fi

# ============================================================
# 5. 测试失败：输出 {"decision":"block","reason":"..."} 让模型自动修复
# ============================================================
FAIL_DETAIL=$(echo "$TEST_OUTPUT" | grep -nE \
    "<<< FAIL!|<<< ERROR!|Caused by:|Exception:|java\.lang\.|AssertionError|expected:|but was:|at com\.github\.lianjiatech|Tests run:.*Failure|Tests run:.*Error|\[ERROR\]" \
    | head -80)

echo '❌ 单元测试失败，模型将自动修复。' >&2
echo '--- 失败摘要 ---' >&2
echo "$TEST_SUMMARY" >&2

REASON=$(cat <<EOF
单元测试失败，请根据以下错误信息修复代码：

=== 测试摘要 ===
$TEST_SUMMARY

=== 失败详情（失败标记、异常、断言、项目代码堆栈）===
$FAIL_DETAIL

请定位失败测试，修复实现代码或测试代码，然后我会再次自动运行测试验证。
EOF
)

emit_block "$REASON"
exit 0
