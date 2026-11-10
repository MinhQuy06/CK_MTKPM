# Kịch bản thuyết trình: Builder (≈ 5 phút)

[← README Builder](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Mẫu Builder thuộc nhóm khởi tạo: tách việc dựng một đối tượng nhiều phần ra khỏi chính đối tượng đó. Giống quầy bánh mì: nhân viên thuộc công thức, quầy cho thêm từng nguyên liệu." | Bảng 4 thành phần trong README |
| 0:40–1:40 | "Trong hệ thống, mỗi email lịch hẹn trước đây được tạo bằng constructor 9 tham số, cả 9 đều là String. Đổi chỗ giờ bắt đầu và giờ kết thúc thì code vẫn biên dịch. Thiếu email người nhận thì chỉ biết khi worker gửi mail lỗi." | Đoạn `new EmailNotificationMessage(...)` cũ trong lịch sử git (commit trước `ab8212f`) |
| 1:40–2:40 | "Em tách thành ba vai: `NotificationMessageBuilder` có từng hàm theo tên trường và `build()` kiểm tra trường bắt buộc; `NotificationMessageDirector` giữ công thức cho xác nhận, nhắc lịch, hủy; dịch vụ email chỉ gọi Director." | Sơ đồ class |
| 2:40–3:30 | "Luồng khi đặt lịch: dịch vụ email gọi `bookingConfirmed`, Director gọi các bước dựng, `build()` kiểm tra; thiếu trường thì báo lỗi ngay. Bản tin chỉ được gửi sau khi giao dịch commit." | Sơ đồ sequence, chỉ khung `alt` |
| 3:30–4:10 | "Builder không thêm bảng: ERD cho thấy cột nào thành trường nào của bản tin. DFD: Director đọc lịch hẹn, Builder dựng và kiểm tra, rồi xếp hàng sau commit." | ERD, DFD |
| 4:10–4:40 | "Kiểm thử: đặt lịch qua API rồi bắt bản tin ở RabbitTemplate, kiểm đủ 9 trường; test đơn vị cho trường bắt buộc." | Chạy `NotificationMessage*` tests |
| 4:40–5:00 | "Hạn chế: nhiều dòng code cho một record nhỏ, chưa kiểm định dạng. Đổi lại, không còn truyền nhầm tham số và công thức nằm một chỗ." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Sao không dùng Lombok `@Builder`?** Lombok sinh builder nhưng không có chỗ kiểm tra trường bắt buộc và không có Director; viết tay để thể hiện đủ mẫu.
- **Director có bắt buộc không?** Không. Nhưng không có Director thì công thức cho từng loại thông báo lặp lại ở nơi gọi.
- **Builder khác Factory thế nào?** Factory chọn *loại* đối tượng cần tạo; Builder dựng *một* đối tượng nhiều bước.
