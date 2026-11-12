# Tài liệu tính năng: Builder dựng bản tin thông báo lịch hẹn

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Builder** để dựng bản tin email (xác nhận đặt lịch, nhắc lịch, hủy lịch) thay cho constructor 9 tham số.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Builder là gì

### 1.1 Định nghĩa

Builder là một mẫu **khởi tạo (creational)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Tách việc dựng một đối tượng phức tạp khỏi cách biểu diễn nó, để cùng một quy trình dựng có thể tạo ra các biểu diễn khác nhau.

Nói đơn giản: **đối tượng có nhiều phần; thay vì nhét tất cả vào một lần gọi, ta lắp từng phần có tên rõ ràng rồi mới "đóng gói" ở bước cuối.**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Builder** | Cung cấp từng bước dựng có tên và bước `build()` cuối cùng |
| **Director** | Biết "công thức": gọi các bước theo thứ tự cho từng loại sản phẩm |
| **Product** | Đối tượng hoàn chỉnh được dựng ra |
| **Client** | Nhờ Director (hoặc gọi thẳng Builder) để có sản phẩm |

### 1.3 Builder ngoài đời và trong phần mềm

| Ví dụ | "Sản phẩm" | "Từng bước" | "Công thức" (Director) |
|---|---|---|---|
| Quầy bánh mì | Ổ bánh mì | Thêm pa-tê, thịt, rau, nước sốt | "Bánh mì thịt", "bánh mì trứng" |
| Đặt suất ăn nhanh | Suất combo | Chọn món chính, nước, món phụ | "Combo 1", "Combo 2" |
| Xây nhà | Ngôi nhà | Móng, tường, mái, điện nước | Bản vẽ nhà cấp 4, nhà phố |
| Java chuẩn | `String` | `StringBuilder.append(...)` rồi `toString()` | — |
| Java chuẩn | `HttpRequest` | `HttpRequest.newBuilder().uri().header().POST()` rồi `build()` | — |

Điểm chung: **sản phẩm có nhiều phần, có phần bắt buộc, có phần tùy chọn; người dùng gọi tên từng phần chứ không nhớ thứ tự tham số.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Constructor có nhiều tham số (đặc biệt nhiều tham số **cùng kiểu**, dễ truyền nhầm chỗ).
- Có trường bắt buộc và trường tùy chọn, cần kiểm tra trước khi tạo đối tượng.
- Có vài "công thức" lặp lại để dựng cùng một loại đối tượng.

Không nên dùng khi:
- Đối tượng chỉ có 2–3 trường khác kiểu nhau (constructor đã đủ rõ).
- Đối tượng hay đổi hình dạng: mỗi lần thêm trường phải sửa cả Product lẫn Builder.

---

## 2. Bài toán thực tế và vì sao chọn Builder

### 2.1 Hiện trạng trước khi làm

- Mỗi email được tạo bằng `new EmailNotificationMessage(type, email, patientName, bookingCode, doctorName, workDate, startTime, endTime, price)`: **9 tham số đều là `String`**.
- Đổi chỗ `startTime` và `endTime`, hay `patientName` và `doctorName`, trình biên dịch **không phát hiện**; bệnh nhân nhận email ghi sai giờ hoặc sai tên bác sĩ.
- Thiếu email người nhận hay mã lịch hẹn chỉ lộ ra khi worker gửi mail thất bại, lúc giao dịch đặt lịch đã commit xong.
- Ba loại thông báo lặp lại cùng một đoạn lấy dữ liệu từ `Appointment`.

### 2.2 Ví dụ ngoài đời: quầy bánh mì

Khách không đọc một chuỗi 9 nguyên liệu theo đúng thứ tự; họ nói "cho một ổ bánh mì thịt". Nhân viên **thuộc công thức** và lấy từng nguyên liệu ở quầy. Trước khi gói, nhân viên kiểm tra: ổ bánh có chưa, có nhân chưa. Thiếu thì không giao.

