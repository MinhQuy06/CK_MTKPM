# Bàn giao Strategy tính phí khám

Repo MinhQuy06/CK_MTKPM, branch `thaihoa`. Phần triển khai gồm báo giá và đặt lịch theo Strategy, cùng quy trình quyền hưởng cần thiết để Bảo hiểm/VIP dùng được. Không có thanh toán, kết nối cơ quan bảo hiểm hay UML cho pattern khác.

## Vai trò và chính sách

| Vai trò Strategy | Lớp trong code |
|---|---|
| Strategy | `ConsultationPricingStrategy` |
| Concrete Strategy | `StandardPricingStrategy`, `InsurancePricingStrategy`, `VipPricingStrategy` |
| Context | `PricingContext`, registry các thuật toán Spring inject |
| Client | `ConsultationQuoteService`, `BookingServiceImpl` |
| Điều kiện sử dụng | `BenefitService`, quyền đã được Admin duyệt |

| Loại | Tỷ lệ giảm mặc định | Bệnh nhân trả khi phí gốc 300.000đ |
|---|---:|---:|
| Tiêu chuẩn | 0% | 300.000đ |
| Bảo hiểm | 80% | 60.000đ |
| VIP | 10% | 270.000đ |

Đây là chính sách giá nội bộ phòng khám, không phải cam kết bảo hiểm hoàn trả. Mỗi lần dùng một Strategy, không cộng dồn. Cấu hình `app.pricing.standard-discount-rate`, `app.pricing.insurance-discount-rate`, `app.pricing.vip-discount-rate` theo các Strategy tương ứng; Tiêu chuẩn giữ mức 0 mặc định. Tỷ lệ phải trong [0,1], tối đa 18 chữ số thập phân. BigDecimal và HALF_UP đến đồng cho phí gốc rồi giá cuối; phí 0 hợp lệ, thiếu/âm/vượt cột DECIMAL bị từ chối. `discountRateExact` là chuỗi dùng khi xác nhận để tránh mất chữ số trong JavaScript; trường numeric vẫn giữ tương thích.

Bệnh nhân gửi mã hồ sơ và thời hạn đề nghị; Admin đối chiếu ngoài hệ thống, duyệt/từ chối có lý do và lịch sử. Thu hồi không xóa lịch sử; gia hạn tạo yêu cầu mới liên kết bản cũ. Quyền phải đã duyệt, chưa thu hồi, hiệu lực hôm nay và ngày khám theo Asia/Ho_Chi_Minh, tính cả ngày biên. Server kiểm tra JWT, role, ownership; sửa request bằng tay không tạo quyền.

## Luồng API và giao dịch

`GET /api/v1/appointments/pricing-options?scheduleId=...` chỉ trả loại hợp lệ cho bệnh nhân đăng nhập. `GET /api/v1/appointments/quote?scheduleId=...&strategyKey=...` tính giá, không giữ ca hoặc gửi email.

`POST /api/v1/appointments/confirm` gửi scheduleId, symptoms, strategyKey, expectedBasePrice, expectedDiscountRate (dùng chuỗi exact), expectedPrice, expectedWorkDate, expectedStartTime, expectedEndTime (ba trường giờ/ngày lấy từ quote). Giá client chỉ là dữ liệu so sánh. Server so khớp ngày/giờ báo giá dưới khóa Schedule, sau đó khóa Doctor → quyền hưởng (nếu có), kiểm tra lại, tính giá bằng Strategy rồi lưu snapshot: pricingKey, basePrice, discountRate, price, benefitRequestId. Lịch cũ giữ metadata NULL. Đổi phí/tỷ lệ hoặc thu hồi quyền sau này không sửa giá lịch đã đặt. POST `/appointments` cũ còn tương thích cho khách Standard chưa gửi quote; luồng UI mới luôn dùng `/confirm` và không thể bỏ hết quote để hạ xuống legacy.

