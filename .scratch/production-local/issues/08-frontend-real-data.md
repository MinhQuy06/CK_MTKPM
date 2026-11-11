# 08 — Frontend chỉ hiển thị dữ liệu thật

Status: resolved

Yêu cầu (2026-11-11): frontend chưa đồng bộ với backend. Khi rà, hai bản `frontend/` và `static/` giống hệt nhau, nhưng giao diện tự sinh dữ liệu giả.

## Quyết định (grilling)

1. Giữ hai bản `frontend/` + `static/`, thêm `scripts/check-frontend-sync.sh` (kiểm tra / `--fix`).
2. Dashboard bác sĩ bỏ toàn bộ lịch hẹn giả (`generateFullDocSchedule`, `isFake`); thống kê đếm từ API.
3. Quên mật khẩu: bỏ phần giả "đã ghi nhận", chỉ hướng dẫn liên hệ Admin.
4. Không hiển thị chế độ demo trên giao diện.
5. `admin/users.html`: thống kê và "Lịch khám" của bác sĩ lấy từ endpoint mới.
6. `AppointmentDto` thêm `diagnosis`, `doctorNotes`.
7. Dashboard bệnh nhân: bỏ 5 sao, câu "bệnh viện lớn", giá trị dự phòng giả.
8. Bảng "giao dịch" của Admin → "Lịch hẹn & phí khám", trạng thái thật, không mã giao dịch / phương thức bịa.
9. Làm dày dữ liệu mẫu (dev/demo) để dashboard có số liệu thật.

## Comments

- Backend: `AppointmentDtoMapper` (dùng chung, nạp bệnh án theo lô), `GET /api/v1/admin/doctors/{id}/appointments`, `GET /api/v1/admin/doctors/appointment-stats`, analytics trả `appointmentFees` thay `transactions`. `DataInitializer.ensureDemoHistory()` (mã `BK-H…`, chạy một lần). Sửa ca của lịch hẹn mẫu 2 (COMPLETED nhưng ở ngày mai) về quá khứ.
- Frontend: `doctor/dashboard.html`, `admin/users.html`, `admin/dashboard.html`, `admin/analytics.html`, `patient/dashboard.html`, `login.html`, `js/api.js`; đã chép sang `static/`.
- Kiểm chứng: `mvn test` 207 chạy, 0 lỗi, 17 bỏ qua (thêm `AdminAppointmentViewsIntegrationTests` 5 test); `npm test` 14/14; `check-frontend-sync.sh` OK. Demo dựng lại từ sạch: seed 331 đã khám, 42 hủy, 35 sắp tới. Xem trong trình duyệt: dashboard bác sĩ (38 ca, 27 đã khám đều có chẩn đoán thật), Admin người dùng + modal lịch khám (khớp số), dashboard và báo cáo Admin (413 lịch hẹn, trạng thái thật), lịch hẹn bệnh nhân, modal quên mật khẩu; không lỗi console.
- Chưa chạy bộ Playwright của Strategy.