| Ngoài đời | Trong hệ thống |
|---|---|
| Quầy nguyên liệu, lấy từng thứ | `NotificationMessageBuilder` (`type(...)`, `recipientEmail(...)`…) |
| Nhân viên thuộc công thức | `NotificationMessageDirector` (`bookingConfirmed`, `reminder`, `cancelled`) |
| Ổ bánh đã gói | `EmailNotificationMessage` |
| Kiểm tra trước khi gói | `build()` kiểm tra `type`, `recipientEmail`, `bookingCode` |
| Khách gọi món | `AsyncEmailNotificationServiceImpl` |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Giữ constructor, thêm comment | Đọc 9 nguyên liệu theo thứ tự và mong không nhầm | **Loại**: comment không ngăn được truyền nhầm |
| Lombok `@Builder` | Quầy nguyên liệu không có người kiểm tra | **Loại**: không có chỗ kiểm tra trường bắt buộc, không có Director; khó trình bày mẫu |
| Factory method cho từng loại | Mỗi món một quầy riêng | **Loại**: bên trong vẫn gọi constructor 9 tham số |
| **Builder viết tay + Director** | **Quầy nguyên liệu + nhân viên thuộc công thức** | **Chọn**: gọi tên từng bước, kiểm tra lúc `build()`, công thức nằm một chỗ |

### 2.4 Vì sao Builder hợp với tính năng này

1. Bản tin có **9 trường cùng kiểu `String`**, đúng chỗ Builder phát huy: gọi theo tên thay vì theo vị trí.
2. Có **trường bắt buộc** (loại, email, mã lịch) và **trường tùy chọn** (giá của lịch cũ).
3. Có **ba công thức lặp lại** (xác nhận, nhắc, hủy) chỉ khác loại tin: đúng vai trò Director.
4. Bản tin được dùng lại cho **nội dung SMS** (Decorator gọi cùng Director), nên công thức cần nằm một chỗ.

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Builder | `builder/NotificationMessageBuilder` | `aMessage()`, 9 hàm đặt trường theo tên, `build()` kiểm tra trường bắt buộc |
| Director | `builder/NotificationMessageDirector` | `bookingConfirmed`, `reminder`, `cancelled`; đọc dữ liệu từ `Appointment` |
| Product | `messaging/EmailNotificationMessage` | record 9 trường, **không đổi hình dạng** |
| Client | `service/impl/AsyncEmailNotificationServiceImpl` | Gọi Director rồi phát `EmailNotificationRequestedEvent` |
| Client phụ | `decorator/SmsNotificationDecorator` | Dùng cùng Director để có nội dung SMS |

### 3.2 Ba công thức (Director)

| Hàm | Loại (`type`) | Dùng khi |
|---|---|---|
| `bookingConfirmed(appointment)` | `BOOKING_CONFIRMED` | Đặt lịch thành công |
| `reminder(appointment)` | `APPOINTMENT_REMINDER` | Nhắc lịch (dịch vụ đã có, chưa có job gọi) |
| `cancelled(appointment)` | `BOOKING_CANCELLED` | Bệnh nhân hủy lịch |

Cả ba dùng chung một hàm `forAppointment(type, appointment)`: lấy email và tên bệnh nhân, mã lịch, tên bác sĩ, ngày, giờ bắt đầu/kết thúc của ca và giá (`toPlainString()`, hoặc `null`).

### 3.3 Trường bắt buộc và tùy chọn

| Trường | Bắt buộc | Lý do |
|---|:---:|---|
| `type` | ✔ | Worker email chọn mẫu thư theo loại |
| `recipientEmail` | ✔ | Không có thì không gửi được |
| `bookingCode` | ✔ | Bệnh nhân tra cứu lịch và chống gửi trùng (Decorator) dựa vào mã này |
| `patientName`, `doctorName`, `workDate`, `startTime`, `endTime` | | Thiếu thì thư vẫn gửi được, chỉ thiếu thông tin hiển thị |
| `price` | | Lịch hẹn tạo trước khi có tính phí Strategy không có giá |

Thiếu trường bắt buộc → `IllegalStateException("Notification message is missing required field: <tên trường>")`.

### 3.4 Luồng chạy

Đặt lịch → Observer gọi dịch vụ thông báo (đã được Decorator bọc) → `AsyncEmailNotificationServiceImpl` gọi `director.bookingConfirmed(appointment)` → Builder dựng bản tin → phát `EmailNotificationRequestedEvent` → `EmailQueuePublisher` (`@TransactionalEventListener(AFTER_COMMIT)`) đẩy vào RabbitMQ sau khi giao dịch commit.

