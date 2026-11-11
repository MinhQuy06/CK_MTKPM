# Chạy production ở localhost: chế độ demo và chế độ thật

Tài liệu này mô tả cách chạy hệ thống bằng `docker-compose.prod.yml`. Có hai chế độ:

| | **Demo** (mặc định) | **Thật** |
|---|---|---|
| `APP_PROFILES` | `prod,demo` | `prod` |
| Dữ liệu | Nạp sẵn dữ liệu mẫu (chuyên khoa, dịch vụ, bác sĩ, bệnh nhân, ca khám 7 ngày tới, lịch hẹn, quyền hưởng) | Trống; chỉ tạo Admin đầu tiên từ `ADMIN_EMAIL` / `ADMIN_PASSWORD` |
| Email | Gửi qua SMTP vào **Mailpit** (hộp thư nội bộ, xem tại `http://localhost:8025`), không ra internet | Gửi qua SMTP thật (Gmail App Password) |
| SMS | Tắt, lịch sử ghi `SKIPPED_DISABLED` | Tắt mặc định; bật bằng `SMS_ENABLED=true` + khóa eSMS |
| Dùng khi | Trình diễn, chấm bài | Chạy với người dùng thật |

Cả hai chế độ đều là cùng một bản build, cùng profile `prod` (Flyway, không mật khẩu mặc định cho JWT/Redis/RabbitMQ, chỉ lộ `/actuator/health`). Chế độ demo chỉ **bật thêm** profile `demo`.

## 1. Chạy demo (cho buổi trình diễn)

Yêu cầu: Docker Desktop (Compose v2). Không cần cài Java hay Maven.

```bash
./scripts/demo-up.sh
```

Script sẽ:

1. Tạo `.env` từ `.env.example` nếu chưa có, với mật khẩu Redis/RabbitMQ và `JWT_SECRET` ngẫu nhiên, chế độ `prod,demo`. Nếu đã có `.env` thì dùng nguyên.
2. Build image và dựng 5 container: `app`, `mysql`, `redis`, `rabbitmq`, `mailpit`.
3. Chờ `/actuator/health` báo `UP` rồi in địa chỉ và tài khoản.

| Địa chỉ | Nội dung |
|---|---|
| `http://localhost:18081` | Giao diện web và API |
| `http://localhost:8025` | Hộp thư Mailpit: mọi email hệ thống gửi (xác nhận, hủy, nhắc lịch) |
| `http://localhost:18081/actuator/health` | Trạng thái ứng dụng |

### Tài khoản mẫu

| Vai trò | Email | Mật khẩu | Ghi chú |
|---|---|---|---|
| Admin | `admin@clinic.vn` | `Admin@123` | Toàn quyền, thống kê |
| Admin | `admin.manager@clinic.vn` | `Admin@123` | |
| Admin | `admin@clinic` | `admin@2026` | |
| Bác sĩ | `bs.nguyenvana@clinic.vn` | `Doctor@123` | Tim mạch, đã duyệt |
| Bác sĩ | `bs.tranthib@clinic.vn` | `Doctor@123` | Nhi khoa, đã duyệt |
| Bác sĩ | `doctor@clinic` | `doctor@2026` | Thần kinh, đã duyệt |
| Bác sĩ | `bs.ngobichn@clinic.vn` | `Doctor@123` | **Chưa duyệt** (để demo Admin duyệt bác sĩ) |
| Bệnh nhân | `bn.hoangminh@gmail.com` | `Patient@123` | Có quyền hưởng **Bảo hiểm** đã duyệt |
| Bệnh nhân | `bn.thuylinh@gmail.com` | `Patient@123` | Có quyền hưởng **VIP**, có bệnh án |
| Bệnh nhân | `bn.tranvannam@gmail.com` | `Patient@123` | Có cả Bảo hiểm và VIP |
| Bệnh nhân | `patient@clinic` | `user@2026` | Có yêu cầu Bảo hiểm **đang chờ duyệt** |

