# Tài liệu tính năng: Facade một cửa cho thao tác lịch hẹn

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Facade** để controller lịch hẹn chỉ nói chuyện với một cửa duy nhất (`BookingFacade`), thay vì tự phối hợp nhiều dịch vụ.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Facade là gì

### 1.1 Định nghĩa

Facade là một mẫu **cấu trúc (structural)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Cung cấp một giao diện thống nhất cho một tập giao diện trong hệ thống con. Facade định nghĩa một giao diện cấp cao hơn để hệ thống con dễ dùng hơn.

Nói đơn giản: **phía sau có nhiều bộ phận; người dùng chỉ cần đến một quầy, quầy đó biết gọi ai, theo thứ tự nào.**

### 1.2 Ba thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Facade** | Cửa duy nhất, biết gọi hệ thống con nào, theo thứ tự nào |
| **Subsystem classes** | Làm việc thật; không biết Facade tồn tại |
| **Client** | Chỉ nói chuyện với Facade |

### 1.3 Facade ngoài đời và trong phần mềm

| Ví dụ | "Một cửa" | "Phía sau" |
|---|---|---|
| Quầy lễ tân phòng khám | "Tôi muốn khám" | Kiểm giấy tờ, tra lịch bác sĩ, thu phí |
| Bộ phận một cửa ở phường | Nộp hồ sơ | Các phòng chuyên môn xử lý |
| Nút "Đặt hàng" trên sàn TMĐT | Một nút bấm | Kho, thanh toán, vận chuyển, hóa đơn |
| Thư viện Java | SLF4J (*Simple Logging Facade for Java*) | Logback, Log4j, `java.util.logging` |
| Spring | `JdbcTemplate` | Kết nối, statement, result set, đóng tài nguyên |

Điểm chung: **người dùng không cần biết bên trong; hệ thống con vẫn dùng trực tiếp được khi cần.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Client phải phối hợp nhiều dịch vụ theo cùng một cách ở nhiều chỗ.
- Muốn tách tầng (ví dụ HTTP) khỏi chi tiết nghiệp vụ.
- Muốn một chỗ duy nhất để thay đổi "cách dùng" hệ thống con.

Không nên dùng khi:
- Client chỉ gọi một dịch vụ, không có gì để gom.
- Facade bắt đầu chứa nghiệp vụ thật: nó sẽ thành "god object".

---

## 2. Bài toán thực tế và vì sao chọn Facade

### 2.1 Hiện trạng trước khi làm

`BookingController` phụ thuộc `BookingService` và `CurrentUserService`, và tự làm việc của tầng dưới:

- tự đổi `UserDetails` thành userId ở **mọi** endpoint;
- tự đọc vai trò từ `getAuthorities()` khi xem chi tiết;
- tự bật cờ `request.setQuoteRequired(true)` ở luồng xác nhận giá.

Controller vừa lo HTTP vừa lo "cách dùng dịch vụ". Đổi cách xác định người dùng là sửa 5 endpoint; quên bật cờ ở một chỗ là bệnh nhân đặt được lịch mà không cần đối chiếu giá.

### 2.2 Ví dụ ngoài đời: quầy lễ tân

Bệnh nhân đến phòng khám chỉ nói "tôi muốn đặt khám", "tôi muốn hủy", "cho tôi xem lịch của tôi". Lễ tân biết phải xem giấy tờ (ai đây?), tra lịch bác sĩ, kiểm tra giá đã báo. Bệnh nhân không cần đi từng phòng; các phòng chuyên môn cũng không cần biết có quầy lễ tân, nhân viên nội bộ vẫn đến thẳng được.

