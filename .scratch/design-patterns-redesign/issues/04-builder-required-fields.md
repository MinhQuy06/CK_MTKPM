# 04 — Builder: kiểm tra trường bắt buộc

Status: ready-for-agent
Blocked by: 03

Thực hiện mục **Builder** của [spec](../spec.md).

## Việc

- `build()` từ chối khi thiếu loại, email người nhận hoặc mã lịch hẹn.
- Giá được phép trống.

## Tiêu chí hoàn thành

- [ ] Test đơn vị: thiếu từng trường bắt buộc → lỗi rõ tên trường; thiếu giá → dựng được.