| Mã | Giao diện và dữ liệu |
|---|---|
| `PRICE_CHANGED` (409) | Lấy báo giá mới cùng ca/loại; hiển thị cũ → mới; giữ triệu chứng; chờ xác nhận rõ ràng, kể cả giá đổi tiếp |
| `BENEFIT_UNAVAILABLE` (403) | Tải lại các loại đủ điều kiện; yêu cầu chọn lại, không tự đổi về Tiêu chuẩn |
| `SLOT_UNAVAILABLE` (409) | Bỏ ca/báo giá cũ; tải ca còn trống; chờ chọn ca mới. Ca bị xóa trả 404 cùng mã; ca bị sửa ngày/giờ trả 409 cùng mã |

Giao diện cũng so ngày/giờ của quote với ca đã chọn để không chấp nhận ca đổi ngay lúc lấy giá. Kết quả thành công hiển thị ngày/giờ từ lịch đã lưu.

Các nhánh thất bại rollback, chưa tiêu thụ ca hoặc phát email. GET trễ bị bỏ theo phiên bản lựa chọn; đang POST thì khóa form/đóng modal. Email giữ cơ chế event AFTER_COMMIT → async RabbitMQ có sẵn, lấy giá snapshot. Kiểm thử bắt bản tin tại biên RabbitTemplate, không yêu cầu gửi email thật ra ngoài.

## UML bàn giao

Chỉ hai chức năng trực tiếp dùng Strategy; quyền hưởng chỉ là cộng tác viên cần thiết trong luồng. Mỗi `.drawio` là XML mxGraph chỉnh sửa được, đen trắng; PNG xuất bằng draw.io desktop cục bộ và kiểm tra hình.

- [Class báo giá](../diagrams/consultation-quote-class.drawio) · [PNG](../diagrams/consultation-quote-class.png)
- [Sequence báo giá](../diagrams/consultation-quote-sequence.drawio) · [PNG](../diagrams/consultation-quote-sequence.png)
- [Class đặt lịch](../diagrams/consultation-booking-class.drawio) · [PNG](../diagrams/consultation-booking-class.png)
- [Sequence đặt lịch](../diagrams/consultation-booking-sequence.drawio) · [PNG](../diagrams/consultation-booking-sequence.png)

Tổng đúng 4 draw.io + 4 PNG; không có sơ đồ quy trình Admin riêng. Tái sinh: `python3 scripts/generate-quote-diagrams.py`, `python3 scripts/generate-booking-diagrams.py`; xuất bằng `DRAWIO_BIN=/path/to/draw.io bash scripts/export-diagrams.sh`. Không đưa XML lên dịch vụ xuất ảnh bên ngoài.

## Cách kiểm thử và demo

Yêu cầu JDK21, Maven (script hỗ trợ Maven của VS Code), Node, Google Chrome và Docker cho MySQL. `npm --prefix frontend ci` cài Playwright; dùng Chrome đã cài sẵn. Backend mặc định port 18081, không chiếm 8080. API dùng cùng origin, có `CLINIC_API_BASE` cho frontend tách riêng.

```bash
bash scripts/maven.sh -q -f backend/pom.xml clean test
npm --prefix frontend test
# Mỗi browser test có H2 riêng; runner dùng port 18082, có thể đổi CK_BROWSER_PORT.
python3 scripts/test-browser.py
# Hoặc mở fixture để demo thủ công, port 18081:
bash scripts/run-browser-fixture.sh
```

Test profile tạo H2 tạm, không kết nối database vận hành. Fixture có `quote-patient@test.vn` và `quote-admin@test.vn`, mật khẩu `BrowserTest123!`, hai bác sĩ giá 300.000/450.000đ và ca ngày mai. Đây là tài khoản tổng hợp chỉ trong test classpath. Đăng nhập bệnh nhân → Quyền hưởng → gửi Bảo hiểm/VIP; đăng nhập Admin ở profile trình duyệt khác → Quyền hưởng → đối chiếu, ghi lý do và duyệt; quay lại bệnh nhân chọn ca/loại, xem giá, đặt, đọc snapshot. Thu hồi trước đặt để xem phục hồi; cập nhật phí bác sĩ giữa báo giá và đặt để xem bước xác nhận lại.

