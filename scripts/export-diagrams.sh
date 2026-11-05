#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
drawio_bin="${DRAWIO_BIN:-/Applications/draw.io.app/Contents/MacOS/draw.io}"
if [ ! -x "$drawio_bin" ]; then echo "Set DRAWIO_BIN to the local draw.io Desktop executable." >&2; exit 1; fi
for source in docs/diagrams/*.drawio; do
  "$drawio_bin" --export --format png --scale 1.5 --border 25 --output "${source%.drawio}.png" "$source"
done
