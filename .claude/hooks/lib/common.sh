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

# 校验 java 可执行文件版本是否 ≥ 17
# 用 `version "17"` / `version "21"` 形式锚定，避免误匹配 `1.8.0_281`
_java_version_ge_17() {
  local java_bin="$1"
  [[ -x "$java_bin" ]] || return 1
  "$java_bin" -version 2>&1 | head -1 | grep -qE 'version "(1[7-9]|[2-9][0-9])'
}

# 确保 JAVA_HOME 存在且 java 版本 ≥ 17
# 探测优先级：
#   L1. 已设置的 JAVA_HOME（若版本 ≥ 17）—— 所有平台
#   L2. 平台专属：
#       - macOS: /usr/libexec/java_home -v 17+ → PATH 中 java 兜底（Homebrew 场景）
#       - Windows (Git Bash): 扫标准安装目录
#       - Linux: 不做探测，返回失败（由调用方给出明确提示）
ensure_java_home() {
  # L1: 已设置的 JAVA_HOME
  if [[ -n "${JAVA_HOME:-}" ]] && _java_version_ge_17 "$JAVA_HOME/bin/java"; then
    return 0
  fi

  case "$(uname -s)" in
    Darwin)
      # macOS 官方 JDK 版本选择接口
      if [[ -x /usr/libexec/java_home ]]; then
        local mac_home
        mac_home="$(/usr/libexec/java_home -v 17+ 2>/dev/null || true)"
        if [[ -n "$mac_home" ]] && _java_version_ge_17 "$mac_home/bin/java"; then
          export JAVA_HOME="$mac_home"
          return 0
        fi
      fi
      # Homebrew 兜底：从 PATH 里的 java 推断
      local java_in_path inferred
      java_in_path="$(command -v java 2>/dev/null || true)"
      if [[ -n "$java_in_path" ]]; then
        inferred="$(dirname "$(dirname "$java_in_path")")"
        if _java_version_ge_17 "$inferred/bin/java"; then
          export JAVA_HOME="$inferred"
          return 0
        fi
      fi
      ;;
    MINGW*|MSYS*|CYGWIN*)
      # Windows Git Bash：扫描主流 JDK 发行版的标准安装目录
      local base d
      for base in "/c/Program Files/Java" \
                  "/c/Program Files/Eclipse Adoptium" \
                  "/c/Program Files/Microsoft" \
                  "/c/Program Files/AdoptOpenJDK" \
                  "/c/Program Files/Amazon Corretto"; do
        [[ -d "$base" ]] || continue
        while IFS= read -r d; do
          if _java_version_ge_17 "$d/bin/java.exe"; then
            export JAVA_HOME="$d"
            return 0
          fi
        done < <(find "$base" -maxdepth 1 -mindepth 1 -type d 2>/dev/null)
      done
      ;;
  esac
  return 1
}

# 未找到 JDK 17+ 时输出平台相关的引导信息，供 hook / skill 复用
warn_missing_java_home() {
  case "$(uname -s)" in
    Linux)
      echo "✗ 未找到 Java 17+。Linux 环境未做自动探测，请手动 export JAVA_HOME 指向 JDK 17+ 安装目录后重启 Claude Code。" >&2
      ;;
    *)
      echo "✗ 未找到 Java 17+ 运行环境。请安装 JDK 17+ 或设置 JAVA_HOME。" >&2
      ;;
  esac
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

# ============================================================
# spotless / pmd 文件级过滤辅助
# ============================================================

# 将任意字符串转义为 Java Pattern 可用的字面量正则
# 覆盖：. \ ( ) [ ] { } * + ? ^ $ | / (斜杠不是元字符但一并转义无害且可避免解析歧义)
# stdin: 原文，stdout: 转义后
escape_regex() {
  local s="$1"
  # 顺序无关：反斜杠必须最先转，避免后续插入的 \ 又被转义
  s="${s//\\/\\\\}"
  s="${s//./\\.}"
  s="${s//(/\\(}"
  s="${s//)/\\)}"
  s="${s//\[/\\[}"
  s="${s//]/\\]}"
  s="${s//\{/\\{}"
  s="${s//\}/\\\}}"
  s="${s//\*/\\*}"
  s="${s//+/\\+}"
  s="${s//\?/\\?}"
  s="${s//^/\\^}"
  s="${s//\$/\\\$}"
  s="${s//|/\\|}"
  printf '%s' "$s"
}

