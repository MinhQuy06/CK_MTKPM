# 17 — Decorator: gửi thêm SMS qua Adapter

Status: ready-for-agent
Blocked by: 15, 12, 11

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Decorator gọi email bên trong trước, rồi gửi SMS qua kênh Adapter.
- Ghi dòng log kênh SMS với kết quả SENT / SKIPPED_NO_PHONE / FAILED.

## Tiêu chí hoàn thành

- [ ] Test qua API: bệnh nhân có số đặt lịch → client SMS giả lập nhận 1 tin, log có dòng SMS/SENT.
- [ ] Test qua API: bệnh nhân không có số → không có tin SMS, log SMS/SKIPPED_NO_PHONE, email vẫn gửi.
