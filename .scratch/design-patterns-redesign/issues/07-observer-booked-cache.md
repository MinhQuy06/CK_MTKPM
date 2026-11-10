# 07 — Observer: observer xóa cache slot khi đặt lịch

Status: ready-for-agent
Blocked by: 06

Thực hiện mục **Observer** của [spec](../spec.md).

## Việc

- Observer cache nhận sự kiện "đã đặt lịch" và xóa cache slot theo bác sĩ + ngày.
- Bỏ lời gọi xóa cache trực tiếp trong luồng đặt lịch.

## Tiêu chí hoàn thành

- [ ] Test qua API với dịch vụ cache ghi nhận: đặt lịch → 1 lần xóa đúng bác sĩ + ngày.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
