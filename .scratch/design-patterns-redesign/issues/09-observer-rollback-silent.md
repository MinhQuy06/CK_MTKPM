# 09 — Observer: luồng thất bại không phát thông báo

Status: ready-for-agent
Blocked by: 08

Thực hiện mục **Observer** của [spec](../spec.md).

## Việc

- Kiểm tra các nhánh đặt lịch thất bại (hết ca, đổi giá, mất quyền hưởng) không để lại tác dụng phụ.

## Tiêu chí hoàn thành

- [ ] Test qua API: mỗi nhánh thất bại → không có bản tin ở RabbitTemplate.
