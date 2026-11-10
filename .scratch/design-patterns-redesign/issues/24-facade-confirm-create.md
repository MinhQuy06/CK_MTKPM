# 24 — Facade: xác nhận đặt lịch và tạo lịch cũ

Status: ready-for-agent
Blocked by: 09, 19

Thực hiện mục **Facade** của [spec](../spec.md).

## Việc

- Facade đặt lịch gom dịch vụ đặt lịch + dịch vụ người dùng hiện tại.
- Hai endpoint xác nhận đặt lịch và tạo lịch cũ đi qua Facade.

## Tiêu chí hoàn thành

- [ ] Hợp đồng kiểm thử đặt lịch Strategy pass nguyên vẹn.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