Ngoài ra có thêm khoảng 50 bác sĩ và 40 bệnh nhân sinh tự động để danh sách trông đầy đủ. Ca khám được tạo cho 7 ngày kể từ ngày mai, tính theo ngày khởi động.

### Kịch bản demo gợi ý (minh họa các mẫu thiết kế)

1. Đăng nhập `bn.hoangminh@gmail.com`, chọn bác sĩ, chọn ca. Bảng giá cho chọn Tiêu chuẩn hoặc **Bảo hiểm** (**Strategy**), xác nhận giá rồi đặt lịch (**Facade**).
2. Mở `http://localhost:8025`: thư "Xác nhận đặt lịch khám" vừa đến. Đặt lịch xong mới phát sự kiện (**Observer**), nội dung thư dựng bằng **Builder**, đi qua RabbitMQ rồi SMTP.
3. Trong DB, bảng `notification_log` có một dòng `EMAIL SENT` và một dòng `SMS SKIPPED_DISABLED` cho cùng mã lịch hẹn. Đó là chuỗi **Decorator** (chống trùng, SMS, ghi lịch sử, email) và kênh SMS qua **Adapter** đang tắt. Lệnh xem:
   ```bash
   docker compose -f docker-compose.prod.yml exec mysql mysql -uroot -e "select type,channel,status,note from datlichkhambenh.notification_log order by id desc limit 6"
   ```
4. Đăng nhập Admin, duyệt yêu cầu Bảo hiểm của `patient@clinic`, duyệt bác sĩ `bs.ngobichn@clinic.vn`. Tạo bác sĩ hoặc bệnh nhân mới qua trang Admin (**Factory Method**).
5. Hủy lịch hẹn: thư "Thông báo hủy lịch" đến Mailpit và ca khám trống trở lại.

Kiểm tra tự động toàn luồng (tạo dữ liệu mới, không phụ thuộc dữ liệu mẫu):

```bash
ADMIN_EMAIL=admin@clinic.vn ADMIN_PASSWORD=Admin@123 MAILPIT=http://localhost:8025 ./scripts/prod-smoke.sh
```

### Dừng và làm lại

```bash
docker compose -f docker-compose.prod.yml down        # dừng, giữ dữ liệu
docker compose -f docker-compose.prod.yml down -v     # dừng và xóa sạch dữ liệu (lần sau nạp lại dữ liệu mẫu)
```

Dữ liệu mẫu chỉ được thêm phần còn thiếu, nên chạy lại không tạo trùng.

## 2. Bật chế độ thật

Sửa `.env`:

```dotenv
APP_PROFILES=prod

# Email thật (Gmail: bật xác minh 2 bước, tạo App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=phongkham.cua.ban@gmail.com
MAIL_PASSWORD=app-password-16-ky-tu

# Admin đầu tiên (mật khẩu từ 8 ký tự)
ADMIN_EMAIL=quanly@phongkham.vn
ADMIN_PASSWORD=...

# SMS thật (tùy chọn, mất phí): thử sandbox trước
SMS_ENABLED=true
ESMS_API_KEY=...
ESMS_SECRET_KEY=...
ESMS_SANDBOX=true
```

Sau đó xóa dữ liệu demo rồi dựng lại, vì DB demo đang chứa tài khoản mẫu:

```bash
docker compose -f docker-compose.prod.yml down -v
docker compose -f docker-compose.prod.yml up -d --build
```

Ở chế độ thật, DB trống. Admin đầu tiên được tạo từ `ADMIN_EMAIL`, các tài khoản khác tạo qua giao diện. Để tắt lại, đặt `APP_PROFILES=prod,demo`, xóa các dòng `MAIL_*` (để quay về Mailpit), đặt `SMS_ENABLED=false`, rồi `down -v` và `up -d`.

## 3. Các thành phần "thật" nằm ở đâu

