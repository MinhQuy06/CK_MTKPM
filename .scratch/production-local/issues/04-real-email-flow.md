# 04 — Email thật qua RabbitMQ + Gmail SMTP

Status: resolved

Blocked by: 01

Thực hiện mục 5 của [spec](../spec.md).

- Thiếu `MAIL_USERNAME`/`MAIL_PASSWORD` ở `prod` thì fail-fast.
- Chạy thật: đặt lịch → message vào queue → `EmailQueueConsumer` gửi → thư đến hộp thư.
- Xong khi: một thư xác nhận thật được nhận; lỗi SMTP không làm hỏng việc đặt lịch (hành vi hiện có giữ nguyên).

## Comments

Hoàn tất trên nhánh `feat/production-local`.
- `EmailQueueConsumer` đặt người gửi từ `MAIL_FROM` (mặc định `MAIL_USERNAME`). `MailSettingsValidator` (prod): thiếu hoặc rỗng `MAIL_USERNAME`/`MAIL_PASSWORD` thì app không khởi động.
- Prod bật retry cho listener RabbitMQ: 3 lần (2s, 4s), hết lần thì bỏ tin, hàng đợi không bị kẹt, app vẫn UP, việc đặt lịch không bị ảnh hưởng (publisher đã tách bằng `AFTER_COMMIT` + `@Async` từ trước).
- Kiểm chứng đầu-cuối ở profile `prod`: gửi message vào RabbitMQ thật → consumer → SMTP thật qua giao thức SMTP tới Mailpit (container) → thư đến đúng người nhận, đúng người gửi, đúng tiêu đề. Tắt SMTP: thấy retry rồi "Retries exhausted", hàng đợi về 0.
- **Chưa thử với Gmail thật**: cần tài khoản Gmail riêng có bật xác minh 2 bước và App Password, đặt vào `.env`. Cấu hình `smtp.gmail.com:587` + STARTTLS đã có sẵn.
- Test mới: `EmailQueueConsumerTests` (3). `mvn test` toàn bộ: xem kết quả cuối lần chạy này.
- Issue 05: thêm `MAIL_*`, `RABBITMQ_*`, `REDIS_HOST` vào `.env.example` và compose.
