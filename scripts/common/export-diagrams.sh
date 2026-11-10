#!/usr/bin/env bash
# Export every .drawio in a diagrams folder to a PNG beside it.
# Usage: DRAWIO_BIN=/path/to/draw.io bash scripts/common/export-diagrams.sh docs/<pattern>/diagrams
set -euo pipefail
cd "$(dirname "$0")/../.."
dir="${1:?Usage: export-diagrams.sh docs/<pattern>/diagrams}"
drawio_bin="${DRAWIO_BIN:-/Applications/draw.io.app/Contents/MacOS/draw.io}"
if [ ! -x "$drawio_bin" ]; then echo "Set DRAWIO_BIN to the local draw.io Desktop executable." >&2; exit 1; fi
for source in "$dir"/*.drawio; do
  "$drawio_bin" --export --format png --scale 1.5 --border 25 --output "${source%.drawio}.png" "$source"
done
