# Phần 3.4 — Nhóm Behavioral Patterns

**Bản chính để đưa vào tiểu luận:** [BAO_CAO_3_4.md](BAO_CAO_3_4.md).

Folder này chỉ chứa phần Behavioral, làm lại theo nội dung đã thống nhất:

- **Observer:** đã có trong backend, phân tích sự kiện email và xử lý sau commit.
- **Strategy định giá:** thiết kế đề xuất; code demo độc lập với ba công thức tiêu chuẩn, bảo hiểm giả lập và VIP giả lập. Chưa tích hợp vào backend.

Mỗi mẫu có đủ bài toán/vị trí áp dụng, lý do chọn, vai trò, sơ đồ lớp, sơ đồ tuần tự, luồng xử lý, code, so sánh ưu/nhược điểm và đánh giá SOLID.

## Sơ đồ đã xuất thành ảnh

| Mẫu | Sơ đồ lớp UML | Sơ đồ tuần tự | File chỉnh sửa |
| --- | --- | --- | --- |
| Observer | [PNG](01-observer/Observer-class.png) | [PNG](01-observer/Observer-sequence.png) | [Observer.drawio](01-observer/Observer.drawio) |
| Strategy định giá | [PNG](02-strategy/Strategy-class.png) | [PNG](02-strategy/Strategy-sequence.png) | [Strategy.drawio](02-strategy/Strategy.drawio) |

Mỗi file Draw.io có hai trang **Class Diagram** và **Sequence Diagram**. Ảnh PNG được xuất ở tỷ lệ 2x và được chèn sẵn trong báo cáo.

## Code và kiểm chứng

- [Phụ lục mã Observer](01-observer/MA_NGUON.md); bản chụp các lớp nằm tại [01-observer/code](01-observer/code).
- [Code Strategy định giá](02-strategy/code): interface, ba strategy, context, mô hình dữ liệu demo và chương trình kiểm chứng.
- [Hướng dẫn và kết quả chạy](KIEM_CHUNG.md).

Các tỷ lệ giảm giá trong demo chỉ là giả định bài tập. Các record Doctor/Patient của demo không thêm cột hoặc thay entity của dự án.
