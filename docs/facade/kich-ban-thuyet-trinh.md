# Kịch bản thuyết trình: Facade (≈ 5 phút)

[← README Facade](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Facade thuộc nhóm cấu trúc: một giao diện đơn giản đứng trước một hệ thống con phức tạp. Giống quầy lễ tân phòng khám: bệnh nhân chỉ nói muốn khám, lễ tân lo phần còn lại." | Bảng thành phần |
| 0:40–1:30 | "Trước đây controller đặt lịch tự đổi người đăng nhập thành userId ở mọi endpoint, tự đọc vai trò, tự bật cờ bắt buộc báo giá rồi gọi thẳng dịch vụ. Nó vừa lo HTTP vừa lo cách dùng dịch vụ." | `git show 6528f29` phần `BookingController` |
| 1:30–2:30 | "Em đặt `BookingFacade` ở giữa: năm hàm cho năm thao tác. Facade gọi `CurrentUserService` và `BookingService`; bên trong đặt lịch còn Strategy tính giá và Observer phát sự kiện, controller không cần biết." | Sơ đồ class (khung hệ thống con) |
| 2:30–3:20 | "Luồng xác nhận đặt lịch: controller kiểm quyền và dữ liệu đầu vào, gọi `confirm`; Facade bật cờ báo giá, lấy userId, gọi dịch vụ. Lỗi đổi giá hay ca kín đi xuyên Facade với đúng mã 409/403 như cũ." | Sơ đồ sequence |
| 3:20–3:50 | "Facade không thêm bảng, không chạm DB. ERD là dữ liệu phía sau cửa. DFD: mọi tác nhân chỉ đi vào một tiến trình." | ERD, DFD |
| 3:50–4:30 | "Test qua API cho cả năm endpoint, cộng toàn bộ hợp đồng đặt lịch của Strategy vẫn pass nguyên vẹn. Controller giờ chỉ còn một phụ thuộc." | Chạy `BookingFacadeIntegrationTests`, mở `BookingController` |
| 4:30–5:00 | "Hạn chế: Facade mỏng, chủ yếu chuyển tiếp; nếu nhồi thêm báo giá, quyền hưởng thì dễ thành lớp ôm đồm, nên em giữ đúng năm thao tác." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Facade khác Adapter?** Adapter đổi một giao diện cho khớp với client; Facade gom nhiều lớp thành một cửa đơn giản.
- **Sao báo giá không đi qua Facade?** Phần Strategy đã có bộ kiểm thử trình duyệt riêng; giữ nguyên để không phá phần đã ổn định.
- **Facade có ngăn gọi thẳng `BookingService` không?** Không, đó là đặc điểm của mẫu: Facade là lối tắt, không phải bức tường.
