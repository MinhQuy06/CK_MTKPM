# Ticket: Thiết kế lại hệ thống bằng 7 mẫu

Spec: [spec.md](spec.md) · GitHub #21

| # | Ticket | GitHub | Blocked by |
|---|---|---|---|
| 01 | [Thư viện draw.io dùng chung](issues/01-lib.md) | #22 | — |
| 02 | [Khung README tổng 7 mẫu](issues/02-readme.md) | #23 | — |
| 03 | [Builder: builder cho bản tin thông báo](issues/03-b-builder.md) | #24 | — |
| 04 | [Builder: kiểm tra trường bắt buộc](issues/04-b-required.md) | #25 | 03 |
| 05 | [Builder: Director cho đặt lịch, hủy, nhắc lịch](issues/05-b-director.md) | #26 | 03 |
| 06 | [Observer: sự kiện đã đặt lịch + observer email](issues/06-o-booked.md) | #27 | — |
| 07 | [Observer: observer xóa cache slot khi đặt lịch](issues/07-o-cache.md) | #28 | 06 |
| 08 | [Observer: sự kiện đã hủy lịch](issues/08-o-cancel.md) | #29 | 07 |
| 09 | [Observer: luồng thất bại không phát thông báo](issues/09-o-rollback.md) | #30 | 08 |
| 10 | [Adapter: kênh thông báo + client SMS giả lập + adapter](issues/10-a-channel.md) | #31 | — |
| 11 | [Adapter: chuẩn hóa số điện thoại Việt Nam](issues/11-a-phone.md) | #32 | 10 |
| 12 | [Adapter: bỏ qua bệnh nhân không có số](issues/12-a-nophone.md) | #33 | 10 |
| 13 | [Decorator: lớp bọc trừu tượng + chuỗi rỗng](issues/13-d-base.md) | #34 | — |
| 14 | [Decorator: ghi lịch sử thông báo (bảng notification_log)](issues/14-d-audit.md) | #35 | 13 |
| 15 | [Decorator: chống gửi trùng](issues/15-d-dedup.md) | #36 | 14 |
| 16 | [Decorator: gửi thêm SMS qua Adapter](issues/16-d-sms.md) | #37 | 14, 11, 12 |
| 17 | [Decorator: lỗi SMS không ảnh hưởng email và đặt lịch](issues/17-d-fail.md) | #38 | 16 |
| 18 | [Decorator: chốt thứ tự chuỗi](issues/18-d-order.md) | #39 | 15, 17 |
| 19 | [Factory: tạo tài khoản Bệnh nhân](issues/19-f-patient.md) | #40 | — |
| 20 | [Factory: Bác sĩ tự đăng ký](issues/20-f-doctor.md) | #41 | 19 |
| 21 | [Factory: Admin tạo Bác sĩ và Bệnh nhân](issues/21-f-admin.md) | #42 | 20 |
| 22 | [Factory: vai trò không hợp lệ](issues/22-f-invalid.md) | #43 | 19 |
| 23 | [Facade: xác nhận đặt lịch và tạo lịch cũ](issues/23-fa-confirm.md) | #44 | — |
| 24 | [Facade: hủy, xem chi tiết, danh sách](issues/24-fa-rest.md) | #45 | 23 |
| 25 | [Sơ đồ Builder (class, sequence, ERD, DFD)](issues/25-dg-builder.md) | #46 | 01, 05 |
| 26 | [Sơ đồ Observer](issues/26-dg-observer.md) | #47 | 01, 09 |
| 27 | [Sơ đồ Adapter](issues/27-dg-adapter.md) | #48 | 01, 11, 12 |
| 28 | [Sơ đồ Decorator](issues/28-dg-decorator.md) | #49 | 01, 18 |
| 29 | [Sơ đồ Factory](issues/29-dg-factory.md) | #50 | 01, 21, 22 |
| 30 | [Sơ đồ Facade](issues/30-dg-facade.md) | #51 | 01, 24 |
| 31 | [Chọn 4 sơ đồ chính của Strategy](issues/31-dg-strategy.md) | #52 | — |
| 32 | [Bản đồ tổng thể 7 mẫu](issues/32-dg-map.md) | #53 | 02, 25, 26, 27, 28, 29, 30 |
| 33 | [README + kịch bản Builder](issues/33-doc-builder.md) | #54 | 25 |
| 34 | [README + kịch bản Observer](issues/34-doc-observer.md) | #55 | 26 |
| 35 | [README + kịch bản Adapter](issues/35-doc-adapter.md) | #56 | 27 |
| 36 | [README + kịch bản Decorator](issues/36-doc-decorator.md) | #57 | 28 |
| 37 | [README + kịch bản Factory](issues/37-doc-factory.md) | #58 | 29 |
| 38 | [README + kịch bản Facade](issues/38-doc-facade.md) | #59 | 30 |
| 39 | [Chạy toàn bộ test và gộp vào main](issues/39-final.md) | #60 | 04, 31, 32, 33, 34, 35, 36, 37, 38 |
