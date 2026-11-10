# 19 — Decorator: chốt thứ tự chuỗi

Status: ready-for-agent
Blocked by: 16, 18

Thực hiện mục **Decorator** của [spec](../spec.md).

## Việc

- Ghép đúng thứ tự: ghi lịch sử → chống trùng → SMS → email gốc ở lớp cấu hình.

## Tiêu chí hoàn thành

- [ ] Test qua bean: lần gửi trùng không phát sinh SMS lần hai (chống trùng đứng trước SMS).
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ.
