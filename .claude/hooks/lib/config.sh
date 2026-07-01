#!/usr/bin/env bash
# team-ai-coding-toolkit hooks 通用配置文件
# 由 install.sh 复制到 .claude/hooks/lib/config.sh，每次 install --update 覆盖。
# 团队级修改请在 toolkit 仓库提 PR；项目级一次性调整请在本地修改并自担升级 diff 责任。

# Stop hook 阻断上限：连续在 Stop 阶段因 PMD 违规阻断 AI 的最大次数
# 超过后放行本轮 Stop，避免 AI 陷入"改-阻断-再改"死循环
STOP_MAX_ATTEMPTS=3
