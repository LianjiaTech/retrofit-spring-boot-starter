# Agent skills

### Issue tracker

Issues are tracked as GitHub issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical labels: needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout. See `docs/agents/domain.md`.

# Project Rules

- 本项目有两个分支需要长期维护：
  - master分支支持Java 17，SpringBoot 3及以上版本
  - 2.x分支支持Java 8，SpringBoot 1.4.2及以上版本，不支持SpringBoot 3及以上版本
- 所有的代码需要兼容2.x分支，即需要支持Java 8，SpringBoot 1.4.2，不能使用高于该版本的功能特性
- SpringBoot 3版本不能强制依赖jackson 3，必须保证SpringBoot 3项目（未依赖jackson 3）依赖本组件时能正常启动
- 编写新功能的时候必须编写相关测试，单元测试规范
  - 单元测试工具只能使用junit，保证分支兼容性
  - 不用针对每个类编写对应的代码测试代码，而是编写集成测试代码即可（参考现在的测试代码编写），重点是要保证场景全覆盖
- 开启caveman模式，优先使用codegraph进行代码搜索分析