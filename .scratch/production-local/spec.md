# Spec: Chạy "production" ở localhost, bỏ mock

Status: ready-for-agent

## Problem Statement

Hệ thống chỉ chạy được ở chế độ dev: SMS là `SimulatedSmsGatewayClient` (không gửi gì thật), dữ liệu bác sĩ/bệnh nhân mẫu do `DataInitializer` nạp, schema do Hibernate `ddl-auto: update` tự sinh, và `application.yml` chứa giá trị mặc định cứng (`JWT_SECRET`, `root/root`, `guest/guest`).

## Phạm vi (đã chốt khi grilling)

- "Production" = chuẩn production, quy mô đồ án, **chạy ở localhost**, miễn phí. **Không** deploy lên mạng: không VPS, domain, HTTPS, CI/CD, sao lưu cloud.
- Bỏ phần tiểu luận đánh giá hướng phát triển; không nằm trong kế hoạch này.
- Giữ nguyên 7 mẫu thiết kế (Builder, Observer, Adapter, Decorator, Factory, Facade, Strategy) và API hiện có.
- Đã đối chiếu với đề (Thông báo tiểu luận cuối kỳ, Mẫu thiết kế cho phần mềm HK1 2026-2027), xem mục "Ràng buộc từ đề" bên dưới.

## Ràng buộc từ đề

- Tối thiểu 2 mẫu mỗi nhóm: Creational (Builder, Factory), Structural (Adapter, Decorator, Facade), Behavioral (Observer, Strategy). Hiện đã đủ; **không được gỡ hay làm yếu mẫu nào** khi bỏ mock. Riêng Adapter: sau khi bỏ giả lập, vẫn phải chứng minh được bằng test (bản giả lập nằm ở `src/test`) và demo được.
- Chấm điểm có mục "demo chạy ổn định, có kịch bản kiểm thử" và "đánh giá hiệu năng và khả năng bảo trì": mọi thay đổi không được làm demo kém ổn định. Giữ profile `dev` chạy được như cũ; demo SMS/email phải có phương án khi mất mạng hoặc hết tiền SMS.
- Hạn nộp báo cáo + link repo là 09/11/2026, báo cáo ngày 10 hoặc 12/11/2026. Làm trên nhánh riêng, chỉ gộp vào `main` sau khi bảo vệ hoặc khi chắc chắn không phá demo.

## Solution

Một lệnh `docker compose up` ở profile `prod` dựng app + MySQL + Redis + RabbitMQ, cấu hình từ `.env`.

1. Flyway thay `ddl-auto: update`; `prod` dùng `validate`; gom `db/upgrade/**` thành `V1…Vn`.
2. Bỏ giá trị mặc định cứng ở `prod`; thiếu biến bắt buộc thì app không khởi động. CORS giới hạn origin; tắt log SQL/DEBUG; thêm `/actuator/health`.
3. `DataInitializer` chỉ ở `dev`. Ở `prod`, admin đầu tiên tạo từ `ADMIN_EMAIL`/`ADMIN_PASSWORD` khi chưa có admin.
4. Client HTTP eSMS thật thay `SimulatedSmsGatewayClient`; cờ `SMS_ENABLED` mặc định tắt. `SmsChannelAdapter` giữ nguyên; bản giả lập chuyển vào `src/test`.
5. Email thật qua Gmail SMTP (App Password, tài khoản riêng) qua RabbitMQ, đặt trong `.env`.
6. `Dockerfile` backend, thêm app vào `docker-compose.yml` kèm healthcheck, cập nhật `.env.example`.
7. Cập nhật `README.md`, `architecture.md`.

## Kiểm chứng

- `mvn test` xanh (gồm `MySqlBookingPricingIntegrationTests`).
- `grep -rniE 'simulat|mock|fake|giả lập' backend/src/main` không còn kết quả.
- Chạy thật bằng Compose: đăng ký bệnh nhân → admin duyệt quyền hưởng → đặt lịch → email đến hộp thư thật.
- SMS thật chỉ thử một tin khi bật cờ và nạp tiền.

## Out of scope

Deploy cloud, domain, HTTPS, CI/CD, rate limit/WAF, sao lưu tự động, tiểu luận.
