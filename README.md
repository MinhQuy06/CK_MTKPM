# Hệ thống đặt lịch khám bệnh

Ứng dụng web quản lý đặt lịch khám cho ba nhóm người dùng: **Bệnh nhân**, **Bác sĩ** và **Quản trị viên**. Backend được xây dựng bằng Spring Boot theo kiến trúc phân tầng; frontend dùng HTML, CSS và JavaScript thuần, được Spring Boot phục vụ trực tiếp.

## Phần Strategy tính phí khám

Mã nguồn, tài liệu, UML và kiểm thử được gom theo thư mục `strategy`. Xem [mục lục Strategy](docs/strategy/README.md) để mở toàn bộ phần tính năng này từ một nơi.

## Chức năng chính

### Bệnh nhân

- Đăng ký, đăng nhập và làm mới JWT token.
- Xem, cập nhật hồ sơ cá nhân và tiền sử bệnh.
- Tìm bác sĩ theo tên hoặc chuyên khoa, xem chi tiết và lịch trống.
- Đặt lịch, nhập triệu chứng ban đầu và nhận email xác nhận bất đồng bộ.
- Xem lịch sử, hủy lịch đúng thời hạn và xem hồ sơ khám bệnh.

### Bác sĩ

- Đăng ký tài khoản và chờ Admin duyệt.
- Quản lý hồ sơ chuyên môn, giá khám và ca làm việc.
- Xem lịch hẹn, bệnh nhân đã khám và cập nhật trạng thái buổi khám.
- Ghi chẩn đoán và ghi chú vào hồ sơ bệnh án.

### Quản trị viên

- Duyệt hoặc từ chối tài khoản bác sĩ.
- Quản lý bác sĩ, bệnh nhân, chuyên khoa và dịch vụ khám.
- Khóa hoặc mở khóa tài khoản.
- Xem thống kê lịch hẹn và doanh thu theo ngày, tháng, bác sĩ.

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.3.2, Maven |
| Bảo mật | Spring Security, JWT, BCrypt, phân quyền theo vai trò |
| Cơ sở dữ liệu | MySQL 8, Spring Data JPA, Hibernate |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3 |
| Email | Spring Mail, SMTP |
| Frontend | HTML5, CSS3, JavaScript, Bootstrap 5 |
| Kiểm thử | JUnit 5, Spring Boot Test, H2, MockMvc |

## Kiến trúc tổng quan

```mermaid
flowchart LR
    UI["Frontend Patient / Doctor / Admin"] --> SEC["JWT Security và phân quyền"]
    SEC --> CONTROLLER["REST Controllers"]
    CONTROLLER --> SERVICE["Service nghiệp vụ"]
    SERVICE --> REPOSITORY["JPA Repositories"]
    REPOSITORY --> MYSQL[(MySQL)]
    SERVICE <--> REDIS[(Redis Cache)]
    SERVICE --> RABBIT["RabbitMQ"]
    RABBIT --> MAIL["SMTP Email"]
```

Luồng đặt lịch dùng pessimistic lock trên slot khám để ngăn hai bệnh nhân đặt trùng cùng một ca. Sự kiện email chỉ được phát sau khi giao dịch đặt lịch thành công.

## Cấu trúc thư mục

```text
dat-lich-kham-benh-main/
├── backend/                         # Ứng dụng Spring Boot
│   ├── src/main/java/               # Controller, Service, Repository, Entity, Security
│   ├── src/main/resources/static/   # Frontend được Spring Boot phục vụ
│   └── src/test/                    # Unit test và integration test
├── frontend/                        # Mã nguồn giao diện độc lập
├── architecture.md                  # Quy ước kiến trúc 5 tầng
├── schema.md                        # Mô tả schema và quan hệ dữ liệu
├── docker-compose.yml               # Hạ tầng cho dev: MySQL, Redis và RabbitMQ
├── docker-compose.prod.yml          # Chạy production ở localhost: app + MySQL + Redis + RabbitMQ + Mailpit
├── .env.example                     # Mẫu biến môi trường cho docker-compose.prod.yml
├── scripts/demo-up.sh               # Dựng stack prod ở chế độ demo (tự tạo .env)
├── scripts/check-frontend-sync.sh   # Kiểm tra / đồng bộ frontend/ → backend/.../static/
├── scripts/prod-smoke.sh            # Kịch bản nghiệm thu chạy thật trên stack prod
├── run.bat                          # Chạy nhanh trên Windows với profile dev
└── DatLichKhamBenh.postman_collection.json
```

Hai thư mục `frontend/` và `backend/src/main/resources/static/` đang chứa cùng một phiên bản giao diện. Khi chạy Spring Boot, giao diện trong thư mục `static/` được phục vụ tại cổng của backend và không cần cài Node.js.

