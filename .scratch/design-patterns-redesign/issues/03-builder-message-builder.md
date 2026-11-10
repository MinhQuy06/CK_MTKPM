# 03 — Builder: builder cho bản tin thông báo

Status: ready-for-agent

Thực hiện mục **Builder** của [spec](../spec.md).

## Việc

- Viết tay builder cho bản tin thông báo 9 trường, hàm theo tên trường.
- Dịch vụ gửi email dựng bản tin bằng builder thay cho constructor 9 tham số.

## Tiêu chí hoàn thành

- [ ] Test qua API: đặt lịch thành công → bản tin bắt ở RabbitTemplate có đủ 9 trường đúng giá trị như trước.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
