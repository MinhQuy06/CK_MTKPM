#!/usr/bin/env bash
set -euo pipefail
if command -v mvn >/dev/null 2>&1; then exec mvn "$@"; fi
for executable in "$HOME"/.vscode/extensions/oracle.oracle-java-*/nbcode/java/maven/bin/mvn.sh; do
  if [ -x "$executable" ]; then exec "$executable" "$@"; fi
done
echo "Maven 3.9+ and JDK 21 are required." >&2
exit 1