Sửa giao diện ở `frontend/`, rồi chạy `./scripts/check-frontend-sync.sh --fix` để chép sang `static/`. Chạy không có `--fix` để kiểm tra hai bản có lệch không. Giao diện chỉ hiển thị dữ liệu từ API, không tự sinh số liệu.

## Yêu cầu môi trường

Cài đặt các công cụ sau trước khi chạy:

- Git.
- JDK 21 trở lên.
- Maven 3.9 trở lên.
- Docker Desktop hoặc Docker Engine có Docker Compose v2.

Kiểm tra nhanh:

```bash
git --version
java -version
mvn -version
docker --version
docker compose version
```

## Clone dự án

Thay `<URL_REPOSITORY>` bằng URL GitHub/GitLab thực tế của dự án:

```bash
git clone <URL_REPOSITORY>
cd dat-lich-kham-benh-main
```

## Chạy nhanh môi trường phát triển

### Windows

Tại thư mục gốc dự án:

```powershell
docker compose up -d
.\run.bat
```
```
Võ Minh Hậu Đẹp Trai
```

### Linux hoặc macOS

```bash
docker compose up -d
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Profile `dev` sử dụng khóa JWT dành riêng cho local và tự tạo dữ liệu mẫu nếu chưa tồn tại. Không dùng profile này cho production.

Sau khi log xuất hiện thông báo `Started BackendApplication`, mở:

| Dịch vụ | Địa chỉ |
|---|---|
| Ứng dụng | [http://localhost:8080](http://localhost:8080) |
| Trang đăng nhập | [http://localhost:8080/login.html](http://localhost:8080/login.html) |
| Health check | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) |
| RabbitMQ Management | [http://localhost:15672](http://localhost:15672) |

Tài khoản RabbitMQ local mặc định là `guest` / `guest`.

## Chạy production ở localhost (Docker Compose)

Hướng dẫn đầy đủ (chế độ demo và chế độ thật, các thành phần nằm ở đâu, cách hoạt động, biến môi trường, xử lý sự cố): **[docs/production.md](docs/production.md)**.

Chạy demo nhanh (có dữ liệu mẫu, email vào hộp thư nội bộ Mailpit, SMS tắt, không gửi gì ra ngoài):

```bash
./scripts/demo-up.sh
```

- Ứng dụng: `http://localhost:18081`. Hộp thư Mailpit: `http://localhost:8025`.
- Tài khoản mẫu: xem mục bên dưới hoặc `docs/production.md`.
- Chế độ thật: trong `.env` đặt `APP_PROFILES=prod`, điền `MAIL_*` (và `ESMS_*` nếu bật SMS), rồi `docker compose -f docker-compose.prod.yml down -v` và `up -d --build`.

## Các dịch vụ Docker

`docker-compose.yml` chỉ khởi chạy hạ tầng cho chế độ dev; backend vẫn chạy bằng Maven hoặc file JAR. Để chạy cả ứng dụng ở chế độ production, dùng `docker-compose.prod.yml` (mục ở trên).

| Dịch vụ | Container | Cổng máy host |
|---|---|---|
| MySQL | `clinic_mysql` | `3307` |
| Redis | `clinic_redis` | `6379` |
| RabbitMQ AMQP | `clinic_rabbitmq` | `5672` |
| RabbitMQ Management | `clinic_rabbitmq` | `15672` |

Kiểm tra trạng thái:

```bash
docker compose ps
```

Dừng hạ tầng nhưng giữ dữ liệu:

```bash
docker compose down
```

## Chạy backend thủ công

### Chạy trực tiếp bằng Maven

PowerShell:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn spring-boot:run
```

Bash:

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### Đóng gói và chạy file JAR

```bash
cd backend
mvn clean package
```

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Bash:

```bash
SPRING_PROFILES_ACTIVE=dev java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Cấu hình môi trường