MySQL kiểm thử riêng, không dùng DB dev hoặc production:

```bash
docker run --name ck-mtkpm-strategy-mysql -e MYSQL_ROOT_PASSWORD=ck-test-only \
  -e MYSQL_DATABASE=ck_mtkpm_tests -p 127.0.0.1:13317:3306 -d mysql:8.0
# Đợi mysqladmin ping thành công trước khi chạy.
CK_MYSQL_URL='jdbc:mysql://127.0.0.1:13317/ck_mtkpm_tests?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh' \
  bash scripts/maven.sh -q -f backend/pom.xml clean test
python3 scripts/test-mysql-upgrades.py
```

Chỉ tạo container khi tên/port còn trống; nếu container tổng hợp này đã có, dùng lại. Suite MySQL dùng schema test create-drop. Script nâng cấp chỉ xóa/tạo schema `ck_mtkpm_upgrade_test` trong container này, kiểm tra đủ 5 SQL nâng cấp và giữ phí lịch cũ/metadata NULL. Với database hiện hữu, áp SQL `backend/src/main/resources/db/upgrade/001` đến `005` theo thứ tự sau sao lưu; không coi ddl-auto là cơ chế nâng cấp dữ liệu.

Bằng chứng được ghi trong [execution.md](execution.md): API H2 + MySQL thật, tranh ca, cập nhật phí/thu hồi đang giữ khóa, rollback không email, commit có snapshot email; Node kiểm tra response trễ; Chrome toàn luồng ba loại, yêu cầu/duyệt UI, thu hồi/gia hạn, xác nhận giá đổi, mất quyền/mất ca. Test tỷ lệ thay đổi dùng response quote cũ mô phỏng deployment chính sách trước, còn POST từ chối và GET giá mới đi server thật; test riêng chạy server với tỷ lệ 18 chữ số và đặt thành công.

## SOLID, lợi ích và giới hạn

SRP: mỗi Strategy giữ một công thức; Context lo registry/chuẩn hóa; eligibility là điều kiện riêng. OCP: thêm Strategy đăng ký mới không sửa thuật toán Context. DIP: service gọi interface qua Context. Không giữ Strategy đã chọn trong singleton mutable nên không lẫn yêu cầu giữa bệnh nhân.

Lợi ích là dùng chung chính sách giữa báo giá/đặt, dễ kiểm thử công thức, lưu căn cứ giải thích giá lịch sử. Chi phí là thêm lớp/điều phối, cấu hình phải nhất quán giữa instance, khóa Doctor khiến booking cùng bác sĩ chờ ngắn hạn. Xác minh hồ sơ là thủ công, chưa có ảnh/đối tác ngoài. Không bổ sung outbox, bảo đảm giao email vượt cơ chế hàng đợi hiện có, hoặc nghiệp vụ quyết toán. Các thay đổi này phục vụ phần đồ án Strategy đã chốt; chưa phải bằng chứng toàn bộ hệ thống sẵn sàng vận hành production.

## Kết quả cuối (2026-11-05)

Build sạch qua **118 test H2/MySQL** (0 lỗi, 0 bỏ qua), **11 test Node**, **16 kịch bản Chrome** với fixture riêng và **5 SQL nâng cấp MySQL**. Reviewer độc lập phát hiện hai lỗi quan trọng (ca bị xóa không phục hồi, ca đổi ngày/giờ nhưng xác nhận cũ vẫn được chấp nhận); cả hai đã có hồi quy RED→GREEN và suite sạch sau sửa. Ngày mặc định Việt Nam và tỷ lệ 18 chữ số cũng có hồi quy trình duyệt.

Điểm nhỏ để lại: chưa có test cố ý rollback *sau khi đã đăng ký event email*. Các test hiện có kiểm tra thất bại sớm không email và commit thật có email đúng snapshot; cơ chế AFTER_COMMIT giữ nguyên. Ngoài phạm vi: quy tắc múi giờ của hủy/hoàn tất lịch cũ, tuần tự hóa khóa/vô hiệu tài khoản trong lúc đặt và bảo đảm gửi lại sau lỗi broker/outbox.
