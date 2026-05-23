---
apply: always
---

- 本项目有两个分支需要长期维护：
  - master分支支持Java 17，SpringBoot 3及以上版本
  - 2.x分支支持Java 8，SpringBoot 1.4.2及以上版本，不支持SpringBoot 3及以上版本
- 所有的代码需要兼容2.x分支，即需要支持Java 8，SpringBoot 1.4.2，不能使用高于该版本的功能特性