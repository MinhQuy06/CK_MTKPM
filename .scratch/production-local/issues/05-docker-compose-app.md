# 05 — Đóng gói app trong Docker Compose

Status: resolved

Blocked by: 01, 02, 03, 04

Thực hiện mục 6 của [spec](../spec.md).

- `Dockerfile` nhiều tầng cho backend; thêm service app vào `docker-compose.yml` với `depends_on` + healthcheck.
- Bỏ mật khẩu cứng `root`/`guest` khỏi compose, đọc từ `.env`; cập nhật `.env.example`.
- Giữ `run.sh`/`run.bat` cho chế độ dev.
- Xong khi: `docker compose up` từ trạng thái sạch → `/actuator/health` UP, đăng nhập admin được.

## Comments

Hoàn tất trên nhánh `feat/production-local`.
- `backend/Dockerfile` nhiều tầng (Maven + JDK 21 build, JRE 21 chạy bằng user không phải root, profile `prod` mặc định), kèm `.dockerignore`.
- **Lệch so với issue**: không sửa `docker-compose.yml` mà thêm `docker-compose.prod.yml` (project `clinic-prod`), vì `docker-compose.yml` là hạ tầng cho `run.sh`/`run.bat` ở chế độ dev (mật khẩu mặc định, mở cổng DB/Redis ra máy); gộp chung sẽ phá cách chạy dev. Bản prod chỉ mở cổng của app (`APP_PORT`, mặc định 18081), MySQL/Redis/RabbitMQ nằm trong mạng nội bộ, có healthcheck và `depends_on: service_healthy`. Mọi bí mật đọc từ `.env`, thiếu biến nào thì `docker compose` báo lỗi ngay.
- `.env.example` ở thư mục gốc (mọi biến của prod, gồm SMS/email/admin đầu tiên); `.env` đã vào `.gitignore`.
- Sửa thêm: đường dẫn không tồn tại (kể cả actuator không mở) trả 404 thay vì 500 + log ERROR (`GlobalExceptionHandler`).
- Kiểm chứng từ trạng thái sạch: `docker compose -f docker-compose.prod.yml up -d --build` → 4 container healthy, Flyway áp dụng V1, tạo Admin, đăng nhập admin OK, `login.html` 200, `/actuator/health` UP, `/actuator/metrics` 404. Dựng lại app: dữ liệu giữ nguyên, Admin không tạo trùng. Đã `down -v` dọn sạch sau khi thử.
- Chưa làm: `run.sh`/`run.bat` giữ nguyên cho dev; hướng dẫn dùng nằm ở issue 06 (README).
