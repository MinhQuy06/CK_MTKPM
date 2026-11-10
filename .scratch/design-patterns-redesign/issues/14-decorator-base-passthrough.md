# 14 — Decorator: lớp bọc trừu tượng + chuỗi rỗng

Status: ready-for-agent
Blocked by: 05

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Lớp bọc trừu tượng cài đặt giao diện dịch vụ thông báo, giữ thành phần được bọc.
- Lớp cấu hình ghép chuỗi (tạm chỉ có email gốc), đánh dấu bean chính.

## Tiêu chí hoàn thành

- [ ] Mọi nơi tiêm dịch vụ thông báo nhận bản bọc; hành vi không đổi.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
