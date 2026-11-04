# Kiến trúc hệ thống - Hospital/Clinic Booking System

Hệ thống được xây dựng theo mô hình **Layered Architecture** (Spring Boot). Agent PHẢI tuân thủ đúng luồng và trách nhiệm của từng tầng dưới đây khi sinh code. Không được gộp logic giữa các tầng (ví dụ: không viết business logic trong Controller).

## 1. Client Layer (Giao diện người dùng)

### Phân hệ Bệnh nhân (Patient)
- Đăng ký / Đăng nhập & Auth
- Xem & Cập nhật Hồ sơ / Bệnh án
- Tra cứu, Lọc, Tìm Bác sĩ
- Đặt lịch khám & Điền triệu chứng
- Xem lịch sử & Hủy lịch hẹn

### Phân hệ Quản trị viên (Admin)
- Quản lý Bác sĩ & Bệnh nhân (CRUD)
- Quản lý Chuyên khoa / Dịch vụ (Master Data)
- Thống kê & Báo cáo Doanh thu / Ca khám

### Phân hệ Bác sĩ (Doctor)
- Đăng ký / Quản lý Ca làm việc (Schedule)
- Xem danh sách Bệnh nhân đã khám
- Cập nhật trạng thái khám & Chẩn đoán

## 2. Security Layer (Bảo mật & Phân quyền)

Thứ tự xử lý bắt buộc theo pipeline:
1. **JwtTokenFilter** — Xác thực Token JWT
   - Cấu trúc token: `Header.Payload.Signature`
   - Header + Payload được mã hoá, chứa thông tin + con dấu (signature)
   - Signature được tạo bằng cách kết hợp với **SECRET_KEY** riêng của server
   - Server so sánh chữ ký nhận được với chữ ký tính lại — nếu payload bị chỉnh sửa thì chữ ký sẽ không khớp → từ chối token
   - Kiểm tra hạn sử dụng (expiration) của token
2. **Role-Based Authorization** — Phân quyền theo role: `PATIENT`, `DOCTOR`, `ADMIN`
3. **Input Validation** — Dùng `@Valid` trên DTO ở tầng Controller

## 3. Controller Layer (RESTful Endpoints)

| Controller | Base path | Phụ trách |
|---|---|---|
| AuthController | `/api/v1/auth` | Đăng ký, đăng nhập, cấp token |
| PatientController | `/api/v1/patients` | Hồ sơ bệnh nhân |
| AdminController | `/api/v1/admin` | Quản trị hệ thống |
| DoctorController | `/api/v1/doctors` | Quản lý bác sĩ, lịch làm việc (gọi DoctorScheduleService + Redis Cache Service) |
| BookingController | `/api/v1/appointments` | Đặt / hủy lịch khám (gọi Booking Processing Engine) |

Controller chỉ nhận request, validate, gọi Service tương ứng, KHÔNG chứa business logic.

## 4. Service Layer (Nghiệp vụ cốt lõi & Tối ưu)

- **AuthService**: Đăng ký, Hash Password (BCrypt), Tạo JWT
- **PatientService**: Quản lý hồ sơ, bệnh án
- **AdminManagementService**: Phê duyệt Bác sĩ, CRUD, Báo cáo
- **DoctorScheduleService**: Tạo / Bật / Tắt Slot khám
- **Redis Cache Service**: Cache danh sách Bác sĩ & Slot rảnh (đọc/ghi Redis Memory Database)

### Booking Processing Engine (khối xử lý đặt lịch — quan trọng nhất hệ thống)
- **BookingServiceImpl**: Xử lý Đặt / Hủy lịch, khóa tranh chấp (concurrency control)
- **Locking Mechanism**: Pessimistic Lock — dùng `SELECT ... FOR UPDATE` trên bản ghi Schedule để tránh 2 bệnh nhân đặt trùng 1 slot
- Sau khi đặt lịch thành công → **trigger sự kiện gửi mail** sang Async Email Notification Service

### Async Email Notification Service
- Gửi mail xác nhận đặt lịch & nhắc lịch
- Không gửi mail đồng bộ trong luồng đặt lịch — đẩy Task vào Message Queue để xử lý bất đồng bộ (tránh block response đặt lịch)

## 5. Repository & Data Layer (Lưu trữ)

- **Spring Data JPA Repositories**: thao tác với SQL WorkBench / MySQL.
- **Redis Memory Database**: bộ nhớ đệm (caching) cho danh sách bác sĩ & slot rảnh
- **Message Queue / Async Task**: xử lý gửi email bất đồng bộ (nhận task từ Booking Processing Engine)
- **MySQL**: lưu Users, Schedules, Appointments...

## Luồng xử lý tổng quát

```
Client → Security (JWT → Role check → Validate) → Controller → Service → Repository → Database
```

Riêng luồng đặt lịch (Booking):
```
BookingController → BookingServiceImpl
  → Locking Mechanism (SELECT FOR UPDATE trên Schedule)
  → Ghi Appointment vào DB
  → Trigger gửi mail → đẩy Task vào Message Queue
  → Async Email Notification Service xử lý gửi mail
```

## Quy tắc bắt buộc khi agent code (áp dụng cho AI trong IDE)
1. Không viết business logic trong Controller — chỉ gọi Service.
2. Mọi thao tác đặt lịch phải qua Locking Mechanism (Pessimistic Lock) để tránh race condition khi 2 người đặt cùng 1 slot.
3. Gửi email luôn phải bất đồng bộ (qua Message Queue), không gọi trực tiếp trong luồng đặt lịch.
4. Danh sách bác sĩ / slot rảnh ưu tiên đọc từ Redis Cache trước khi query DB.
5. Mọi endpoint (trừ AuthController) phải đi qua JwtTokenFilter + Role-Based Authorization.
6. Password luôn hash bằng BCrypt, không lưu plaintext.