| Ngoài đời | Trong hệ thống |
|---|---|
| Bệnh nhân | `BookingController` (Client) |
| Quầy lễ tân | `BookingFacade` |
| Xem giấy tờ "ai đây" | `CurrentUserService` (`UserDetails` → userId) |
| Phòng xếp lịch, thu phí | `BookingService` / `BookingServiceImpl` (khóa ca, quyền hưởng, Strategy, Observer) |
| Phiếu "giá đã đồng ý" | `Expectation` (`ConfirmedQuote`, `LegacyQuote`, `LegacyStandard`) |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Giữ nguyên | Bệnh nhân tự đi từng phòng | **Loại**: logic dùng dịch vụ rải ở 5 endpoint |
| Dồn vào `BookingService` | Phòng xếp lịch tự kiểm giấy tờ | **Loại**: dịch vụ đặt lịch phải biết `UserDetails` của Spring Security |
| Interceptor / `HandlerMethodArgumentResolver` | Bảo vệ cửa tự điền tên vào phiếu | **Loại**: chỉ giải quyết userId, không giải quyết "kỳ vọng giá" theo route |
| **Facade giữa controller và dịch vụ** | **Quầy lễ tân một cửa** | **Chọn**: controller chỉ còn HTTP; Facade gom cách dùng hệ thống con |

### 2.4 Vì sao Facade hợp với tính năng này

1. Có **hai hệ thống con** (người dùng hiện tại, đặt lịch) mà controller phải phối hợp ở **5 chỗ**.
2. Có một quyết định "cách dùng" quan trọng (route nào bắt buộc báo giá) cần nằm **một chỗ**.
3. Hệ thống con (đặt lịch) đã rất phức tạp và đã kiểm thử kỹ; Facade **không sửa** nó.
4. Hệ thống con vẫn cần được gọi trực tiếp (test, job sau này): đúng tinh thần Facade "không che giấu bắt buộc".

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Client | `controller/BookingController` | Chỉ tiêm `BookingFacade`; giữ `@PreAuthorize`, mã trạng thái, thông điệp |
| Facade | `facade/BookingFacade` | `confirm`, `bookWithoutQuote`, `cancel`, `details`, `myAppointments` |
| Subsystem | `service/CurrentUserService` | `UserDetails` → userId |
| Subsystem | `service/BookingService`, `service/impl/BookingServiceImpl` | Khóa ca, quyền hưởng, tính giá (Strategy), lưu, phát sự kiện (Observer) |
| Subsystem | `strategy/offer/Expectation` | Kỳ vọng của bệnh nhân về giá, `BookingOffer` kiểm tra |

### 3.2 Năm thao tác qua một cửa

| Endpoint | Hàm Facade | Facade làm gì |
|---|---|---|
| `POST /api/v1/appointments/confirm` | `confirm` | Luôn tạo `ConfirmedQuote` (loại phí, giá gốc, tỷ lệ, giá cuối, ngày/giờ ca); thiếu phần nào → `QUOTE_REQUIRED` |
| `POST /api/v1/appointments` | `bookWithoutQuote` | Có trường báo giá → `LegacyQuote` (so số tiền, không so ngày giờ); không có → `LegacyStandard` (chỉ Tiêu chuẩn) |
| `DELETE /api/v1/appointments/{id}/cancel` | `cancel` | Lấy userId rồi gọi `cancelBooking` |
| `GET /api/v1/appointments/{id}` | `details` | Lấy userId **và vai trò** rồi gọi `getBookingDetails` (chủ lịch, bác sĩ của lịch, Admin) |
| `GET /api/v1/appointments/my-appointments` | `myAppointments` | Lấy userId rồi gọi `getPatientAppointments` |

Các controller Strategy (báo giá, quyền hưởng) **không** đi qua Facade để giữ nguyên phần đã kiểm thử kỹ.

### 3.3 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/facade/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `facade-class` | Controller → Facade → hệ thống con |
| Tuần tự | `facade-sequence` | Xác nhận đặt lịch: controller → Facade → userId → `ConfirmedQuote` → `BookingService` |
| ERD | `facade-erd` | Facade không thêm bảng; dùng `users`, `appointments` qua hệ thống con |
| DFD | `facade-dfd` | Yêu cầu HTTP → Facade → hệ thống con → kết quả |
| Use Case | `facade-use-case` | Bệnh nhân, Bác sĩ, Admin; mọi thao tác «include» thao tác qua cửa đặt lịch; «extend» khóa giá chào khi đặt lịch |

