# 10 — Adapter: kênh thông báo + client SMS giả lập + adapter

Status: ready-for-agent

Thực hiện mục **Adapter** của [spec](../spec.md).

## Việc

- Giao diện đích "kênh thông báo" trả kết quả gửi/bỏ qua/lỗi.
- Client SMS giả lập có chữ ký khác (số quốc tế + nội dung), chỉ ghi nhận tin.
- Adapter SMS giữ tham chiếu client (composition), chuyển bản tin thành nội dung SMS ngắn.

## Tiêu chí hoàn thành

- [ ] Test qua bean kênh thông báo: gửi 1 bản tin → client giả lập nhận đúng số + nội dung có mã lịch hẹn.
