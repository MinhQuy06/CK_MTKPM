# 07 — Chế độ demo trên stack prod

Status: resolved

Yêu cầu bổ sung sau nghiệm thu (2026-11-11): bản production cần dữ liệu demo để trình diễn trong ngày; các kết nối thật tạm tắt; có tài liệu rõ chúng nằm ở đâu, cách dùng, cách hoạt động. MySQL tạm thời không cần user/mật khẩu.

## Comments

- Profile `demo` bật thêm trên `prod` (mặc định trong compose: `APP_PROFILES=prod,demo`). `DataInitializer` chạy ở `dev` hoặc `demo` (`@Order(1)`), `AdminBootstrap` `@Order(2)` nên tự bỏ qua khi đã có Admin mẫu.
- Email ở demo đi qua SMTP vào container Mailpit (`http://localhost:8025`). SMS tắt. `DemoModeGuard` không cho khởi động nếu demo mà SMTP trỏ ra ngoài hoặc bật SMS, vì dữ liệu mẫu có email/số trông như thật.
- MySQL: `MYSQL_ALLOW_EMPTY_PASSWORD`, app dùng `root` mật khẩu rỗng; MySQL không mở cổng ra máy.
- `scripts/demo-up.sh`: tự tạo `.env` (bí mật ngẫu nhiên) nếu chưa có, dựng stack, chờ sẵn sàng.
- Tài liệu: `docs/production.md` (mới), README, architecture.md, docs/README.md.
- Kiểm chứng: `mvn test` 202 chạy, 0 lỗi, 17 bỏ qua. `demo-up.sh` từ trạng thái sạch: 5 container healthy, profile `prod,demo`, Flyway V1, seed hoàn tất, Admin bootstrap bỏ qua. `bn.hoangminh@gmail.com` thấy giá Bảo hiểm; `prod-smoke.sh` (sửa số điện thoại để không trùng dữ liệu mẫu) qua, email đến Mailpit, `notification_log` có `EMAIL SENT` + `SMS SKIPPED_DISABLED`.
