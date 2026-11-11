#!/usr/bin/env bash
# Kiểm tra hai bản giao diện giống hệt nhau:
#   frontend/                              — bản để sửa và chạy test Node/Playwright
#   backend/src/main/resources/static/     — bản Spring Boot phục vụ (và đóng vào Docker)
# Bỏ qua những gì chỉ thuộc frontend/: tests/, node_modules/, package.json, package-lock.json.
#
#   ./scripts/check-frontend-sync.sh          # báo lệch, mã thoát 1 nếu lệch
#   ./scripts/check-frontend-sync.sh --fix    # chép frontend/ → static/ rồi kiểm tra lại
set -euo pipefail
cd "$(dirname "$0")/.."
SRC=frontend
DST=backend/src/main/resources/static
EXCLUDES=(-x tests -x node_modules -x package.json -x package-lock.json)

if [ "${1:-}" = "--fix" ]; then
  rsync -a --delete --exclude tests --exclude node_modules --exclude package.json --exclude package-lock.json "$SRC/" "$DST/"
  echo "[sync] Đã chép $SRC/ → $DST/"
fi

if diff -rq "${EXCLUDES[@]}" "$SRC" "$DST"; then
  echo "[sync] OK: $SRC/ và $DST/ giống nhau."
else
  echo "[sync] LỆCH: sửa ở $SRC/ rồi chạy '$0 --fix'." >&2
  exit 1
fi
