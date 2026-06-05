# Agent skills

### Issue tracker

Issues are tracked as GitHub issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical labels: needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout. See `docs/agents/domain.md`.

# Project Rules

- 对话过程所有信息都用中文展示，git提交代码commit信息都用英文
- 提交代码的时候，本文件可以永远跟着一起提交
- SpringBoot 3版本不能强制依赖jackson 3，必须保证SpringBoot 3项目（未依赖jackson 3）依赖本组件时能正常启动
- 编写新功能的时候必须编写相关测试，单元测试规范
  - 单元测试工具只能使用junit，保证分支兼容性
  - 不用针对每个类编写对应的代码测试代码，而是编写集成测试代码即可（参考现在的测试代码编写），重点是要保证场景全覆盖
- 开启caveman模式，优先使用codegraph进行代码搜索分析
- 编写新功能的时候，一定要同步更新文档：README.md 和 README_EN.md。更新文档的时候把"功能特性"目录也更新一下
- 不要直接就写代码，永远要跟我确认方案没问题之后，再写代码