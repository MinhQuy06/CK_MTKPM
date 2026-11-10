# 18 — Decorator: lỗi SMS không ảnh hưởng email và đặt lịch

Status: ready-for-agent
Blocked by: 17

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Client SMS ném lỗi → Decorator SMS ghi FAILED, không ném ra ngoài.

## Tiêu chí hoàn thành

- [ ] Test qua API với client SMS luôn lỗi: đặt lịch vẫn thành công, email vẫn gửi, log SMS/FAILED.