Sinh lại bằng `python3 scripts/facade/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/facade/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Facade nhận `UserDetails`, dịch vụ nhận userId

- **Vấn đề**: Nếu `BookingService` nhận `UserDetails`, tầng nghiệp vụ phụ thuộc Spring Security; test dịch vụ phải dựng người dùng giả.
- **Ngoài đời**: Lễ tân xem giấy tờ, chỉ chuyển mã bệnh nhân vào trong.
- **Giải pháp**: Phụ thuộc Spring Security dừng ở Facade.

### QĐ2. Facade quyết định kỳ vọng của bệnh nhân theo route

- **Vấn đề**: Trước đây controller bật cờ `quoteRequired` trên DTO. Nếu thêm trường báo giá vào DTO dưới dạng tùy chọn, client có thể bỏ hết trường để vượt kiểm tra giá.
- **Ngoài đời**: Quầy "đặt theo giá đã báo" luôn đòi phiếu báo giá; quầy cũ chỉ nhận giá niêm yết.
- **Giải pháp** (từ 11/11/2026): `/confirm` **luôn** thành `ConfirmedQuote`; route cũ thành `LegacyQuote` hoặc `LegacyStandard`. JSON client không thể hạ cấp xuống luồng cũ. `BookingOffer` kiểm tra theo kỳ vọng đó.
- **Bằng chứng**: `BookingFacadeIntegrationTests.confirmRouteStillDemandsTheQuote`.

### QĐ3. Lỗi đi xuyên Facade

- **Vấn đề**: Bọc lỗi lại sẽ đổi mã trạng thái mà frontend và test Strategy đang dựa vào (409 `PRICE_CHANGED`, 403 `BENEFIT_UNAVAILABLE`…).
- **Giải pháp**: Facade không bắt `BookingRejectionException`; `GlobalExceptionHandler` trả đúng mã như trước.
- **Bằng chứng**: Hợp đồng đặt lịch Strategy (`BookingPricingContract` và các lớp con) pass nguyên vẹn.

### QĐ4. Facade không thay thế hệ thống con

- **Lý do**: Code khác (test, job nhắc lịch sau này) vẫn gọi `BookingService` trực tiếp được. Facade là lối vào tiện cho controller, không phải bức tường.

### QĐ5. Phủ cả 5 endpoint, kể cả endpoint cũ

- **Vấn đề**: Để endpoint cũ gọi thẳng dịch vụ thì vẫn còn một chỗ tự lấy userId.
- **Giải pháp**: `POST /appointments` cũ cũng qua `bookWithoutQuote`, giữ tương thích cho client chưa gửi báo giá.
- **Bằng chứng**: `legacyCreateStillBooksWithoutAQuote`.

### QĐ6. Giữ `@PreAuthorize` và mã trạng thái ở controller

- **Lý do**: Đó là việc của tầng HTTP. Facade không biết HTTP; đổi sang gRPC hay CLI vẫn dùng được Facade.

### QĐ7. Không đưa báo giá và quyền hưởng vào Facade

- **Lý do**: Phần Strategy đã có controller riêng, đã kiểm thử bằng trình duyệt thật. Đưa vào Facade dễ biến nó thành "god object" mà không thêm giá trị.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu đầu vào thật** đi qua Facade có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Gọi `/confirm` mà bỏ trường báo giá | Đặt lịch không đối chiếu giá | `/confirm` luôn là `ConfirmedQuote` → `QUOTE_REQUIRED` (400) | `confirmRouteStillDemandsTheQuote` |
| 2 | Client cũ không gửi trường báo giá | Bị từ chối, client cũ hỏng | `LegacyStandard` (chỉ Tiêu chuẩn) | `legacyCreateStillBooksWithoutAQuote` |
| 3 | Client cũ gửi một phần báo giá | Bỏ qua phần đã gửi, đặt sai giá | `LegacyQuote` vẫn so số tiền | Hợp đồng `BookingPricingContract` |
| 4 | Bệnh nhân khác đoán ID lịch hẹn | Xem được lịch người khác | Facade chuyển userId + vai trò; dịch vụ chỉ cho chủ lịch, bác sĩ của lịch, Admin | `detailsAreVisibleToTheOwnerAndTheDoctorButNotToAnotherPatient` |
| 5 | `UserDetails` của Spring Security | Nghiệp vụ dính framework bảo mật | Đổi thành userId tại Facade | Thiết kế (QĐ1) |
| 6 | Lỗi nghiệp vụ (giá đổi, mất quyền, ca kín) | Bọc lại thành 500 | Đi xuyên Facade, giữ 409/403/404 | `BookingPricingContract` |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `BookingFacadeIntegrationTests` | 5 | Qua API: xác nhận đặt lịch ra lịch CONFIRMED giá STANDARD; endpoint cũ vẫn đặt được; `/confirm` thiếu báo giá → 400; hủy rồi thấy CANCELLED trong danh sách; chủ lịch và bác sĩ xem được chi tiết, bệnh nhân khác bị chặn |
| Hợp đồng đặt lịch Strategy (`BookingPricingContract` và các lớp con) | — | Pass nguyên vẹn sau khi controller chuyển sang Facade |
| Trình duyệt Strategy (`python3 scripts/strategy/test-browser.py`) | 17 | Luồng đặt lịch thật qua giao diện, đi qua Facade |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Facade là **lớp điều phối tất định**: cùng yêu cầu và cùng người dùng luôn cho cùng lời gọi hệ thống con. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Test mới cho Facade + toàn bộ test cũ (MockMvc, trình duyệt) vẫn pass sau khi chuyển controller |
| **Hiệu chuẩn** | Không | "Hành vi trước refactor" phải khớp "sau refactor": cùng mã trạng thái, cùng thông điệp lỗi |
| **Bootstrap** | Không | Thử cả 5 endpoint, cả hai luồng đặt lịch (mới, cũ), cả ba vai trò xem chi tiết |
| **Công bằng giới** | Facade không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Quyền xem chỉ dựa trên quan hệ với lịch hẹn (chủ lịch, bác sĩ của lịch, Admin) |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **SRP** | Controller chỉ lo HTTP; Facade lo cách dùng hệ thống con; dịch vụ lo nghiệp vụ |
| **DIP** | Controller phụ thuộc một Facade thay vì hai dịch vụ |
| **Law of Demeter** | Controller không còn đi sâu vào `getAuthorities()` hay DTO nội bộ |
| An toàn | Quyết định "route nào bắt buộc báo giá" nằm một chỗ, client không hạ cấp được |
| Dễ đổi | Đổi cách xác định người dùng chỉ sửa Facade |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Phần lớn là chuyển tiếp | Ngoài việc tạo kỳ vọng, các hàm chủ yếu gọi thẳng dịch vụ |
| Thêm một tầng | Lần theo một request phải qua controller → Facade → dịch vụ |
| Nguy cơ phình to | Nếu thêm báo giá, quyền hưởng… Facade dễ thành "god object"; phải giữ phạm vi 5 thao tác |
| Vai trò lấy phần tử đầu | `details` đọc quyền đầu tiên của người dùng; đúng vì mỗi user chỉ có một vai trò |

---

## 9. Hướng phát triển

- Nếu cần, thêm Facade riêng cho nhóm thao tác Admin thay vì mở rộng `BookingFacade`.
- Khi có job nhắc lịch, cân nhắc thêm hàm Facade nếu job cần cùng cách dùng hệ thống con.
- Bổ sung test đơn vị cho Facade với dịch vụ giả để kiểm tra kỳ vọng tạo ra theo từng tổ hợp trường.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Facade | `backend/src/main/java/com/datlichkhambenh/backend/facade/BookingFacade.java` |
| Client | `backend/src/main/java/com/datlichkhambenh/backend/controller/BookingController.java` |
| Hệ thống con | `backend/src/main/java/com/datlichkhambenh/backend/service/`, `strategy/offer/` |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/facade/` |
| Sơ đồ | `docs/facade/diagrams/` |
| Script sinh sơ đồ | `scripts/facade/generate-diagrams.py` |
