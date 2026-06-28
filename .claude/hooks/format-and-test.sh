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
# 3. Ali P3C PMD 规则检查（仅扫描 src/main/java）
# ============================================================
PMD_OUTPUT=$(mvn pmd:check -q 2>&1)
PMD_EXIT=$?
if [ $PMD_EXIT -ne 0 ]; then
    echo '❌ Ali P3C PMD 检查未通过，模型将自动修复。' >&2

    # 优先解析 target/pmd.xml（结构化、信息全），失败回退到原始输出
    PMD_DETAIL=""
    if [ -f target/pmd.xml ]; then
        PMD_DETAIL=$(python3 - <<'PY'
import xml.etree.ElementTree as ET
import os, sys
try:
    tree = ET.parse('target/pmd.xml')
    root = tree.getroot()
    ns = {'p': root.tag.split('}')[0].strip('{')} if '}' in root.tag else {}
    files = root.findall('.//p:file', ns) if ns else root.findall('.//file')
    lines = []
    total = 0
    for f in files:
        path = f.get('name', '')
        rel = os.path.relpath(path) if path else path
        violations = f.findall('p:violation', ns) if ns else f.findall('violation')
        for v in violations:
            total += 1
            if total > 80:
                continue
            ln = v.get('beginline', '?')
            rule = v.get('rule', '?')
            msg = (v.text or '').strip().replace('\n', ' ')
            lines.append(f"{rel}:{ln} [{rule}] {msg}")
    header = f"共 {total} 条违规" + ("（仅显示前 80 条）" if total > 80 else "")
    print(header)
    print('\n'.join(lines))
except Exception as e:
    print(f"解析 target/pmd.xml 失败: {e}", file=sys.stderr)
    sys.exit(1)
PY
)
    fi
    if [ -z "$PMD_DETAIL" ]; then
        PMD_DETAIL=$(echo "$PMD_OUTPUT" | grep -nE "PMD Failure|\[WARNING\]|\[ERROR\]" | head -80)
    fi

    echo '--- PMD 违规摘要 ---' >&2
    echo "$PMD_DETAIL" | head -20 >&2

    REASON=$(cat <<EOF
Ali P3C PMD 规则检查未通过，请按以下违规清单修复代码（仅修业务代码，勿改测试）：

=== 违规清单（文件:行号 [规则名] 描述）===
$PMD_DETAIL

修复指引：
- 命名违规（Naming）：调整类/方法/常量名称使其符合驼峰/全大写下划线等约定。
- 并发违规（Concurrent）：禁用 Executors.newFixedThreadPool 等，改用显式 ThreadPoolExecutor；ThreadLocal 使用后必须 remove。
- 异常违规（Exception）：禁止吞异常、禁止 catch Throwable/Error；finally 不得 return；NPE 防御。
- 集合违规（Set）：subList 注意 ConcurrentModification；toArray 必须指定数组大小；Arrays.asList 返回的列表不可变。
- 注释违规（Comment）：public 方法须有 Javadoc；类/字段注释规范。
修完后会自动重跑 PMD + 测试验证。
EOF
)
    emit_block "$REASON"
    exit 0
fi
echo "✅ Ali P3C PMD 检查通过。" >&2

# ============================================================
# 4. 运行单元测试
# ============================================================
TEST_OUTPUT=$(mvn test 2>&1)

# ============================================================
# 5. 判断测试结果
# ============================================================
TEST_SUMMARY=$(echo "$TEST_OUTPUT" | grep -E "Tests run:|Failures:|Errors:|BUILD" | tail -8)

if echo "$TEST_SUMMARY" | grep -q "BUILD SUCCESS"; then
    # 测试通过：仅输出纯文本到 stdout，无 JSON
    echo '✅ 格式化完成 + 单元测试全部通过。'
    echo "$TEST_SUMMARY"
    exit 0
fi

# ============================================================
# 6. 测试失败：输出 {"decision":"block","reason":"..."} 让模型自动修复
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