### 3.5 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/builder/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `builder-class` | Builder, Director, Product, Client và quan hệ |
| Tuần tự | `builder-sequence` | Đặt lịch → Director → Builder → `build()` → hàng đợi email |
| ERD | `builder-erd` | Các bảng Director đọc (`appointments`, `patients`, `doctors`, `schedules`, `users`); Builder không ghi DB |
| DFD | `builder-dfd` | Dữ liệu lịch hẹn → dựng bản tin → hàng đợi email |
| Use Case | `builder-use-case` | Đặt / hủy / nhắc lịch «include» gửi thông báo; «extend» báo lỗi thiếu trường |

Sinh lại bằng `python3 scripts/builder/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/builder/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Kiểm tra trong `build()`, không kiểm tra ở từng hàm đặt trường

- **Vấn đề**: Builder cho phép đặt trường theo thứ tự bất kỳ. Nếu kiểm tra ngay ở `type(...)`, ta chưa biết người gọi có định đặt `recipientEmail` sau đó hay không.
- **Ngoài đời**: Nhân viên chỉ kiểm tra ổ bánh lúc gói, không kiểm tra sau mỗi lần thêm nguyên liệu.
- **Giải pháp**: `build()` gọi `require(...)` cho ba trường bắt buộc; lỗi nêu đúng tên trường.
- **Bằng chứng**: `NotificationMessageBuilderTests.missingTypeIsRejected`, `blankRecipientEmailIsRejected`, `missingBookingCodeIsRejected`.
- **Cái giá**: lỗi lộ ra lúc chạy chứ không lúc biên dịch.

### QĐ2. Chuỗi rỗng cũng tính là thiếu

- **Vấn đề**: Dữ liệu cũ có thể lưu email là `""` hoặc toàn khoảng trắng; kiểm tra `null` thôi là chưa đủ.
- **Giải pháp**: `require` từ chối cả `null` lẫn `isBlank()`.
- **Bằng chứng**: `blankRecipientEmailIsRejected`.

### QĐ3. `price` được phép `null`

- **Vấn đề**: Lịch hẹn tạo trước khi có tính phí Strategy không có giá. Bắt buộc giá sẽ làm hỏng email hủy/nhắc của dữ liệu cũ.
- **Ngoài đời**: Hóa đơn cũ không có dòng "giảm giá" vẫn là hóa đơn hợp lệ.
- **Giải pháp**: Không kiểm tra `price`; Director truyền `null` khi lịch không có giá.
- **Bằng chứng**: `priceIsOptionalForLegacyAppointments`.

### QĐ4. Director là bean Spring

- **Vấn đề**: Cả dịch vụ email và Decorator SMS cần cùng công thức.
- **Giải pháp**: `@Component`, tiêm vào cả hai nơi. Builder vẫn là lớp thường, tạo mới mỗi lần qua `aMessage()`.
- **Cái giá**: Director phụ thuộc cấu trúc `Appointment` (bệnh nhân, bác sĩ, ca).

### QĐ5. Không đổi `EmailNotificationMessage`

- **Vấn đề**: Bản tin đi qua RabbitMQ tới worker. Đổi hình dạng record là phải sửa và triển khai lại worker.
- **Ngoài đời**: Đổi cách làm trong bếp, nhưng hộp đựng giao cho shipper giữ nguyên.
- **Giải pháp**: Product giữ đúng 9 trường; Builder chỉ thay cách tạo.
- **Bằng chứng**: `NotificationMessageIntegrationTests.bookingConfirmationCarriesEveryField` kiểm tra bản tin tại `RabbitTemplate`.

### QĐ6. Constructor của Builder là `private`, chỉ vào qua `aMessage()`

- **Lý do**: Mỗi lần dựng bắt đầu từ một Builder mới, không tái sử dụng Builder cũ còn sót trường của lần trước.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu đầu vào thật** của bản tin có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | 9 tham số cùng kiểu `String` truyền theo vị trí | Email ghi sai giờ, sai tên bác sĩ, không ai phát hiện | Gọi theo tên trường | `bookingConfirmationCarriesEveryField` (so từng trường) |
| 2 | Email bệnh nhân rỗng hoặc toàn khoảng trắng | Worker gửi thất bại sau commit, không ai biết | `build()` từ chối `isBlank()` | `blankRecipientEmailIsRejected` |
| 3 | Thiếu loại tin | Worker không biết dùng mẫu thư nào | `build()` từ chối | `missingTypeIsRejected` |
| 4 | Thiếu mã lịch hẹn | Bệnh nhân không tra cứu được; chống trùng không hoạt động | `build()` từ chối | `missingBookingCodeIsRejected` |
| 5 | Lịch hẹn cũ không có giá | Ném lỗi khi gửi email hủy/nhắc cho lịch cũ | `price` tùy chọn, truyền `null` | `priceIsOptionalForLegacyAppointments` |
| 6 | Giá `BigDecimal` in ra dạng khoa học (`3E+5`) | Email hiển thị giá khó đọc | `toPlainString()` | Mã nguồn `NotificationMessageDirector`; giá trị được so trong `bookingConfirmationCarriesEveryField` |
| 7 | Ngày giờ ca nhầm giữa nhắc lịch và đặt lịch | Nhắc sai giờ khám | Mọi công thức đọc từ cùng `Schedule` | `reminderHasReminderTypeAndSlotTimes` |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `NotificationMessageBuilderTests` | 4 | Thiếu từng trường bắt buộc → lỗi nêu tên; thiếu giá → vẫn dựng được |
| `NotificationMessageDirectorTests` | 2 | Mỗi công thức ra đúng loại; nhắc lịch mang đúng ngày giờ ca |
| `NotificationMessageIntegrationTests` | 2 | Qua API (MockMvc, H2): đặt lịch → bản tin tại `RabbitTemplate` đủ 9 trường đúng giá trị; hủy → bản tin loại hủy cho cùng lịch |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Builder là **quy tắc tất định**: cùng lịch hẹn luôn cho cùng bản tin. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Test đơn vị cho từng trường bắt buộc + test tích hợp quan sát bản tin thật tại biên `RabbitTemplate` |
| **Hiệu chuẩn** | Không | "Dữ liệu lịch hẹn" phải khớp "nội dung bản tin": test so từng trường với lịch vừa đặt |
| **Bootstrap** | Không | Kiểm thử biên: `null`, chuỗi rỗng, chuỗi toàn khoảng trắng, lịch cũ không có giá |
| **Công bằng giới** | Builder không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Mọi bệnh nhân nhận cùng mẫu bản tin, chỉ khác dữ liệu lịch hẹn của chính họ |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **SRP** | Builder lo lắp và kiểm tra; Director lo công thức; dịch vụ email lo phát sự kiện |
| **OCP** | Thêm loại thông báo (ví dụ "đổi giờ khám") = thêm một hàm Director, không sửa Builder hay worker |
| **DIP** | Dịch vụ email và Decorator SMS phụ thuộc Director, không tự đọc `Appointment` |
| Dễ đọc | `.doctorName(...)` rõ hơn tham số thứ 5 của constructor |
| Báo lỗi sớm | Thiếu trường bị chặn trong giao dịch, trước khi bản tin vào hàng đợi |
| Dùng lại | Email và SMS cùng một nguồn dữ liệu, không lệch nhau |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Nhiều code | 9 hàm gần trùng tên trường cho một record nhỏ |
| Kiểm tra còn nông | Chỉ "có/không", chưa kiểm tra định dạng email hay ngày giờ hợp lệ |
| Lỗi lúc chạy | Thiếu trường chỉ lộ khi chạy, không lộ khi biên dịch |
| Phải đồng bộ hai nơi | Thêm trường vào Product thì phải thêm hàm vào Builder |
| Không thread-safe | Builder có trạng thái; an toàn vì mỗi lần dựng tạo builder mới qua `aMessage()` |
| Còn để lại | Dịch vụ nhắc lịch đã có nhưng chưa có job định kỳ gọi |

---

## 9. Hướng phát triển

- Thêm job nhắc lịch định kỳ (ví dụ 24 giờ trước giờ khám) gọi `director.reminder(...)`.
- Kiểm tra định dạng email trong `build()` thay vì chỉ kiểm tra rỗng.
- Thêm công thức "đổi giờ khám" khi hệ thống hỗ trợ đổi lịch.
- Nếu có nhiều mẫu thư (tiếng Anh, tiếng Việt), thêm Director theo ngôn ngữ dùng chung Builder.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Builder, Director | `backend/src/main/java/com/datlichkhambenh/backend/builder/` |
| Product | `backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailNotificationMessage.java` |
| Client | `backend/src/main/java/com/datlichkhambenh/backend/service/impl/AsyncEmailNotificationServiceImpl.java` |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/builder/` |
| Sơ đồ | `docs/builder/diagrams/` |
| Script sinh sơ đồ | `scripts/builder/generate-diagrams.py` |
