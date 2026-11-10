# 05 — Builder: Director cho đặt lịch, hủy, nhắc lịch

Status: ready-for-agent
Blocked by: 03

Thực hiện mục **Builder** của [spec](../spec.md).

## Việc

- Director nhận lịch hẹn, trả bản tin cho 3 trường hợp: đặt lịch, hủy, nhắc lịch.
- Dịch vụ gửi email chỉ gọi Director, không tự gọi builder.

## Tiêu chí hoàn thành

- [ ] Test qua API: hủy lịch → bản tin loại hủy đúng các trường.
- [ ] Test đơn vị: bản tin nhắc lịch có loại nhắc lịch.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
