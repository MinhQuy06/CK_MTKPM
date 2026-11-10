# 06 — Observer: sự kiện đã đặt lịch + observer email

Status: ready-for-agent
Blocked by: 05

Thực hiện mục **Observer** của [spec](../spec.md).

## Việc

- Định nghĩa sự kiện "đã đặt lịch" mang lịch hẹn đã lưu.
- Dịch vụ đặt lịch phát sự kiện thay vì gọi trực tiếp dịch vụ email.
- Observer email nhận sự kiện và gọi dịch vụ thông báo.

## Tiêu chí hoàn thành

- [ ] Test qua API: đặt lịch thành công → đúng 1 bản tin xác nhận ở RabbitTemplate, sau commit.
- [ ] Dịch vụ đặt lịch không còn phụ thuộc dịch vụ email.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
