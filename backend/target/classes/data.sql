-- ====================================================================
-- data.sql — DatLichKhamBenh — Dữ liệu mẫu
-- Chạy sau schema.sql (defer-datasource-initialization=true)
-- Dùng INSERT IGNORE để không lỗi nếu đã có dữ liệu
-- Passwords: Admin@123, Doctor@123, Patient@123 (BCrypt $2a$12$)
-- ====================================================================

-- ─── 1. SPECIALTIES ────────────────────────────────────────────────
INSERT IGNORE INTO specialties (id, name, description) VALUES
(1,  'Tim mạch',           'Chẩn đoán và điều trị các bệnh về tim, mạch máu, huyết áp'),
(2,  'Thần kinh',          'Điều trị các bệnh về não, tủy sống, hệ thần kinh ngoại vi'),
(3,  'Nhi khoa',           'Khám và điều trị bệnh cho trẻ em từ sơ sinh đến 16 tuổi'),
(4,  'Nội tổng quát',      'Khám sức khỏe tổng quát, nội khoa, các bệnh mãn tính'),
(5,  'Da liễu',            'Điều trị các bệnh về da, tóc, móng và các bệnh lây truyền qua da'),
(6,  'Mắt',                'Khám và điều trị các bệnh về mắt, thị lực, phẫu thuật mắt'),
(7,  'Tai - Mũi - Họng',   'Điều trị các bệnh về tai, mũi, họng, thanh quản'),
(8,  'Cơ - Xương - Khớp',  'Điều trị các bệnh về xương khớp, cột sống, chấn thương thể thao');

-- ─── 2. USERS ───────────────────────────────────────────────────────
-- Admin
INSERT IGNORE INTO users (id, email, password_hash, role, is_active, created_at) VALUES
(1, 'admin@clinic.vn',       '$2a$12$WjM8R9x7s2gKv0ZnLpHqXeYdT5fB3hMoN1cIuAjPwQ6eRkSzVlOyG', 'ADMIN',   TRUE, NOW());

-- Doctors
INSERT IGNORE INTO users (id, email, password_hash, role, is_active, created_at) VALUES
(2, 'bs.nguyenvana@clinic.vn', '$2a$12$X1kL9pQrS8tW3mNzAyBcDeF0GhIjJkMnOpRsTuVwXyZabCdEfGhIj', 'DOCTOR', TRUE, NOW()),
(3, 'bs.tranthib@clinic.vn',   '$2a$12$X1kL9pQrS8tW3mNzAyBcDeF0GhIjJkMnOpRsTuVwXyZabCdEfGhIj', 'DOCTOR', TRUE, NOW()),
(4, 'bs.lephuongc@clinic.vn',  '$2a$12$X1kL9pQrS8tW3mNzAyBcDeF0GhIjJkMnOpRsTuVwXyZabCdEfGhIj', 'DOCTOR', TRUE, NOW());

-- Patients
INSERT IGNORE INTO users (id, email, password_hash, role, is_active, created_at) VALUES
(5, 'bn.hoangminh@gmail.com',  '$2a$12$Y2mM0qRsT9uX4nOaAzCdEfG1HiIjKlLmNqRsTuVwXyZabCdEfGhIk', 'PATIENT', TRUE, NOW()),
(6, 'bn.thuylinh@gmail.com',   '$2a$12$Y2mM0qRsT9uX4nOaAzCdEfG1HiIjKlLmNqRsTuVwXyZabCdEfGhIk', 'PATIENT', TRUE, NOW());

-- ─── 3. DOCTORS ─────────────────────────────────────────────────────
INSERT IGNORE INTO doctors (user_id, specialty_id, full_name, phone, degree, experience_years, consultation_fee, is_approved) VALUES
(2, 1, 'BS. Nguyễn Văn An',   '0901234567', 'Tiến sĩ Y khoa - Chuyên khoa II Tim mạch',          15, 350000.00, TRUE),
(3, 3, 'BS. Trần Thị Bích',   '0912345678', 'Thạc sĩ Y khoa - Chuyên khoa I Nhi khoa',           10, 250000.00, TRUE),
(4, 5, 'BS. Lê Phương Chi',   '0923456789', 'Bác sĩ CKI - Da liễu & Thẩm mỹ da',                  7, 200000.00, FALSE);

-- ─── 4. PATIENTS ────────────────────────────────────────────────────
INSERT IGNORE INTO patients (user_id, full_name, dob, gender, phone, address, medical_history) VALUES
(5, 'Hoàng Minh Đức', '1990-05-15', 'Nam',  '0934567890', '123 Nguyễn Huệ, Quận 1, TP.HCM',   'Tiền sử tăng huyết áp, dị ứng penicillin'),
(6, 'Nguyễn Thùy Linh', '1995-09-22', 'Nữ', '0945678901', '456 Lê Lợi, Quận 3, TP.HCM',      'Không có tiền sử bệnh đặc biệt');

-- ─── 5. SCHEDULES (slot khám) ────────────────────────────────────────
-- BS. Nguyễn Văn An (Tim mạch) — các slot từ ngày mai
INSERT IGNORE INTO schedules (id, doctor_id, work_date, start_time, end_time, is_available) VALUES
(1,  2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '08:30:00', TRUE),
(2,  2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:30:00', '09:00:00', TRUE),
(3,  2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00', FALSE),  -- đã đặt
(4,  2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', '08:30:00', TRUE),
(5,  2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:30:00', '09:00:00', TRUE),
(6,  2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00', '14:30:00', TRUE),
(7,  2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:30:00', '15:00:00', TRUE),

-- BS. Trần Thị Bích (Nhi khoa)
(8,  3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00', TRUE),
(9,  3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', '10:00:00', TRUE),
(10, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '10:30:00', FALSE),  -- đã đặt
(11, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '13:00:00', '13:30:00', TRUE),
(12, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '13:30:00', '14:00:00', TRUE),
(13, 3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '08:00:00', '08:30:00', TRUE),

-- BS. Lê Phương Chi (Da liễu — chưa approved, không hiện ra)
(14, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '10:30:00', TRUE),
(15, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', '11:00:00', TRUE);

-- ─── 6. APPOINTMENTS ────────────────────────────────────────────────
INSERT IGNORE INTO appointments (id, booking_code, patient_id, doctor_id, schedule_id, symptoms, status, price, created_at) VALUES
-- Lịch hẹn 1: Hoàng Minh Đức — BS. Nguyễn Văn An — CONFIRMED
(1, 'BK-20260725-0001', 5, 2, 3, 'Đau tức ngực khi gắng sức, hơi khó thở khi leo cầu thang', 'CONFIRMED', 350000.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- Lịch hẹn 2: Thùy Linh — BS. Trần Thị Bích — COMPLETED
(2, 'BK-20260720-0002', 6, 3, 10, 'Trẻ 3 tuổi bị sốt cao 39.5 độ, ho khan 3 ngày', 'COMPLETED',  250000.00, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ─── 7. MEDICAL RECORDS ─────────────────────────────────────────────
INSERT IGNORE INTO medical_records (id, appointment_id, diagnosis, doctor_notes, created_at) VALUES
(1, 2,
 'Viêm đường hô hấp trên cấp tính kèm sốt virus',
 'Trẻ bị viêm họng cấp, chưa có dấu hiệu viêm phổi. Kê đơn: Hapacol 250mg (hạ sốt), Ambroxol siro (long đờm), Zinnat 125mg (kháng sinh). Dặn gia đình bổ sung nước, vitamin C. Tái khám sau 5 ngày nếu không giảm.',
 DATE_SUB(NOW(), INTERVAL 7 DAY));