| Thành phần | File | Bật khi |
|---|---|---|
| Schema DB (Flyway) | `backend/src/main/resources/db/migration/V1__baseline_schema.sql` | Luôn bật ở `prod` |
| Cấu hình prod (không mặc định bí mật) | `backend/src/main/resources/application-prod.yml` | `prod` |
| Admin đầu tiên | `backend/src/main/java/.../config/AdminBootstrap.java` | `prod`, khi chưa có Admin |
| Dữ liệu mẫu | `backend/src/main/java/.../config/DataInitializer.java` | `dev` hoặc `demo` |
| Chặn gửi thật ở demo | `backend/src/main/java/.../config/DemoModeGuard.java` | `demo` |
| Kiểm tra tài khoản SMTP | `backend/src/main/java/.../config/MailSettingsValidator.java` | `prod` |
| Gửi email (consumer RabbitMQ → SMTP) | `backend/src/main/java/.../messaging/EmailQueueConsumer.java` | Luôn bật; SMTP đích do `MAIL_HOST` quyết định |
| Client SMS thật (eSMS) | `backend/src/main/java/.../adapter/EsmsGatewayClient.java` | `SMS_ENABLED=true` |
| SMS đã tắt | `backend/src/main/java/.../adapter/DisabledSmsGatewayClient.java` | `SMS_ENABLED=false` (mặc định) |
| Chọn client SMS | `backend/src/main/java/.../adapter/SmsGatewayConfig.java` | |
| Stack Docker | `docker-compose.prod.yml`, `backend/Dockerfile` | |
| Biến môi trường | `.env.example` → `.env` | |
| Bản giả lập SMS (chỉ test) | `backend/src/test/java/.../adapter/SimulatedSmsGatewayClient.java` | Chỉ trong `mvn test` |

(`...` = `com/datlichkhambenh/backend`)

## 4. Cách hoạt động

### Khởi động

```text
docker compose up
  → mysql, redis, rabbitmq khỏe (healthcheck) → app khởi động
  → profile prod: kiểm tra mọi biến bắt buộc; thiếu → dừng ngay
  → Flyway áp dụng db/migration (lần đầu tạo bảng), Hibernate validate schema
  → [demo] DemoModeGuard: SMTP phải là Mailpit/nội bộ, SMS phải tắt; sai → dừng
  → [demo] DataInitializer (@Order 1) nạp dữ liệu mẫu còn thiếu
  → AdminBootstrap (@Order 2): chưa có Admin → tạo từ ADMIN_EMAIL; đã có (demo) → bỏ qua
```

### Gửi thông báo khi đặt, hủy hoặc nhắc lịch

```text
Đặt lịch (Facade) → commit giao dịch → sự kiện (Observer)
  → Decorator chống trùng → Decorator SMS → Decorator ghi lịch sử → dịch vụ email
      email: dựng bản tin (Builder) → RabbitMQ (clinic.email.queue) → EmailQueueConsumer → SMTP
               demo: SMTP = mailpit:1025 → xem ở http://localhost:8025
               thật: SMTP = smtp.gmail.com:587 → hộp thư bệnh nhân
               SMTP lỗi → thử lại 3 lần (2s, 4s) → bỏ tin; đặt lịch không bị ảnh hưởng
      SMS:   SmsChannelAdapter (Adapter) → SmsGatewayClient
               SMS_ENABLED=false → DisabledSmsGatewayClient → SKIPPED_DISABLED
               SMS_ENABLED=true  → EsmsGatewayClient → REST eSMS (CodeResult 100 = đã nhận)
  → mỗi kênh ghi 1 dòng vào notification_log (SENT / FAILED / SKIPPED_*)
```

### Vì sao demo không được gửi thật

Dữ liệu mẫu có email và số điện thoại trông như thật (ví dụ `bn.hoangminh@gmail.com`). `DemoModeGuard` không cho app khởi động nếu bật `demo` mà SMTP không phải Mailpit/nội bộ hoặc `SMS_ENABLED=true`. Nhờ vậy không thể vô tình gửi thư cho người lạ.

