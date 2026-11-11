# 02 — Admin đầu tiên, bỏ seed ở prod

Status: resolved

Blocked by: 01

Thực hiện mục 3 của [spec](../spec.md).

- `DataInitializer` chỉ ở profile `dev` (đã có `@Profile("dev")`, kiểm tra lại).
- Runner `prod`: nếu chưa có admin thì tạo từ `ADMIN_EMAIL`/`ADMIN_PASSWORD` (BCrypt); thiếu biến thì fail-fast.
- Xong khi: `prod` DB trống → chỉ có đúng 1 admin, đăng nhập được, không có bác sĩ/bệnh nhân giả; chạy lại không tạo trùng.

## Comments

Hoàn tất trên nhánh `feat/production-local`.
- `AdminBootstrap` (`@Profile("prod")`): khi chưa có Admin đang hoạt động thì tạo từ `ADMIN_EMAIL`/`ADMIN_PASSWORD` (BCrypt); đã có Admin thì bỏ qua. Mật khẩu < 8 ký tự, email sai, hoặc email đã thuộc tài khoản không phải Admin thì app không khởi động. Hai biến mới nằm trong `application-prod.yml`, không có mặc định.
- `DataInitializer` giữ `@Profile("dev")`.
- Kiểm chứng: MySQL trống + prod → đúng 1 user ADMIN, 0 bác sĩ, đăng nhập được; chạy lại không tạo trùng. Thêm 4 test `AdminBootstrapTests`.
- Còn lại cho issue 05: thêm `ADMIN_EMAIL`/`ADMIN_PASSWORD` vào `.env.example` và compose.
