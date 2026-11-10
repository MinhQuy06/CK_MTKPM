# Kịch bản thuyết trình: Factory Method (≈ 5 phút)

[← README Factory](README.md)

| Phút | Nói | Cho xem |
|---|---|---|
| 0:00–0:40 | "Factory Method thuộc nhóm khởi tạo: lớp cha giữ quy trình chung, để lớp con quyết định tạo đối tượng nào. Giống quầy mở thẻ bệnh viện: thủ tục chung giống nhau, bước cuối khác nhau theo loại thẻ." | Bảng thành phần |
| 0:40–1:30 | "Trước đây tạo tài khoản lặp ở ba chỗ: đăng ký với một khối if theo vai trò, và hai hàm Admin tạo bác sĩ, tạo bệnh nhân. Thêm vai trò là sửa cả ba." | Mục 2 README |
| 1:30–2:30 | "Creator `AccountFactory` có `create()` là final: lưu login, mã hóa mật khẩu, gán vai trò, rồi gọi factory method `createProfile()`. Hai lớp con tạo hồ sơ bệnh nhân và bác sĩ. Danh bạ `AccountFactories` chọn factory theo vai trò." | Sơ đồ class |
| 2:30–3:20 | "Luồng đăng ký: kiểm tra email và số điện thoại, chọn factory theo vai trò; không có factory thì báo lỗi như cũ; có thì tạo login rồi hồ sơ trong một giao dịch." | Sơ đồ sequence |
| 3:20–3:50 | "ERD: mỗi tài khoản là một dòng users và đúng một hồ sơ bệnh nhân hoặc bác sĩ. DFD: kiểm tra, chọn factory, tạo login, tạo hồ sơ." | ERD, DFD |
| 3:50–4:30 | "Điểm bảo mật: không có factory ADMIN. Em test cả qua API và gọi thẳng service bỏ qua kiểm tra đầu vào: ADMIN, NURSE đều bị từ chối và không tạo user nào." | Chạy `AccountFactoryIntegrationTests` |
| 4:30–5:00 | "Hạn chế: `AccountRequest` gom trường của cả hai vai trò và client phải ép kiểu sản phẩm. Đổi lại, thêm vai trò chỉ cần thêm một lớp." | Mục Hạn chế |

## Câu hỏi có thể gặp

- **Đây là Factory Method hay Simple Factory?** Factory Method: phương thức tạo hồ sơ là abstract trong Creator và mỗi lớp con cài một phiên bản. `AccountFactories` chỉ là danh bạ chọn creator, không chứa `switch`.
- **Sao không có Abstract Factory?** Mỗi vai trò chỉ tạo một sản phẩm (tài khoản), không phải một họ sản phẩm liên quan.
- **Thêm vai trò y tá?** Thêm `NurseAccountFactory` và `NurseAccount`; nếu muốn tự đăng ký thì nới quy tắc `@Pattern` của DTO.
