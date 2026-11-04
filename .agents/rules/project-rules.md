Dự án DatLichKhamBenh: hệ thống đặt lịch khám bệnh, backend Spring Boot, frontend riêng.

PHẢI đọc /architecture.md và /schema.md trước khi sinh entity, service, controller,
hoặc bất kỳ logic đặt lịch nào. Không tự ý đổi tên field, thêm/bớt cột, hoặc gộp
business logic vào Controller.

Mọi thao tác đặt lịch (appointment) phải dùng Pessimistic Lock (SELECT FOR UPDATE)
trên bảng Schedule để tránh 2 người đặt trùng 1 slot.

Gửi email luôn phải bất đồng bộ qua Message Queue, không gọi trực tiếp trong luồng
đặt lịch.

Danh sách bác sĩ / slot rảnh ưu tiên đọc từ Redis Cache trước khi query database.

Mọi endpoint (trừ AuthController) phải đi qua JwtTokenFilter + Role-Based
Authorization (PATIENT / DOCTOR / ADMIN).

Password luôn hash bằng BCrypt, không lưu plaintext.

Code backend đặt trong /backend, code frontend đặt trong /frontend.
