# 16 — Decorator: chống gửi trùng

Status: ready-for-agent
Blocked by: 15

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Đã có dòng cùng loại + mã lịch hẹn với kết quả SENT → bỏ qua và ghi SKIPPED_DUPLICATE.

## Tiêu chí hoàn thành

- [ ] Test qua bean dịch vụ thông báo: gọi nhắc lịch 2 lần cùng lịch hẹn → 1 bản tin ở RabbitTemplate, 2 dòng log (SENT, SKIPPED_DUPLICATE).
