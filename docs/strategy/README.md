# Strategy tính phí khám

Mục lục chung cho mã nguồn, tài liệu, UML, kiểm thử và công cụ của tính năng Strategy. Backend và frontend giữ cấu trúc build hiện tại, mỗi phần có thư mục `strategy` riêng.

## Mã nguồn

| Phần | Vị trí | Nội dung |
|---|---|---|
| Backend | [strategy](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/) | Toàn bộ lớp riêng của tính phí và quyền hưởng |
| Thuật toán | [pricing](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/) | Interface, Context, Standard, Insurance, VIP và kết quả tính phí |
| API | [controller](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/controller/) | Báo giá, bệnh nhân gửi yêu cầu, Admin duyệt/thu hồi |
| Nghiệp vụ | [service](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/service/) | Báo giá và kiểm tra/quản lý quyền hưởng |
| Dữ liệu | [entity](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/entity/) · [repository](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/repository/) · [dto](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/) | Hồ sơ, lịch sử quyết định, truy vấn và request/response riêng |
| Frontend | [strategy](../../frontend/strategy/) | Báo giá và quyền hưởng; có bản tương ứng trong static backend |
| SQL nâng cấp | [upgrade/strategy](../../backend/src/main/resources/db/upgrade/strategy/) | Năm migration theo thứ tự 001–005, không đổi nội dung SQL |
| Java tests | [strategy](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/) | API, giao dịch, MySQL, công thức và fixture trình duyệt |
| Frontend tests | [strategy](../../frontend/tests/strategy/) | Node và các luồng Chrome |
| Công cụ | [scripts/strategy](../../scripts/strategy/) | Sinh/xuất UML, chạy browser, fixture và kiểm tra nâng cấp |

## Tài liệu và UML

- [Báo cáo, cách demo và bằng chứng](report.md)
- [Báo giá](standard-quote.md) · [Xác nhận đặt lịch](booking-pricing.md)
- [Bốn UML draw.io và PNG đen trắng](diagrams/)
- [Ảnh kiểm thử](evidence/) · [Nhật ký thực hiện](execution.md)
- [Spec](../../.scratch/consultation-pricing-strategy/spec.md) · [Các ticket](../../.scratch/consultation-pricing-strategy/issues/)

## Điểm tích hợp dùng chung

`BookingServiceImpl` và các DTO/entity lịch hẹn dùng chung vẫn nằm ở tầng hiện tại; chúng gọi `strategy.pricing` và `strategy.service` để tính/xác nhận giá. Dashboard đặt lịch, trang lịch hẹn, trang quyền hưởng bệnh nhân/Admin giữ URL hiện có và dùng JavaScript trong thư mục `strategy`. `api.js`, đăng nhập và tài nguyên dùng chung giữ nguyên vị trí. Mã static backend được đồng bộ với frontend.

## Lệnh chạy từ thư mục gốc repo

```bash
bash scripts/maven.sh -q -f backend/pom.xml clean test
npm --prefix frontend test
python3 scripts/strategy/test-browser.py
python3 scripts/strategy/test-mysql-upgrades.py
bash scripts/strategy/run-browser-fixture.sh
python3 scripts/strategy/generate-quote-diagrams.py
python3 scripts/strategy/generate-booking-diagrams.py
DRAWIO_BIN=/path/to/draw.io bash scripts/strategy/export-diagrams.sh
```

Cấu hình MySQL kiểm thử, tài khoản fixture và giới hạn phạm vi được ghi trong [báo cáo](report.md). Việc sắp xếp thư mục không đổi công thức, phân quyền, dữ liệu hay endpoint API.
