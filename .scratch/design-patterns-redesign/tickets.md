# Ticket: Thiết kế lại hệ thống bằng 7 mẫu

Xem [spec](spec.md).

| # | Ticket | Blocked by |
|---|---|---|
| 01 | [Tách thư viện draw.io dùng chung](issues/01-shared-drawio-lib.md) | — |
| 02 | [Khung README tổng 7 mẫu](issues/02-overview-readme-skeleton.md) | — |
| 03 | [Builder: builder cho bản tin thông báo](issues/03-builder-message-builder.md) | — |
| 04 | [Builder: kiểm tra trường bắt buộc](issues/04-builder-required-fields.md) | 03 |
| 05 | [Builder: Director cho đặt lịch, hủy, nhắc lịch](issues/05-builder-director.md) | 03 |
| 06 | [Observer: sự kiện đã đặt lịch + observer email](issues/06-observer-booked-event-email.md) | 05 |
| 07 | [Observer: observer xóa cache slot khi đặt lịch](issues/07-observer-booked-cache.md) | 06 |
| 08 | [Observer: sự kiện đã hủy lịch](issues/08-observer-cancelled-event.md) | 07 |
| 09 | [Observer: luồng thất bại không phát thông báo](issues/09-observer-rollback-silent.md) | 08 |
| 10 | [Adapter: kênh thông báo + client SMS giả lập + adapter](issues/10-adapter-channel-sms.md) | — |
| 11 | [Adapter: chuẩn hóa số điện thoại Việt Nam](issues/11-adapter-phone-normalize.md) | 10 |
| 12 | [Adapter: bỏ qua bệnh nhân không có số](issues/12-adapter-skip-no-phone.md) | 10 |
| 13 | [Decorator: bảng notification_log](issues/13-decorator-notification-log-table.md) | — |
| 14 | [Decorator: lớp bọc trừu tượng + chuỗi rỗng](issues/14-decorator-base-passthrough.md) | 05 |
| 15 | [Decorator: ghi lịch sử thông báo](issues/15-decorator-audit-log.md) | 13, 14 |
| 16 | [Decorator: chống gửi trùng](issues/16-decorator-dedup.md) | 15 |
| 17 | [Decorator: gửi thêm SMS qua Adapter](issues/17-decorator-sms.md) | 15, 12, 11 |
| 18 | [Decorator: lỗi SMS không ảnh hưởng email và đặt lịch](issues/18-decorator-sms-failure-isolated.md) | 17 |
| 19 | [Decorator: chốt thứ tự chuỗi](issues/19-decorator-chain-order.md) | 16, 18 |
| 20 | [Factory: tạo tài khoản Bệnh nhân](issues/20-factory-patient.md) | — |
| 21 | [Factory: Bác sĩ tự đăng ký](issues/21-factory-doctor-register.md) | 20 |
| 22 | [Factory: Admin tạo Bác sĩ và Bệnh nhân](issues/22-factory-admin-create.md) | 21 |
| 23 | [Factory: vai trò không hợp lệ](issues/23-factory-invalid-role.md) | 20 |
| 24 | [Facade: xác nhận đặt lịch và tạo lịch cũ](issues/24-facade-confirm-create.md) | 09, 19 |
| 25 | [Facade: hủy, xem chi tiết, danh sách](issues/25-facade-cancel-read.md) | 24 |
| 26 | [Sơ đồ Builder (class, sequence, ERD, DFD)](issues/26-diagrams-builder.md) | 01, 05 |
| 27 | [Sơ đồ Observer](issues/27-diagrams-observer.md) | 01, 09 |
| 28 | [Sơ đồ Adapter](issues/28-diagrams-adapter.md) | 01, 12 |
| 29 | [Sơ đồ Decorator](issues/29-diagrams-decorator.md) | 01, 19 |
| 30 | [Sơ đồ Factory](issues/30-diagrams-factory.md) | 01, 22 |
| 31 | [Sơ đồ Facade](issues/31-diagrams-facade.md) | 01, 25 |
| 32 | [Chọn 4 sơ đồ chính của Strategy](issues/32-diagrams-strategy-pick.md) | — |
| 33 | [Bản đồ tổng thể 7 mẫu](issues/33-diagram-overview-map.md) | 26, 27, 28, 29, 30, 31 |
| 34 | [README + kịch bản Builder](issues/34-docs-builder.md) | 26 |
| 35 | [README + kịch bản Observer](issues/35-docs-observer.md) | 27 |
| 36 | [README + kịch bản Adapter](issues/36-docs-adapter.md) | 28 |
| 37 | [README + kịch bản Decorator](issues/37-docs-decorator.md) | 29 |
| 38 | [README + kịch bản Factory](issues/38-docs-factory.md) | 30 |
| 39 | [README + kịch bản Facade](issues/39-docs-facade.md) | 31 |
| 40 | [Chạy toàn bộ test và gộp vào main](issues/40-final-regression-merge.md) | 34, 35, 36, 37, 38, 39, 33, 32, 02, 23 |
