# 01 — Flyway và profile prod

Status: resolved

Thực hiện mục 1 và 2 của [spec](../spec.md).

- Thêm Flyway; gom `db/upgrade/**` thành `V1…Vn`; `prod` đặt `ddl-auto: validate`.
- `application-prod.yml`: không giá trị mặc định cho `JWT_SECRET`, mật khẩu DB/Redis/RabbitMQ; thiếu thì app không khởi động.
- CORS giới hạn origin, tắt log SQL/DEBUG, thêm `/actuator/health`.
- Xong khi: DB trống + `prod` → Flyway tạo đủ bảng, `validate` qua, `mvn test` xanh.

## Comments

Hoàn tất trên nhánh `feat/production-local` (chưa commit).
- Thêm `flyway-core` + `flyway-mysql`; `db/migration/V1__baseline_schema.sql` (11 bảng, dump từ schema Hibernate sinh ra, đã gồm các bản nâng cấp `db/upgrade/**`). Flyway tắt ở `application.yml` (dev dùng `ddl-auto`, test dùng H2), bật ở `prod`.
- `application-prod.yml`: `validate`, mọi bí mật không có mặc định (thiếu `JWT_SECRET` → `Could not resolve placeholder`, app không khởi động), CORS từ `CORS_ALLOWED_ORIGINS`, chỉ lộ `/actuator/health`, log INFO.
- Kiểm chứng: MySQL trống + prod → Flyway áp dụng V1, `validate` qua, `/actuator/health` = UP, `/actuator/metrics` không còn lộ. `mvn test`: 187 chạy, 0 lỗi, 17 bỏ qua (test cần MySQL thật, bỏ qua sẵn từ trước).
- Còn lại cho issue 05: `.env.example` và `docker-compose.yml` chưa cập nhật các biến mới (`CORS_ALLOWED_ORIGINS`, `RABBITMQ_*`, `REDIS_HOST`...).
