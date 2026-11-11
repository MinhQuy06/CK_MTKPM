#!/usr/bin/env bash
# Dựng stack production ở localhost cho buổi demo (hướng dẫn: docs/production.md).
#  - Chưa có .env: tạo từ .env.example với mật khẩu và JWT_SECRET ngẫu nhiên, chế độ prod,demo.
#  - Đã có .env: dùng nguyên, không ghi đè.
# Sau đó build, chạy và chờ ứng dụng sẵn sàng.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  rnd() { openssl rand -hex "$1"; }
  sed -e "s|^REDIS_PASSWORD=.*|REDIS_PASSWORD=$(rnd 16)|" \
      -e "s|^RABBITMQ_PASSWORD=.*|RABBITMQ_PASSWORD=$(rnd 16)|" \
      -e "s|^JWT_SECRET=.*|JWT_SECRET=$(rnd 64)|" \
      -e "s|^ADMIN_EMAIL=.*|ADMIN_EMAIL=quanly@clinic.local|" \
      -e "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=$(rnd 8)|" \
      .env.example > .env
  echo "[demo-up] Đã tạo .env (bí mật ngẫu nhiên, chế độ $(grep '^APP_PROFILES=' .env | cut -d= -f2))."
else
  echo "[demo-up] Dùng .env có sẵn (chế độ $(grep '^APP_PROFILES=' .env | cut -d= -f2 || echo 'prod,demo'))."
fi

docker compose -f docker-compose.prod.yml up -d --build

port="$(grep '^APP_PORT=' .env | cut -d= -f2)"; port="${port:-18081}"
mail_port="$(grep '^MAILPIT_UI_PORT=' .env | cut -d= -f2)"; mail_port="${mail_port:-8025}"
echo "[demo-up] Chờ ứng dụng sẵn sàng (lần đầu có thể mất vài phút để build)..."
for _ in $(seq 1 120); do
  if curl -fs "http://localhost:$port/actuator/health" | grep -q UP; then
    cat <<MSG

[demo-up] Sẵn sàng.
  Ứng dụng:   http://localhost:$port
  Hộp thư:    http://localhost:$mail_port   (mọi email của hệ thống ở chế độ demo)
  Tài khoản:  admin@clinic.vn / Admin@123, bs.nguyenvana@clinic.vn / Doctor@123,
              bn.hoangminh@gmail.com / Patient@123  (đủ danh sách: docs/production.md)
  Dừng:       docker compose -f docker-compose.prod.yml down      (thêm -v để xóa sạch dữ liệu)
MSG
    exit 0
  fi
  sleep 3
done
echo "[demo-up] Ứng dụng chưa lên sau 6 phút. Xem log: docker compose -f docker-compose.prod.yml logs app" >&2
exit 1
