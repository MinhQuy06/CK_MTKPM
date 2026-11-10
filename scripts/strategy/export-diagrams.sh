#!/usr/bin/env bash
set -euo pipefail
exec bash "$(dirname "$0")/../common/export-diagrams.sh" docs/strategy/diagrams
