# Kịch bản thuyết trình: Decorator (≈ 5 phút)

[← README Decorator](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Decorator thuộc nhóm cấu trúc: thêm trách nhiệm cho đối tượng bằng cách bọc nó, thay vì kế thừa. Giống ly cà phê: thêm sữa, thêm đường đều vẫn là một ly cà phê." | Bảng thành phần |
| 0:40–1:30 | "Dịch vụ thông báo chỉ gửi email. Phòng khám cần thêm lịch sử, chống gửi trùng và SMS. Sửa thẳng thì một lớp ôm hết; kế thừa thì số lớp bùng nổ theo tổ hợp." | Bảng phương án |
| 1:30–2:30 | "Em có một lớp trừu tượng cùng giao diện với dịch vụ email, giữ `inner`. Ba decorator: chống trùng, SMS, ghi lịch sử. Lắp ở một chỗ duy nhất `NotificationDecoratorConfig`, đánh dấu `@Primary` nên observer email tự nhận bản đã bọc." | Sơ đồ class, mở `NotificationDecoratorConfig` |
| 2:30–3:30 | "Thứ tự: chống trùng ngoài cùng, rồi SMS, rồi ghi lịch sử, rồi email. Ban đầu spec để ghi lịch sử ngoài cùng, nhưng như vậy nó ghi SENT cả cho lần bị bỏ qua, nên em đổi. Chống trùng đứng trước SMS nên gửi trùng không ra SMS thứ hai; em đã thử đảo thứ tự và test đỏ ngay." | Sơ đồ sequence |
| 3:30–4:10 | "Bảng mới `notification_log`: loại, mã lịch hẹn, kênh, kết quả, ghi chú, thời điểm. Không đặt khóa ngoại, liên kết qua mã lịch hẹn. DFD: chống trùng → email + lịch sử → SMS + kết quả." | ERD, DFD |
| 4:10–4:40 | "Test qua API: đặt lịch ra đúng các dòng lịch sử; nhắc hai lần ra một email; nhà cung cấp SMS sập thì đặt lịch và email vẫn thành công." | Chạy 3 lớp test Decorator |
| 4:40–5:00 | "Hạn chế em biết: SMS giả lập đi ngay còn email đi sau commit, nên commit hỏng ở bước cuối thì SMS đã đi. Cách sửa là dời SMS sang sau commit." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Decorator khác Observer ở đây?** Observer chọn ai phản ứng khi đặt/hủy lịch; Decorator thêm hành vi vào chính việc gửi thông báo và có thể chặn (chống trùng).
- **Sao không dùng AOP (`@Around`)?** AOP làm được, nhưng thứ tự và việc bật/tắt ẩn trong annotation; Decorator thấy rõ ở một chỗ và là mẫu cần trình bày.
- **Thêm kênh Zalo thì sao?** Viết một Adapter mới cài `NotificationChannel` và một decorator (hoặc cho decorator SMS nhận danh sách kênh).
