# 02 — Admin đầu tiên, bỏ seed ở prod

Status: ready-for-agent

Blocked by: 01

Thực hiện mục 3 của [spec](../spec.md).

- `DataInitializer` chỉ ở profile `dev` (đã có `@Profile("dev")`, kiểm tra lại).
- Runner `prod`: nếu chưa có admin thì tạo từ `ADMIN_EMAIL`/`ADMIN_PASSWORD` (BCrypt); thiếu biến thì fail-fast.
- Xong khi: `prod` DB trống → chỉ có đúng 1 admin, đăng nhập được, không có bác sĩ/bệnh nhân giả; chạy lại không tạo trùng.