# 按字符预算把已转义的路径切成多批
# 输入：BUDGET, 逐行读取待打包的（已转义）路径
# 输出：每行一批，路径以逗号分隔
# 用法：printf '%s\n' "${ESC[@]}" | build_spotless_batches 6000
build_spotless_batches() {
  local budget="${1:-6000}"
  local current="" current_len=0 add_len line
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" ]] && continue
    add_len=$(( ${#line} + 1 ))  # +1 为逗号分隔符
    if (( current_len + add_len > budget )) && [[ -n "$current" ]]; then
      printf '%s\n' "$current"
      current="$line"
      current_len=${#line}
    else
      if [[ -z "$current" ]]; then
        current="$line"
        current_len=${#line}
      else
        current="$current,$line"
        current_len=$(( current_len + add_len ))
      fi
    fi
  done
  [[ -n "$current" ]] && printf '%s\n' "$current"
  return 0
}

# 过滤 PMD XML，仅保留白名单文件的 <file> 段
# 参数：
#   $1 = pmd.xml 路径
#   $2 = 模块根绝对路径（用于把白名单文件转成相对路径以兼容 PMD 相对路径输出）
#   $3.. = 白名单文件绝对路径列表
# stdout：
#   第一行：过滤后 <violation> 数量
#   之后：过滤后的 XML 段拼接（每个保留的 <file>...</file> 之间原样输出）
# 返回值：始终 0（即便无匹配，也返回 0 并输出 "0" 计数 + 空正文）
filter_pmd_xml() {
  local xml="$1"; shift
  local mod="$1"; shift
  # 剩余参数为白名单
  local -a whitelist=("$@")

  if [[ ! -f "$xml" ]] || (( ${#whitelist[@]} == 0 )); then
    printf '0\n'
    return 0
  fi

  # 构造名字匹配的两组：绝对 & 相对模块根
  # macOS 的 /var 是 /private/var 的 symlink，mvn/PMD 可能报告任一形式；
  # 因此每个绝对路径同时插入 "/private" 前缀的孪生版本，保证匹配到位。
  local -a names=()
  local f rel twin
  for f in "${whitelist[@]}"; do
    names+=("$f")
    if [[ "$f" == /var/* ]]; then
      twin="/private$f"
      names+=("$twin")
    elif [[ "$f" == /private/var/* ]]; then
      twin="${f#/private}"
      names+=("$twin")
    fi
    rel="${f#$mod/}"
    if [[ "$rel" != "$f" ]]; then
      names+=("$rel")
    fi
  done

  awk -v NAMES="$(printf '%s\t' "${names[@]}")" '
    BEGIN {
      n = split(NAMES, arr, "\t")
      for (i = 1; i <= n; i++) {
        if (arr[i] != "") allow[arr[i]] = 1
      }
      inFile = 0
      keep = 0
      violations = 0
      body = ""
    }
    {
      line = $0
      # 逐行扫描；<file 可能带前置空白
      if (!inFile) {
        # 匹配 <file name="..."> （可能跨行属性极少见，此处按单行处理）
        if (match(line, /<file[[:space:]][^>]*>/)) {
          seg = substr(line, RSTART, RLENGTH)
          if (match(seg, /name="[^"]*"/)) {
            nm = substr(seg, RSTART + 6, RLENGTH - 7)
            keep = (nm in allow) ? 1 : 0
          } else {
            keep = 0
          }
          inFile = 1
          if (keep) body = body line "\n"
          # 处理同行即结束的情况：<file ...>...</file>
          if (index(line, "</file>")) {
            if (keep) {
              # 统计 violations
              tmp = line
              while (match(tmp, /<violation[[:space:]>]/)) {
                violations++
                tmp = substr(tmp, RSTART + RLENGTH)
              }
            }
            inFile = 0
            keep = 0
          }
        }
      } else {
        if (keep) {
          body = body line "\n"
          tmp = line
          while (match(tmp, /<violation[[:space:]>]/)) {
            violations++
            tmp = substr(tmp, RSTART + RLENGTH)
          }
        }
        if (index(line, "</file>")) {
          inFile = 0
          keep = 0
        }
      }
    }
    END {
      print violations
      printf "%s", body
    }
  ' "$xml"
}
