# Cấu trúc dữ liệu (ERD) - Hospital/Clinic Booking System

Agent PHẢI sinh entity/migration đúng 100% theo bảng dưới đây, không tự thêm/bớt cột, không tự đổi tên field.

## USERS
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | bigint | PK, Mã tài khoản |
| email | string | UNIQUE, Email đăng nhập |
| password_hash | string | Mật khẩu mã hoá (BCrypt) |
| role | string | PATIENT / DOCTOR / ADMIN |
| is_active | boolean | Trạng thái tài khoản (true/false) |
| created_at | timestamp | Thời gian tạo |
| updated_at | timestamp | Thời gian cập nhật |

## PATIENTS
| Cột | Kiểu | Ghi chú |
|---|---|---|
| user_id | bigint | PK, FK → USERS.id (quan hệ 1-1 "là") |
| full_name | string | Họ và tên |
| dob | date | Ngày sinh |
| gender | string | MALE / FEMALE / OTHER |
| phone | string | UNIQUE, Số điện thoại |
| address | string | Địa chỉ |
| medical_history | string | Tiền sử bệnh án cơ bản |

## SPECIALTIES
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | bigint | PK, Mã chuyên khoa |
| name | string | Tên chuyên khoa (VD: Tim mạch, Nhi...) |
| description | string | Mô tả chuyên khoa |

## DOCTORS
| Cột | Kiểu | Ghi chú |
|---|---|---|
| user_id | bigint | PK, FK → USERS.id (quan hệ 1-1 "là") |
| specialty_id | bigint | FK → SPECIALTIES.id (quan hệ N-1 "thuộc về") |
| full_name | string | Họ và tên bác sĩ |
| phone | string | UNIQUE, Số điện thoại |
| degree | string | Bằng cấp / Học vị |
| experience_years | int | Số năm kinh nghiệm |
| consultation_fee | decimal | Giá khám mặc định |
| is_approved | boolean | Xác nhận duyệt bởi Admin |

## SCHEDULES
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | bigint | PK, Mã ca làm việc |
| doctor_id | bigint | FK → DOCTORS.user_id (quan hệ 1-N "đăng ký") |
| work_date | date | Ngày làm việc |
| start_time | time | Giờ bắt đầu slot (VD: 08:00) |
| end_time | time | Giờ kết thúc slot (VD: 08:30) |
| is_available | boolean | Trạng thái trống (true = còn trống) |

## APPOINTMENTS
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | bigint | PK, Mã lịch hẹn |
| booking_code | string | UNIQUE, Mã đặt chỗ (UUID / random string) |
| patient_id | bigint | FK → PATIENTS.user_id (quan hệ 1-N "đặt") |
| doctor_id | bigint | FK → DOCTORS.user_id (quan hệ 1-N "phụ trách") |
| schedule_id | bigint | FK → SCHEDULES.id (quan hệ 1-1 "áp dụng cho") |
| symptoms | string | Triệu chứng ban đầu / Lý do khám |
| status | string | CONFIRMED / COMPLETED / CANCELLED |
| price | decimal | Giá khám thực tế tại thời điểm đặt |
| created_at | timestamp | Thời gian tạo lịch hẹn |

## MEDICAL_RECORDS
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | bigint | PK, Mã hồ sơ khám |
| appointment_id | bigint | FK, UNIQUE → APPOINTMENTS.id (quan hệ 1-1 "có kết quả") |
| diagnosis | string | Chẩn đoán bệnh |
| doctor_notes | string | Ghi chú bác sĩ |
| created_at | timestamp | Thời gian tạo ghi chú |

## Sơ đồ quan hệ (tổng hợp)

```
USERS (1) ────── (1) PATIENTS
USERS (1) ────── (1) DOCTORS
SPECIALTIES (1) ────── (N) DOCTORS
DOCTORS (1) ────── (N) SCHEDULES
DOCTORS (1) ────── (N) APPOINTMENTS
PATIENTS (1) ────── (N) APPOINTMENTS
SCHEDULES (1) ────── (1) APPOINTMENTS
APPOINTMENTS (1) ────── (1) MEDICAL_RECORDS
```

## Ràng buộc quan trọng agent phải nhớ khi code Service/Repository
1. `DOCTORS.user_id` và `PATIENTS.user_id` vừa là PK vừa là FK (kế thừa danh tính từ USERS) — không tạo thêm cột `id` riêng cho 2 bảng này.
2. Một `SCHEDULES` chỉ được gắn với tối đa 1 `APPOINTMENTS` còn hiệu lực (1-1) — đây chính là điểm cần Pessimistic Lock khi đặt lịch để tránh 2 người cùng đặt 1 schedule_id.
3. `booking_code`, `email`, `phone` (DOCTORS & PATIENTS) đều phải UNIQUE ở tầng DB, không chỉ validate ở tầng Service.
4. `MEDICAL_RECORDS.appointment_id` UNIQUE — mỗi lịch hẹn chỉ có tối đa 1 hồ sơ khám.
