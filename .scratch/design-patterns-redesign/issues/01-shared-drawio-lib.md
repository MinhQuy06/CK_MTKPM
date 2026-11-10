# 01 — Tách thư viện draw.io dùng chung

Status: ready-for-agent

Thực hiện mục **Sơ đồ và tài liệu** của [spec](../spec.md).

## Việc

- Chuyển lớp vẽ draw.io của Strategy thành thư viện dùng chung, nhận thư mục đầu ra làm tham số.
- Ba script sinh sơ đồ Strategy dùng thư viện mới.
- Script xuất PNG nhận thư mục sơ đồ làm tham số.

## Tiêu chí hoàn thành

- [ ] Chạy lại ba script Strategy cho ra file `.drawio` giống hệt bản đang commit (`git diff` rỗng).
- [ ] Có hướng dẫn chạy ngắn ở đầu thư viện.
