#!/usr/bin/env bash
# -------------------------------------------------
#  DatLichKhamBenh – Khởi chạy backend (macOS/Linux)
#  Tương đương run.bat trên Windows.
#  Cách dùng:
#    docker compose up -d
#    ./run.sh
# -------------------------------------------------
set -euo pipefail

# Kill process dang dung port 8080 (neu co)
echo "[INFO] Kiem tra va giai phong port 8080..."
pids=$(lsof -ti tcp:8080 -sTCP:LISTEN 2>/dev/null || true)
for pid in $pids; do
  echo "[INFO] Kill PID $pid dang chiem port 8080"
  kill -9 "$pid" 2>/dev/null || true
done

# Chuyen toi thu muc backend
script_dir="$(cd "$(dirname "$0")" && pwd)"
cd "$script_dir/backend"

# Profile dev bat du lieu mau; production khong tu tao tai khoan mac dinh.
exec "$script_dir/scripts/maven.sh" clean spring-boot:run -Dspring-boot.run.profiles=dev
