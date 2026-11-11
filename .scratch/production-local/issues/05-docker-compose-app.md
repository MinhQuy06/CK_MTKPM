# 05 — Đóng gói app trong Docker Compose

Status: ready-for-agent

Blocked by: 01, 02, 03, 04

Thực hiện mục 6 của [spec](../spec.md).

- `Dockerfile` nhiều tầng cho backend; thêm service app vào `docker-compose.yml` với `depends_on` + healthcheck.
- Bỏ mật khẩu cứng `root`/`guest` khỏi compose, đọc từ `.env`; cập nhật `.env.example`.
- Giữ `run.sh`/`run.bat` cho chế độ dev.
- Xong khi: `docker compose up` từ trạng thái sạch → `/actuator/health` UP, đăng nhập admin được.
