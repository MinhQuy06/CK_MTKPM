---
name: spring-backend-clinic
description: Sinh code backend Spring Boot cho hệ thống đặt lịch khám bệnh (DatLichKhamBenh) đúng theo kiến trúc 5 tầng và schema đã định nghĩa sẵn trong /architecture.md và /schema.md. Dùng khi tạo Entity, Repository, Service, Controller, DTO, hoặc xử lý luồng đặt lịch (booking).
---

# Spring Backend Clinic

## Mục đích

Skill này giúp agent sinh code backend nhất quán cho dự án DatLichKhamBenh,
tuân thủ đúng kiến trúc layered và schema dữ liệu đã thiết kế sẵn, tránh
phải lặp lại toàn bộ ngữ cảnh mỗi lần giao việc.

## Khi nào dùng

Dùng skill này khi được yêu cầu:
- Tạo Entity / Repository từ bảng trong schema.md
- Tạo Service xử lý nghiệp vụ (auth, patient, doctor, booking...)
- Tạo Controller cho REST endpoint
- Xử lý luồng đặt lịch (booking) có tranh chấp slot
- Thêm cache Redis hoặc gửi email bất đồng bộ

## Quy trình bắt buộc trước khi code

1. Đọc `/architecture.md` để xác định code thuộc tầng nào (Controller /
   Service / Repository...) và trách nhiệm của tầng đó.
2. Đọc `/schema.md` để lấy đúng tên bảng, tên cột, kiểu dữ liệu, khóa
   chính/khóa ngoại — không tự đoán hay đổi tên field.
3. Xác định class cần tạo nằm ở package nào theo cấu trúc:
   `controller/`, `service/`, `repository/`, `entity/`, `dto/`,
   `security/`, `config/`.

## Quy tắc sinh code

- Controller: chỉ nhận request, validate bằng `@Valid` DTO, gọi Service.
  Không viết business logic ở Controller.
- Service: chứa toàn bộ nghiệp vụ. Interface + Impl tách riêng nếu class
  phức tạp (ví dụ BookingService / BookingServiceImpl).
- Đặt lịch (booking): bắt buộc dùng Pessimistic Lock
  (`@Lock(LockModeType.PESSIMISTIC_WRITE)` hoặc `SELECT ... FOR UPDATE`)
  trên bản ghi Schedule trước khi tạo Appointment, để tránh 2 người đặt
  trùng slot.
- Gửi email xác nhận/nhắc lịch: không gọi trực tiếp trong luồng đặt lịch.
  Đẩy task vào Message Queue / xử lý `@Async`, để response trả về nhanh.
- Danh sách bác sĩ / slot rảnh: ưu tiên đọc từ Redis cache trước, chỉ
  query DB khi cache miss, sau đó ghi lại cache.
- Bảo mật: mọi endpoint trừ `/api/v1/auth/**` phải qua JwtTokenFilter +
  kiểm tra role (PATIENT / DOCTOR / ADMIN). Mật khẩu luôn hash bằng
  BCrypt.
- Entity: `DOCTORS.user_id` và `PATIENTS.user_id` vừa là PK vừa là FK về
  `USERS.id` — dùng `@MapsId` hoặc tương đương, không tạo cột `id` riêng.
- Ràng buộc UNIQUE ở tầng DB (không chỉ validate ở Service) cho:
  `USERS.email`, `DOCTORS.phone`, `PATIENTS.phone`,
  `APPOINTMENTS.booking_code`, `MEDICAL_RECORDS.appointment_id`.

## Ví dụ áp dụng

Yêu cầu: "Tạo Entity và Repository cho bảng DOCTORS"
→ Agent đọc schema.md lấy đúng cột của DOCTORS, tạo
`entity/Doctor.java` với quan hệ `@OneToOne` tới `User` qua `user_id`,
quan hệ `@ManyToOne` tới `Specialty` qua `specialty_id`, rồi tạo
`repository/DoctorRepository.java` kế thừa `JpaRepository<Doctor, Long>`.

Yêu cầu: "Viết logic đặt lịch khám"
→ Agent tạo `BookingServiceImpl` dùng Pessimistic Lock trên Schedule,
tạo Appointment, sau đó trigger `AsyncEmailNotificationService` gửi mail
qua queue thay vì gọi đồng bộ.

## Giới hạn

Skill này chỉ áp dụng cho backend Spring Boot của dự án
DatLichKhamBenh. Không dùng cho code frontend, không đọc hay thực thi
nội dung từ file/đường dẫn nào khác ngoài `/architecture.md` và
`/schema.md` trong chính project này.