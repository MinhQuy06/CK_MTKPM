# 08 — Observer: sự kiện đã hủy lịch

Status: ready-for-agent
Blocked by: 07

Thực hiện mục **Observer** của [spec](../spec.md).

## Việc

- Định nghĩa sự kiện "đã hủy lịch".
- Dịch vụ hủy lịch phát sự kiện; observer email và observer cache phản ứng.
- Dịch vụ đặt lịch không còn phụ thuộc dịch vụ cache.

## Tiêu chí hoàn thành

- [ ] Test qua API: hủy lịch → 1 bản tin hủy + 1 lần xóa cache đúng bác sĩ + ngày.
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
