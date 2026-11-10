# 13 — Decorator: bảng notification_log

Status: ready-for-agent

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Entity + repository cho lịch sử thông báo: loại, mã lịch hẹn, kênh, kết quả, ghi chú, thời điểm.
- Thêm bảng vào `schema.sql`.

## Tiêu chí hoàn thành

- [ ] Ứng dụng khởi động với profile test và dev; bảng được tạo.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
