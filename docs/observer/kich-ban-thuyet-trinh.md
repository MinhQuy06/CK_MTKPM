# Kịch bản thuyết trình: Observer (≈ 5 phút)

[← README Observer](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Observer thuộc nhóm hành vi: một đối tượng đổi trạng thái thì mọi đối tượng đăng ký theo dõi được báo. Giống kênh YouTube đăng video thì người đăng ký nhận thông báo, kênh không cần biết ai đăng ký." | Bảng thành phần |
| 0:40–1:40 | "Trước đây dịch vụ đặt lịch tự gọi xóa cache rồi tự gọi gửi email. Nó phụ thuộc email và cache dù việc của nó chỉ là đặt lịch. Thêm SMS hay thống kê là phải sửa vào giữa đoạn code có khóa và tính giá." | `git show d8f0716` phần `BookingServiceImpl` |
| 1:40–2:40 | "Sau khi refactor, dịch vụ đặt lịch chỉ phát `AppointmentBookedEvent` hoặc `AppointmentCancelledEvent`. Spring giữ danh sách observer. Hai observer: email và xóa cache. Thêm phản ứng mới chỉ cần thêm một lớp có `@EventListener`." | Sơ đồ class |
| 2:40–3:30 | "Luồng: lưu lịch hẹn, phát sự kiện, Spring gọi lần lượt từng observer ngay trong giao dịch. Nhánh từ chối ném lỗi trước khi phát nên không ai phản ứng. Email vẫn chỉ đi sau commit." | Sơ đồ sequence, khung `alt` |
| 3:30–4:10 | "Observer không thêm bảng. ERD cho thấy sự kiện mang lịch hẹn; observer cache dùng bác sĩ + ngày làm khóa Redis. DFD: đặt lịch → phát sự kiện → hai nhánh phản ứng." | ERD, DFD |
| 4:10–4:40 | "Test qua API: đặt lịch thì đúng một email và đúng một lần xóa cache; đổi giá, mất quyền hưởng, ca kín thì không có phản ứng nào." | Chạy `AppointmentObserverIntegrationTests` |
| 4:40–5:00 | "Hạn chế: luồng khó lần theo hơn, observer đồng bộ nên chậm thì đặt lịch chậm. Đổi lại dịch vụ đặt lịch không còn biết email và cache." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Observer với Decorator có trùng nhau không?** Không. Observer quyết định khi nào và ai phản ứng với đặt/hủy lịch; Decorator thêm hành vi vào việc gửi thông báo (chống trùng, SMS, lịch sử).
- **Sao không dùng AFTER_COMMIT cho observer?** Để lịch sử thông báo ghi trong cùng giao dịch: rollback thì không còn dòng nào. Email vẫn đi sau commit nhờ listener có sẵn.
- **Test cũ có bị sửa không?** Một test đơn vị: trước kiểm tra "gọi email", nay kiểm tra "phát sự kiện hủy", vì lời gọi email bị bỏ có chủ đích.