Các giá trị local mặc định khớp với `docker-compose.yml`.

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `SERVER_PORT` | `8080` | Cổng HTTP của ứng dụng |
| `DB_URL` | MySQL tại `localhost:3307/datlichkhambenh` | JDBC URL |
| `DB_USERNAME` | `root` | Tài khoản MySQL |
| `DB_PASSWORD` | `root` | Mật khẩu MySQL |
| `REDIS_HOST` | `localhost` | Máy chủ Redis |
| `REDIS_PORT` | `6379` | Cổng Redis |
| `RABBITMQ_HOST` | `localhost` | Máy chủ RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Cổng AMQP |
| `RABBITMQ_USERNAME` | `guest` | Tài khoản RabbitMQ |
| `RABBITMQ_PASSWORD` | `guest` | Mật khẩu RabbitMQ |
| `JWT_SECRET` | Không có ở profile thường | Khóa ký JWT, bắt buộc ngoài môi trường dev/test |
| `JWT_EXPIRATION_MS` | `86400000` | Thời hạn access token, mặc định 24 giờ |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Thời hạn refresh token, mặc định 7 ngày |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server |
| `MAIL_PORT` | `587` | Cổng SMTP |
| `MAIL_USERNAME` | Trống | Tài khoản gửi email |
| `MAIL_PASSWORD` | Trống | Mật khẩu ứng dụng SMTP |
| `MAIL_FROM` | `MAIL_USERNAME` (prod) | Người gửi email |
| `SMS_ENABLED` | `false` | Bật gửi SMS thật qua eSMS (tốn phí theo tin) |
| `ESMS_API_KEY`, `ESMS_SECRET_KEY` | Trống | Khóa eSMS, bắt buộc khi `SMS_ENABLED=true` |
| `ESMS_SANDBOX` | `false` | `true`: eSMS nhận tin nhưng không gửi và không trừ tiền |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Chỉ prod, bắt buộc | Admin đầu tiên (mật khẩu từ 8 ký tự) |
| `CORS_ALLOWED_ORIGINS` | Chỉ prod, bắt buộc | Origin được phép gọi API |

Ví dụ cấu hình PowerShell trước khi chạy ở môi trường không dùng profile `dev`:

```powershell
$env:JWT_SECRET = "thay-bang-khoa-bi-mat-ngau-nhien-it-nhat-32-ky-tu"
$env:DB_PASSWORD = "mat-khau-mysql"
$env:MAIL_USERNAME = "your-account@gmail.com"
$env:MAIL_PASSWORD = "your-google-app-password"
mvn spring-boot:run
```

Không commit khóa JWT, mật khẩu cơ sở dữ liệu hoặc mật khẩu SMTP thật lên Git.

## Cấu hình gửi email

Đặt lịch vẫn thành công khi chưa cấu hình SMTP, nhưng email thực tế chỉ được gửi khi có `MAIL_USERNAME` và `MAIL_PASSWORD` hợp lệ. Với Gmail, sử dụng **App Password**, không dùng mật khẩu đăng nhập thông thường.

Luồng xử lý:

```text
Đặt lịch thành công -> phát sự kiện sau commit -> RabbitMQ queue
-> Email consumer -> SMTP server -> hộp thư bệnh nhân
```

Queue mặc định: `clinic.email.queue`.

## Tài khoản mẫu (Demo Accounts)

Hệ thống cung cấp sẵn 3 tài khoản cho mỗi nhóm người dùng (**3 Admin**, **3 Bác sĩ**, **3 Bệnh nhân**) để kiểm tra và đăng nhập ngay mà không cần tạo mới. Dữ liệu được khởi tạo tự động khi ứng dụng chạy với profile `dev` (Maven) hoặc `demo` (`./scripts/demo-up.sh`).

### 1. Quản trị viên (Admin)
| STT | Tên tài khoản | Email | Mật khẩu | Quyền hạn & Ghi chú |
|:---:|---|---|---|---|
| 1 | Quản trị viên hệ thống | `admin@clinic.vn` | `Admin@123` | Toàn quyền quản trị, thống kê doanh thu |
| 2 | Quản trị viên Dev | `admin@clinic` | `admin@2026` | Tài khoản dev kiểm thử nhanh |
| 3 | Quản trị viên điều hành | `admin.manager@clinic.vn` | `Admin@123` | Quản lý chuyên khoa, duyệt bác sĩ |

### 2. Bác sĩ (Doctor)
| STT | Bác sĩ phụ trách | Chuyên khoa | Email | Mật khẩu | Trạng thái |
|:---:|---|---|---|---|---|
| 1 | BS. Nguyễn Văn An | Tim mạch | `bs.nguyenvana@clinic.vn` | `Doctor@123` | Đã duyệt (Approved) |
| 2 | BS. Trần Thị Bích | Nhi khoa | `bs.tranthib@clinic.vn` | `Doctor@123` | Đã duyệt (Approved) |
| 3 | BS. Hoàng Văn Cường | Thần kinh | `doctor@clinic` | `doctor@2026` | Đã duyệt (Approved) |

### 3. Bệnh nhân (Patient)
| STT | Họ và tên bệnh nhân | Số điện thoại | Email | Mật khẩu | Dữ liệu mẫu |
|:---:|---|---|---|---|---|
| 1 | Hoàng Minh Đức | `0934567890` | `bn.hoangminh@gmail.com` | `Patient@123` | Đã có lịch hẹn xác nhận |
| 2 | Nguyễn Thùy Linh | `0945678901` | `bn.thuylinh@gmail.com` | `Patient@123` | Đã hoàn tất khám & có bệnh án |
| 3 | Trần Thị Bình | `0911223344` | `patient@clinic` | `user@2026` | Có lịch khám chuyên khoa Thần kinh |