## 5. Biến môi trường

| Biến | Bắt buộc | Mặc định trong compose | Ý nghĩa |
|---|---|---|---|
| `APP_PROFILES` | | `prod,demo` | `prod,demo` = demo, `prod` = thật |
| `REDIS_PASSWORD` | Có | | Mật khẩu Redis |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | Có | | Tài khoản RabbitMQ |
| `JWT_SECRET` | Có | | Khóa ký JWT (`openssl rand -hex 64`) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Có | | Admin đầu tiên (ở demo không được dùng vì đã có Admin mẫu) |
| `APP_PORT` | | `18081` | Cổng web/API trên máy |
| `MAILPIT_UI_PORT` | | `8025` | Cổng giao diện Mailpit |
| `CORS_ALLOWED_ORIGINS` | | `http://localhost:18081` | Origin được gọi API |
| `MAIL_HOST`, `MAIL_PORT` | | `mailpit`, `1025` | SMTP đích |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | | `demo@clinic.local`, `demo` | Tài khoản SMTP |
| `MAIL_FROM` | | `MAIL_USERNAME` | Người gửi |
| `SMS_ENABLED` | | `false` | Bật SMS thật |
| `ESMS_API_KEY`, `ESMS_SECRET_KEY` | Khi bật SMS | | Khóa eSMS |
| `ESMS_SMS_TYPE` | | `8` | Loại tin eSMS |
| `ESMS_SANDBOX` | | `false` | `true`: eSMS nhận nhưng không gửi, không trừ tiền |

MySQL hiện **không dùng user/mật khẩu riêng**: app kết nối bằng `root` với mật khẩu rỗng (`MYSQL_ALLOW_EMPTY_PASSWORD`). Điều này chấp nhận được vì MySQL không mở cổng ra máy, chỉ app trong mạng Compose truy cập được. Nếu đưa ra mạng thật thì phải đặt lại mật khẩu.

## 6. Xử lý sự cố

| Hiện tượng | Nguyên nhân và cách xử lý |
|---|---|
| `required variable ... is missing a value` khi `docker compose up` | `.env` thiếu biến bắt buộc. Dùng `./scripts/demo-up.sh` hoặc điền theo `.env.example`. |
| App dừng với `Could not resolve placeholder` | Thiếu biến môi trường của profile `prod`. Xem `docker compose -f docker-compose.prod.yml logs app`. |
| App dừng với `Chế độ demo chỉ gửi email vào SMTP nội bộ` | Đang bật `demo` nhưng `MAIL_HOST` trỏ ra ngoài. Xóa `MAIL_*` trong `.env` hoặc chuyển `APP_PROFILES=prod`. |
| Không thấy dữ liệu mẫu | `APP_PROFILES` không có `demo`, hoặc DB đã được tạo ở chế độ thật. Đặt `prod,demo` rồi `down -v` và `up -d`. |
| Không thấy email ở Mailpit | Xem `logs app` có `Sent BOOKING_CONFIRMED`. Email đi qua RabbitMQ nên trễ 1–2 giây. |
| Gmail từ chối (`535 Authentication failed`) | Phải dùng App Password, không dùng mật khẩu đăng nhập. Tài khoản cần bật xác minh 2 bước. |
| Cổng 18081 hoặc 8025 đang bận | Đổi `APP_PORT` / `MAILPIT_UI_PORT` trong `.env` (khi đổi `APP_PORT` thì đổi cả `CORS_ALLOWED_ORIGINS`). |

## 7. Chưa làm (ngoài phạm vi chạy ở localhost)

HTTPS và domain, sao lưu MySQL tự động, mật khẩu MySQL, rate limit, giám sát và lưu log tập trung. eSMS và Gmail thật chưa được nhóm kiểm chứng bằng tài khoản thật: thử `ESMS_SANDBOX=true` và gửi một thư thật trước khi dùng.
