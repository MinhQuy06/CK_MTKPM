# 04 — Email thật qua RabbitMQ + Gmail SMTP

Status: ready-for-agent

Blocked by: 01

Thực hiện mục 5 của [spec](../spec.md).

- Thiếu `MAIL_USERNAME`/`MAIL_PASSWORD` ở `prod` thì fail-fast.
- Chạy thật: đặt lịch → message vào queue → `EmailQueueConsumer` gửi → thư đến hộp thư.
- Xong khi: một thư xác nhận thật được nhận; lỗi SMTP không làm hỏng việc đặt lịch (hành vi hiện có giữ nguyên).
