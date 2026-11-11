# 06 — Tài liệu và nghiệm thu

Status: resolved

Blocked by: 05

Thực hiện mục 7 và phần Kiểm chứng của [spec](../spec.md).

- Cập nhật `README.md`, `architecture.md`: cách chạy `prod` local, đã bỏ mock nào, biến môi trường.
- Chạy đủ phần Kiểm chứng của spec và ghi kết quả vào cuối file này dưới `## Comments`.

## Comments

Nghiệm thu trên nhánh `feat/production-local` (2026-11-11).

**Tài liệu**: `README.md` (mục "Chạy production ở localhost", bảng biến môi trường, "Lưu ý production" viết lại cho đúng), `architecture.md` (bảng cấu hình theo profile), `docs/adapter/README.md` và `docs/README.md` (Adaptee thật/giả).

**Kiểm chứng**
1. `mvn test`: 199 chạy, 0 lỗi, 17 bỏ qua (test cần MySQL thật, bỏ qua sẵn từ trước, chưa chạy riêng).
2. Chạy thật `docker compose -f docker-compose.prod.yml up -d --build` từ trạng thái sạch, rồi `scripts/prod-smoke.sh` (2 lần, DB mới mỗi lần): Admin tạo chuyên khoa + bác sĩ → bác sĩ mở ca → bệnh nhân đăng ký → trước khi duyệt chỉ có giá Tiêu chuẩn → xin quyền Bảo hiểm → Admin duyệt → báo giá 300000 → 60000 (giảm 0.8) → đặt lịch với giá chốt 60000 → email xác nhận đến hộp thư. Bảng `users` chỉ có 3 tài khoản vừa tạo, không dữ liệu giả.
3. Lịch sử thông báo (Decorator + Adapter) ở prod: `EMAIL SENT` và `SMS SKIPPED_DISABLED` cho cùng mã lịch hẹn.
4. `grep -rniE 'simulat|mock|fake|giả lập' backend/src/main`: không còn bản giả lập nào; chỉ còn cờ `simulated()` luôn `false` trong Strategy/DTO báo giá (hợp đồng API cũ, chưa gỡ).

**Chưa kiểm chứng (cần bạn làm)**
- Email thật qua Gmail: lượt chạy dùng Mailpit làm SMTP (cùng giao thức SMTP). Cần App Password Gmail trong `.env`.
- SMS thật: chưa thử với tài khoản eSMS; chạy `ESMS_SANDBOX=true` trước, đối chiếu tên trường API với tài liệu eSMS.
- 17 test bị bỏ qua (MySQL thật) chưa chạy riêng.
- Chưa có đánh giá hiệu năng; đề có mục "hiệu năng và khả năng bảo trì".
