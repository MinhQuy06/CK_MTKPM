# 25 — Facade: hủy, xem chi tiết, danh sách

Status: ready-for-agent
Blocked by: 24

Thực hiện mục **Facade** của [spec](../spec.md).

## Việc

- Ba endpoint còn lại đi qua Facade; controller đặt lịch chỉ còn phụ thuộc Facade.

## Tiêu chí hoàn thành

- [ ] Controller đặt lịch không còn tiêm dịch vụ đặt lịch hay dịch vụ người dùng.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