> [!NOTE]
> Mọi mật khẩu đều được băm bảo mật bằng thuật toán **BCrypt** trước khi lưu vào cơ sở dữ liệu. Không sử dụng các tài khoản mẫu này ở chế độ thật (`APP_PROFILES=prod`).

## API và Postman

Base URL:

```text
http://localhost:8080/api/v1
```

Các nhóm endpoint chính:

| Nhóm | Base path |
|---|---|
| Xác thực | `/api/v1/auth` |
| Bệnh nhân | `/api/v1/patients` |
| Bác sĩ và lịch làm việc | `/api/v1/doctors` |
| Đặt lịch | `/api/v1/appointments` |
| Dịch vụ khám | `/api/v1/services` |
| Quản trị | `/api/v1/admin` |

Import file `DatLichKhamBenh.postman_collection.json` vào Postman để thử API. Sau khi đăng nhập, dùng access token trong header:

```http
Authorization: Bearer <access-token>
```

## Chạy kiểm thử

Bộ test sử dụng H2 in-memory và profile `test`, không cần khởi động MySQL, Redis hoặc RabbitMQ thật.

```bash
cd backend
mvn test
```

Đóng gói và chạy toàn bộ kiểm thử:

```bash
mvn clean package
```

## Chạy frontend riêng khi phát triển

Frontend không cần bước build. Có thể mở thư mục `frontend/` bằng Live Server tại `http://localhost:5500`; backend vẫn phải chạy ở `http://localhost:8080`. CORS mặc định cho phép các origin local `localhost:5500` và `127.0.0.1:5500`.

Khi hoàn tất thay đổi giao diện, đồng bộ các tệp sang `backend/src/main/resources/static/` để bản Spring Boot sử dụng đúng phiên bản mới.

## Làm mới dữ liệu local

Lệnh sau xóa toàn bộ volume MySQL, Redis và RabbitMQ của dự án. Chỉ chạy khi chấp nhận mất toàn bộ dữ liệu local:

```bash
docker compose down -v
docker compose up -d
```

Sau đó chạy lại backend với profile `dev` để tạo schema và dữ liệu mẫu.

## Xử lý lỗi thường gặp

### Backend không kết nối được MySQL

- Kiểm tra `docker compose ps` và container `clinic_mysql`.
- Xác nhận cổng MySQL là `3307`, không phải `3306`.
- Kiểm tra `DB_URL`, `DB_USERNAME` và `DB_PASSWORD`.

### Cổng 8080 đã được sử dụng

Đóng tiến trình đang chiếm cổng hoặc đặt cổng khác:

```powershell
$env:SERVER_PORT = "8081"
.\run.bat
```

### Không nhận được email xác nhận

- Kiểm tra RabbitMQ tại `http://localhost:15672`.
- Kiểm tra queue `clinic.email.queue` có consumer hay không.
- Cấu hình `MAIL_USERNAME` và `MAIL_PASSWORD` hợp lệ.
- Với Gmail, kiểm tra App Password và xác thực hai bước.

### Thay đổi frontend nhưng giao diện không cập nhật

- Kiểm tra tệp đã được đồng bộ vào `backend/src/main/resources/static/`.
- Khởi động lại backend sau khi đóng gói JAR.
- Hard refresh trình duyệt để bỏ cache cũ.

## Tài liệu liên quan

- `architecture.md`: mô tả trách nhiệm từng tầng và luồng booking.
- `schema.md`: mô tả bảng, khóa ngoại và ràng buộc dữ liệu.
- `DatLichKhamBenh.postman_collection.json`: collection kiểm thử API.

## Lưu ý production

Đã làm trong `docker-compose.prod.yml` và profile `prod` (chi tiết: [docs/production.md](docs/production.md)):

- Mặc định chạy chế độ demo (`APP_PROFILES=prod,demo`): có dữ liệu mẫu, email vào Mailpit, SMS tắt. `DemoModeGuard` không cho bật gửi thật khi đang demo.
- Chế độ thật (`APP_PROFILES=prod`): không dữ liệu mẫu, Admin đầu tiên lấy từ biến môi trường, email SMTP thật, SMS eSMS khi bật.
- `JWT_SECRET`, mật khẩu Redis và RabbitMQ bắt buộc đến từ `.env`; thiếu thì không khởi động.
- Schema do Flyway quản lý (`db/migration`), Hibernate chỉ `validate`.
- Chỉ lộ `/actuator/health`; CORS giới hạn theo `CORS_ALLOWED_ORIGINS`; log mức INFO.

Chưa làm (ngoài phạm vi chạy ở localhost): MySQL tạm chưa đặt user/mật khẩu (không mở cổng ra ngoài), HTTPS và domain, sao lưu MySQL tự động, rate limit, giám sát và lưu log tập trung.
