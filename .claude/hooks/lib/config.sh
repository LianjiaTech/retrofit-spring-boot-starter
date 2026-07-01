#!/usr/bin/env bash
# team-ai-coding-toolkit hooks 通用配置文件
# 由 install.sh 复制到 .claude/hooks/lib/config.sh，每次 install --update 覆盖。
# 团队级修改请在 toolkit 仓库提 PR；项目级一次性调整请在本地修改并自担升级 diff 责任。

# Stop hook 阻断上限：连续在 Stop 阶段因 PMD 违规阻断 AI 的最大次数
# 超过后放行本轮 Stop，避免 AI 陷入"改-阻断-再改"死循环
STOP_MAX_ATTEMPTS=3

# spotless:apply 单次 mvn 命令允许的 -DspotlessFiles 字符预算
# 取值 6000 是为兼容 Windows cmd/CreateProcess 约 8KB 的命令行上限
# 超过时按字符累加切批，多次调用 spotless:apply
SPOTLESS_FILES_BUDGET=6000
