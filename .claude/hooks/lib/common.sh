#!/usr/bin/env bash
# team-ai-coding-toolkit / Claude Code hooks core lib
# 被 record-edit.sh / stop-check.sh 及 /format、/pmd-check skill 共用
# 由 install.sh 复制到 .claude/hooks/lib/common.sh
#
# 注意：本脚本使用 bash 关联数组（declare -A）等特性，必须在 bash 下运行。
# skill 调用时需用 bash -c 包裹，不能在 zsh 中直接 source。

set -uo pipefail

# ============================================================
# JAVA_HOME 自动检测
# ============================================================

# 确保 JAVA_HOME 存在且 java 版本 ≥ 17
# 搜索优先级：
#   1. 已设置的 JAVA_HOME（若版本 ≥ 17）
#   2. macOS /usr/libexec/java_home（系统 JDK 管理接口）
#   3. 各平台 JDK 安装目录动态扫描（不硬编码版本号）
#   4. PATH 中 java 可执行文件推断
ensure_java_home() {
  # 1. 已设置的 JAVA_HOME 若版本 ≥ 17，直接用
  local current_java="${JAVA_HOME:-}"
  if [[ -n "$current_java" && -x "$current_java/bin/java" ]]; then
    local ver
    ver="$("$current_java/bin/java" -version 2>&1 | head -1)"
    if echo "$ver" | grep -qE '(1[7-9]|[2-9][0-9])'; then
      return 0
    fi
  fi

  # 2. macOS: /usr/libexec/java_home 是标准 JDK 版本选择接口
  if [[ -x /usr/libexec/java_home ]]; then
    local mac_home
    mac_home="$(/usr/libexec/java_home -v 17+ 2>/dev/null || true)"
    if [[ -n "$mac_home" && -x "$mac_home/bin/java" ]]; then
      export JAVA_HOME="$mac_home"
      return 0
    fi
  fi

  # 3. 各平台 JDK 安装目录动态扫描
  local search_dirs=()
  case "$(uname -s)" in
    Darwin)
      for d in /Library/Java/JavaVirtualMachines/*.jdk/Contents/Home \
               "$HOME/Library/Java/JavaVirtualMachines/*.jdk/Contents/Home"; do
        if [[ -d "$d" && -x "$d/bin/java" ]]; then
          search_dirs+=("$d")
        fi
      done
      ;;
    Linux)
      for d in /usr/lib/jvm/java-*; do
        if [[ -d "$d" && -x "$d/bin/java" ]]; then
          search_dirs+=("$d")
        fi
      done
      if [[ -d "$HOME/.sdkman/candidates/java/current" && -x "$HOME/.sdkman/candidates/java/current/bin/java" ]]; then
        search_dirs+=("$HOME/.sdkman/candidates/java/current")
      fi
      ;;
    MINGW*|MSYS*|CYGWIN*)
      for d in "/c/Program Files/Java/jdk-*" \
               "/c/Program Files/Eclipse Adoptium/jdk-*" \
               "/c/Program Files/AdoptOpenJDK/jdk-*" \
               "/c/Program Files/Microsoft/jdk-*"; do
        if [[ -d "$d" && -x "$d/bin/java.exe" ]]; then
          search_dirs+=("$d")
        fi
      done
      ;;
  esac

  for d in "${search_dirs[@]:-}"; do
    if [[ -z "$d" ]]; then continue; fi
    local d_ver
    d_ver="$("$d/bin/java" -version 2>&1 | head -1)"
    if echo "$d_ver" | grep -qE '(1[7-9]|[2-9][0-9])'; then
      export JAVA_HOME="$d"
      return 0
    fi
  done

  # 4. PATH 中 java 推断
  local java_in_path
  java_in_path="$(command -v java 2>/dev/null)"
  if [[ -n "$java_in_path" ]]; then
    local path_ver
    path_ver="$(java -version 2>&1 | head -1)"
    if echo "$path_ver" | grep -qE '(1[7-9]|[2-9][0-9])'; then
      local inferred
      inferred="$(dirname "$(dirname "$java_in_path")")"
      if [[ -d "$inferred/bin" && -x "$inferred/bin/java" ]]; then
        export JAVA_HOME="$inferred"
        return 0
      fi
    fi
  fi
  return 1
}

# ============================================================
# 依赖检测
# ============================================================

require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "✗ 缺少依赖命令: $cmd" >&2
    return 1
  fi
}

# ============================================================
# 路径解析
# ============================================================

find_project_root() {
  git rev-parse --show-toplevel 2>/dev/null
}

# 向上查找最近的 pom.xml，返回所在目录绝对路径
find_module_root() {
  local file="$1"
  local dir
  if [[ -f "$file" ]]; then
    dir="$(cd "$(dirname "$file")" && pwd)"
  elif [[ -d "$file" ]]; then
    dir="$(cd "$file" && pwd)"
  else
    return 1
  fi
  while [[ "$dir" != "/" ]]; do
    if [[ -f "$dir/pom.xml" ]]; then
      echo "$dir"
      return 0
    fi
    dir="$(dirname "$dir")"
  done
  return 1
}

# ============================================================
# 文件收集
# ============================================================

# 输入：目录或单个 .java 文件路径
# 输出：换行分隔的 .java 文件绝对路径
collect_java_files() {
  local target="${1:-.}"
  if [[ ! -e "$target" ]]; then
    echo "✗ 路径不存在: $target" >&2
    return 1
  fi
  local abs
  abs="$(cd "$(dirname "$target")" && pwd)/$(basename "$target")"
  if [[ -f "$abs" ]]; then
    if [[ "$abs" == *.java ]]; then
      echo "$abs"
    fi
    return 0
  fi
  # 目录
  find "$abs" -type f -name "*.java" 2>/dev/null
}

# ============================================================
# 已编辑文件列表（PostToolUse record-edit.sh 写入，Stop hook 消费）
# ============================================================

_edited_list_file() {
  local root
  root="$(find_project_root)" || return 1
  echo "$root/.claude/hooks/.edited-files.txt"
}

# 读列表并规范化：去重、过滤不存在文件、按模块分组输出
# stdout: 逐行 "<module_root>\t<file1>,<file2>,..."
edited_files_grouped() {
  local list
  list="$(_edited_list_file)" || return 1
  [[ -f "$list" ]] || return 0

  declare -A groups
  local f mod seen_line
  declare -A seen
  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    [[ -n "${seen[$f]:-}" ]] && continue
    seen[$f]=1
    [[ -f "$f" ]] || continue
    mod="$(find_module_root "$f")" || continue
    if [[ -n "${groups[$mod]:-}" ]]; then
      groups[$mod]="${groups[$mod]},$f"
    else
      groups[$mod]="$f"
    fi
  done < "$list"

  for mod in "${!groups[@]}"; do
    printf '%s\t%s\n' "$mod" "${groups[$mod]}"
  done
}

clear_edited_list() {
  local list
  list="$(_edited_list_file)" || return 0
  if [[ -f "$list" ]]; then : > "$list"; fi
}

# ============================================================
# Stop 阻断计数（.claude/hooks/.stop-block-count）
# ============================================================

_stop_block_file() {
  local root
  root="$(find_project_root)" || return 1
  echo "$root/.claude/hooks/.stop-block-count"
}

stop_block_get() {
  local f
  f="$(_stop_block_file)" || return 1
  [[ -f "$f" ]] || { echo 0; return 0; }
  cat "$f"
}

stop_block_inc() {
  local f cur new
  f="$(_stop_block_file)" || return 1
  mkdir -p "$(dirname "$f")"
  cur="$(stop_block_get)"
  new=$((cur + 1))
  echo "$new" > "$f"
  echo "$new"
}

stop_block_reset() {
  local f
  f="$(_stop_block_file)" || return 0
  if [[ -f "$f" ]]; then rm -f "$f"; fi
}
