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

## Bốn sơ đồ chính (nộp cùng 6 mẫu còn lại)

| Loại | Sơ đồ | Vì sao chọn |
|---|---|---|
| Class | [consultation-booking-class.png](diagrams/consultation-booking-class.png) | Đủ 4 vai trò Strategy / Concrete Strategy / Context / Client trong luồng đặt lịch thật |
| Sequence | [consultation-booking-sequence.png](diagrams/consultation-booking-sequence.png) | Client chọn Strategy, Context tính giá, nhánh đổi giá và mất quyền hưởng |
| ERD | [strategy-erd.png](diagrams/strategy-erd.png) | Bảng quyền hưởng mới và cột snapshot giá trên appointments |
| DFD | [strategy-dfd-level1.png](diagrams/strategy-dfd-level1.png) | Ba tiến trình báo giá, quyền hưởng, đặt lịch và các kho dữ liệu |

Các sơ đồ còn lại trong [diagrams/](diagrams/) là phụ lục: báo giá (class + sequence), DFD mức 0, Use Case, State Machine, Activity, Package.

## Tài liệu và UML

- [Tài liệu chi tiết tính năng Strategy](tai-lieu-strategy.md) · [Kịch bản thuyết trình](kich-ban-thuyet-trinh.md) · [Bản đồ mã nguồn](ma-nguon-strategy.md)
- [Báo cáo, cách demo và bằng chứng](report.md)
- [Báo giá](standard-quote.md) · [Xác nhận đặt lịch](booking-pricing.md)
- [Sơ đồ draw.io và PNG đen trắng](diagrams/): lớp + tuần tự (báo giá, đặt lịch), Use Case, ERD, DFD mức 0/1, State Machine, Activity, Package
- [Ảnh kiểm thử](evidence/) · [Nhật ký thực hiện](execution.md)
- [Spec nghiệp vụ](../../.scratch/consultation-pricing-strategy/spec.md) · [Các ticket](../../.scratch/consultation-pricing-strategy/issues/)

- [Spec tổ chức mã nguồn và tài liệu](../../.scratch/strategy-feature-organization/spec.md)

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
python3 scripts/strategy/generate-overview-diagrams.py
DRAWIO_BIN=/path/to/draw.io bash scripts/strategy/export-diagrams.sh
```

Cấu hình MySQL kiểm thử, tài khoản fixture và giới hạn phạm vi được ghi trong [báo cáo](report.md). Việc sắp xếp thư mục không đổi công thức, phân quyền, dữ liệu hay endpoint API.
