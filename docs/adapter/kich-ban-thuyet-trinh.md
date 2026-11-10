# Kịch bản thuyết trình: Adapter (≈ 5 phút)

[← README Adapter](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Adapter thuộc nhóm cấu trúc: làm hai giao diện không khớp làm việc được với nhau mà không sửa bên nào. Giống củ sạc du lịch." | Bảng thành phần |
| 0:40–1:40 | "Phòng khám muốn gửi thêm SMS. SDK nhà cung cấp nhận số quốc tế, văn bản thô và trả mã số. Phòng khám lưu số kiểu 0912 345 678 và làm việc với bản tin. Gọi thẳng SDK thì các chi tiết đó lan vào nghiệp vụ. Ở đây SDK là giả lập, chỉ lưu tin trong bộ nhớ." | Mục 2 README |
| 1:40–2:40 | "Target là `NotificationChannel` của phòng khám. Adaptee là `SimulatedSmsGatewayClient`. `SmsChannelAdapter` giữ client bên trong, đổi số sang +84, soạn SMS ngắn và dịch mã 0/400 thành SENT/FAILED." | Sơ đồ class |
| 2:40–3:30 | "Luồng: không có số thì bỏ qua; số sai thì FAILED và không gọi nhà cung cấp; hợp lệ thì gọi `submitSms` rồi dịch kết quả." | Sơ đồ sequence, hai khung `alt` |
| 3:30–4:10 | "Adapter chỉ đọc số điện thoại của bệnh nhân; kết quả được Decorator ghi vào `notification_log`. DFD: chuẩn hóa số → soạn nội dung → gửi và dịch mã." | ERD, DFD |
| 4:10–4:40 | "Test qua bean: nhà cung cấp nhận đúng +84 và nội dung có mã lịch hẹn; số rỗng hoặc sai thì không gọi nhà cung cấp. Thêm 13 test cho các dạng số." | Chạy `SmsChannelAdapterTests`, `VietnamPhoneTests` |
| 4:40–5:00 | "Hạn chế: chưa có nhà cung cấp thật, chưa retry. Đổi nhà cung cấp chỉ cần viết một adapter mới." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Adapter khác Facade?** Facade đơn giản hóa nhiều lớp thành một cửa; Adapter đổi một giao diện thành giao diện khác.
- **Adapter khác Decorator?** Decorator giữ nguyên giao diện và thêm hành vi; Adapter đổi giao diện.
- **Sao dùng object adapter?** Java chỉ kế thừa đơn và SDK thường là lớp final; giữ client bên trong dễ thay bằng client giả trong test.
