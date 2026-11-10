#!/usr/bin/env bash
set -euo pipefail

# Project targets JDK 21; newer JDKs (e.g. 25+, 27) break Lombok's annotation processor.
java_major() { "$1/bin/java" -version 2>&1 | awk -F'"' '/version/ {split($2, v, "."); print v[1]; exit}'; }

if [ -z "${JAVA_HOME:-}" ] || [ "$(java_major "$JAVA_HOME" 2>/dev/null)" != "21" ]; then
  jdk21=""
  if [ -x /usr/libexec/java_home ]; then
    jdk21="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  fi
  if [ -z "$jdk21" ]; then
    for candidate in \
      "$HOME/Library/Application Support/Code/User/globalStorage/pleiades.java-extension-pack-jdk/java/21" \
      /usr/lib/jvm/java-21-openjdk* /usr/lib/jvm/temurin-21*; do
      if [ -x "$candidate/bin/java" ]; then jdk21="$candidate"; break; fi
    done
  fi
  if [ -n "$jdk21" ]; then
    export JAVA_HOME="$jdk21"
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo "[WARN] Khong tim thay JDK 21, dung Java mac dinh (co the loi Lombok)." >&2
  fi
fi

if command -v mvn >/dev/null 2>&1; then exec mvn "$@"; fi
for executable in "$HOME"/.vscode/extensions/oracle.oracle-java-*/nbcode/java/maven/bin/mvn.sh; do
  if [ -x "$executable" ]; then exec "$executable" "$@"; fi
done
echo "Maven 3.9+ and JDK 21 are required." >&2
exit 1
