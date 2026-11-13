# Tài liệu tính năng: Decorator bọc dịch vụ thông báo

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Decorator** để thêm cho dịch vụ thông báo các hành vi: chống gửi trùng, gửi thêm SMS và ghi lịch sử, mà không sửa dịch vụ gửi email gốc.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Decorator là gì

### 1.1 Định nghĩa

Decorator là một mẫu **cấu trúc (structural)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Gắn thêm trách nhiệm cho một đối tượng một cách động. Decorator là cách mở rộng chức năng linh hoạt thay cho kế thừa.

Nói đơn giản: **giữ nguyên đối tượng gốc, bọc thêm từng lớp; mỗi lớp làm thêm một việc rồi chuyển tiếp vào trong, và bên ngoài vẫn thấy nó là "cùng một loại".**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Component** | Giao diện chung |
| **ConcreteComponent** | Đối tượng gốc |
| **Decorator** | Cùng giao diện, giữ tham chiếu tới Component bên trong, mặc định chuyển tiếp |
| **ConcreteDecorator** | Thêm hành vi trước/sau khi chuyển tiếp |

### 1.3 Decorator ngoài đời và trong phần mềm

| Ví dụ | "Gốc" | "Các lớp bọc" |
|---|---|---|
| Ly cà phê | Cà phê đen | Thêm sữa, thêm đường, thêm kem |
| Gửi bưu phẩm | Gói hàng | Bọc chống sốc, bảo hiểm, giao nhanh |
| Ảnh chụp | Ảnh gốc | Khung, kính, thẻ tên |
| Java chuẩn | `FileInputStream` | `BufferedInputStream`, `GZIPInputStream` |
| Java chuẩn | `List` | `Collections.unmodifiableList`, `synchronizedList` |

Điểm chung: **mỗi lớp bọc độc lập, chồng theo tổ hợp nào cũng được, và thứ tự chồng có ý nghĩa.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Cần thêm nhiều hành vi phụ quanh một hành vi chính, có thể bật/tắt hoặc đổi thứ tự.
- Kế thừa sẽ sinh bùng nổ lớp theo tổ hợp (A, A+B, A+C, A+B+C…).
- Muốn giữ nguyên lớp gốc đã chạy ổn.

Không nên dùng khi:
- Chỉ có một hành vi thêm và không bao giờ thay đổi.
- Giao diện Component quá lớn: mỗi Decorator phải chuyển tiếp mọi hàm.

---

## 2. Bài toán thực tế và vì sao chọn Decorator

### 2.1 Hiện trạng trước khi làm

Dịch vụ thông báo (`AsyncEmailNotificationServiceImpl`) chỉ biết gửi email. Phòng khám cần thêm:

- **lịch sử thông báo** để tra cứu khi bệnh nhân nói "tôi không nhận được email";
- **chống gửi trùng** (cùng loại tin, cùng lịch hẹn), ví dụ khi nhắc lịch bị gọi hai lần;
- **gửi thêm SMS** qua kênh Adapter.

Nếu nhét cả ba vào dịch vụ email, một lớp sẽ ôm 4 trách nhiệm và mỗi hành vi mới là một lần sửa lớp đã chạy ổn.

### 2.2 Ví dụ ngoài đời: gửi bưu phẩm

Nhân viên bưu điện nhận một gói hàng (email gốc). Khách có thể yêu cầu thêm: **kiểm tra đã gửi chưa** (khỏi gửi hai lần), **nhắn tin báo người nhận**, **ghi sổ biên nhận**. Mỗi dịch vụ là một lớp bọc quanh gói hàng; gói hàng bên trong không đổi. Thứ tự quan trọng: phải kiểm tra trùng **trước** khi nhắn tin, nếu không người nhận sẽ nhận tin nhắn hai lần.

| Ngoài đời | Trong hệ thống |
|---|---|
| "Gửi bưu phẩm" (dịch vụ chung) | `AsyncEmailNotificationService` (Component) |
| Gói hàng gốc | `AsyncEmailNotificationServiceImpl` (ConcreteComponent) |
| Lớp bọc chung | `NotificationServiceDecorator` (abstract Decorator) |
| Kiểm tra đã gửi chưa | `DeduplicationDecorator` |
| Nhắn tin báo người nhận | `SmsNotificationDecorator` |
| Ghi sổ biên nhận | `AuditLogDecorator` |
| Quầy sắp xếp thứ tự dịch vụ | `NotificationDecoratorConfig` |
| Sổ biên nhận | Bảng `notification_log` |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Sửa thẳng `AsyncEmailNotificationServiceImpl` | Một nhân viên tự làm mọi dịch vụ | **Loại**: một lớp ôm email, SMS, lịch sử, chống trùng; mỗi hành vi mới là một lần sửa |
| Kế thừa (`EmailWithLog`, `EmailWithLogAndSms`…) | In sẵn mọi tổ hợp gói dịch vụ | **Loại**: bùng nổ số lớp theo tổ hợp; không đổi thứ tự được |
| Thêm observer cho từng hành vi | Mỗi phòng tự nghe tin rồi làm | **Loại**: observer không **chặn** được lần gửi trùng; thứ tự không đảm bảo |
| **Decorator** | **Bọc từng lớp dịch vụ quanh gói hàng** | **Chọn**: mỗi hành vi một lớp nhỏ, cùng giao diện, xếp chồng ở một chỗ cấu hình |

### 2.4 Vì sao Decorator hợp với tính năng này

1. Ba hành vi đều **bao quanh** cùng một việc (gửi thông báo), không phải ba việc riêng.
2. Chống trùng phải **chặn** được việc gửi: chỉ Decorator (đứng trước và quyết định có chuyển tiếp không) làm được.
3. **Thứ tự có ý nghĩa** và phải nằm một chỗ.
4. Dịch vụ email gốc đã chạy ổn với hàng đợi sau commit; không muốn sửa nó.

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Component | `service/AsyncEmailNotificationService` | `sendBookingConfirmation`, `sendAppointmentReminder`, `sendCancellationNotice` |
| ConcreteComponent | `service/impl/AsyncEmailNotificationServiceImpl` | Dựng bản tin (Builder), xếp hàng email sau commit |
| Decorator | `decorator/NotificationServiceDecorator` | abstract, giữ `inner`, mặc định chuyển tiếp |
| ConcreteDecorator 1 | `decorator/DeduplicationDecorator` | Đã có EMAIL SENT cùng loại + mã → ghi SKIPPED_DUPLICATE, dừng |
| ConcreteDecorator 2 | `decorator/SmsNotificationDecorator` | Gọi bên trong trước, rồi SMS qua `NotificationChannel`; lỗi SMS chỉ ghi lại |
| ConcreteDecorator 3 | `decorator/AuditLogDecorator` | Ghi EMAIL SENT (lỗi → FAILED rồi ném lại) |
| Lắp ráp | `decorator/NotificationDecoratorConfig` | Bean `@Primary` duy nhất; mọi nơi tiêm dịch vụ thông báo nhận bản đã bọc |
| Lưu trữ | `decorator/NotificationLog`, `decorator/NotificationLogRepository` | Bảng `notification_log` |

### 3.2 Thứ tự chuỗi bọc

```
DeduplicationDecorator          ← ngoài cùng: trùng thì dừng ở đây
  └ SmsNotificationDecorator    ← gọi vào trong trước, rồi gửi SMS
      └ AuditLogDecorator       ← ghi EMAIL SENT / FAILED
          └ AsyncEmailNotificationServiceImpl   ← gốc: dựng bản tin, xếp hàng email
```

| Tình huống | Chống trùng | SMS | Ghi lịch sử | Email gốc | Dòng `notification_log` |
|---|---|---|---|---|---|
| Lần đầu, bệnh nhân có số | chuyển tiếp | gửi | ghi | xếp hàng | EMAIL SENT, SMS SENT |
| Lần đầu, không có số | chuyển tiếp | bỏ qua | ghi | xếp hàng | EMAIL SENT, SMS SKIPPED_NO_PHONE |
| Lần đầu, SMS tắt | chuyển tiếp | bỏ qua | ghi | xếp hàng | EMAIL SENT, SMS SKIPPED_DISABLED |
| Lần hai cùng loại, cùng lịch | **dừng** | — | — | — | EMAIL SKIPPED_DUPLICATE |
| Nhà cung cấp SMS sập | chuyển tiếp | lỗi, ghi lại | ghi | xếp hàng | EMAIL SENT, SMS FAILED |

### 3.3 Bảng `notification_log`

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính |
| `type` | VARCHAR(40) | `BOOKING_CONFIRMED` / `APPOINTMENT_REMINDER` / `BOOKING_CANCELLED` |
| `booking_code` | VARCHAR(50) | Mã lịch hẹn (không khóa ngoại) |
| `channel` | VARCHAR(10) | `EMAIL` / `SMS` |
| `status` | VARCHAR(30) | SENT, FAILED, SKIPPED_DUPLICATE, SKIPPED_NO_PHONE, SKIPPED_DISABLED |
| `note` | VARCHAR(255) | Lý do / chi tiết, cắt còn 255 ký tự |
| `created_at` | DATETIME(6) | Thời điểm ghi |

Chỉ mục `idx_notification_log_booking (booking_code, type)` phục vụ câu hỏi chống trùng. MySQL nâng cấp tay: `backend/src/main/resources/db/upgrade/decorator/001-notification-log.sql`.

### 3.4 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/decorator/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `decorator-class` | Component, ConcreteComponent, Decorator, 3 ConcreteDecorator |
| Tuần tự | `decorator-sequence` | Chống trùng → SMS → ghi lịch sử → email, kể cả nhánh trùng |
| ERD | `decorator-erd` | Bảng `notification_log` mới, liên kết logic qua `booking_code` |
| DFD | `decorator-dfd` | Yêu cầu thông báo → chuỗi bọc → hàng đợi email / kênh SMS / lịch sử |
| Use Case | `decorator-use-case` | Nhận thông báo «include» chống trùng, gửi SMS, gửi email; cả hai kênh «include» ghi lịch sử |

Sinh lại bằng `python3 scripts/decorator/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/decorator/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Thứ tự chống trùng → SMS → ghi lịch sử → email

- **Vấn đề**: Spec ban đầu đặt "ghi lịch sử" ngoài cùng. Khi đó nó không biết lớp trong đã bỏ qua do trùng và sẽ ghi SENT sai. Nếu SMS đứng trước chống trùng, lần gửi trùng vẫn sinh SMS thứ hai.
- **Ngoài đời**: Kiểm tra "đã gửi chưa" trước rồi mới nhắn tin; ghi sổ biên nhận lúc gói hàng thật sự được giao.
- **Giải pháp**: Chống trùng ngoài cùng; ghi lịch sử sát email nên chỉ ghi lần gửi thật.
- **Bằng chứng**: `SmsNotificationIntegrationTests.duplicateReminderSendsNoSecondSms` (đã thử đảo thứ tự, test đỏ).
- **Cái giá**: thứ tự là "luật ngầm" trong `NotificationDecoratorConfig`, chỉ được bảo vệ bằng test.

### QĐ2. Chống trùng đọc bảng, không đọc bộ nhớ

- **Vấn đề**: Đếm trong bộ nhớ thì khởi động lại server là quên hết, gửi lại.
- **Ngoài đời**: Tra sổ biên nhận chứ không nhờ trí nhớ của nhân viên ca trước.
- **Giải pháp**: `existsByTypeAndBookingCodeAndChannelAndStatus(type, code, "EMAIL", "SENT")`.
- **Bằng chứng**: `NotificationDecoratorIntegrationTests.duplicateReminderIsSkippedAndRecorded`.

### QĐ3. Lỗi SMS không lan ra

- **Vấn đề**: SMS là kênh phụ. Nhà cung cấp SMS sập không được làm hỏng email hay việc đặt lịch.
- **Giải pháp**: `SmsNotificationDecorator.alsoSend` bắt `RuntimeException`, ghi `SMS FAILED`.
- **Bằng chứng**: `SmsFailureIntegrationTests.bookingAndEmailSucceedWhenSmsCrashes` (đặt lịch 201, email vẫn đi, SMS FAILED).

### QĐ4. Lỗi email thì ghi FAILED rồi ném lại

- **Vấn đề**: Email là kênh chính. Nuốt lỗi email sẽ che giấu sự cố.
- **Giải pháp**: `AuditLogDecorator` ghi `EMAIL FAILED` kèm thông điệp rồi ném lại; observer phía trên quyết định không làm hỏng đặt lịch.

### QĐ5. Không đặt khóa ngoại sang `appointments`

- **Lý do**: Liên kết qua `booking_code` để lịch sử độc lập với vòng đời lịch hẹn (xóa/đổi lịch không kéo theo lịch sử).

### QĐ6. Lịch sử ghi trong giao dịch đặt lịch

- **Vấn đề**: Nếu lịch rollback mà lịch sử vẫn còn, tra cứu sẽ thấy "đã gửi" cho lịch không tồn tại.
- **Giải pháp**: Decorator chạy trong giao dịch (observer dùng `@EventListener` đồng bộ); rollback thì không còn dòng nào.

### QĐ7. Một bean `@Primary` duy nhất

- **Lý do**: Mọi chỗ tiêm `AsyncEmailNotificationService` (observer, sau này job nhắc lịch) tự nhận bản đã bọc; không ai vô tình gọi thẳng bản gốc.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu và tình huống thật** của thông báo có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu / tình huống thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Nhắc lịch bị gọi hai lần | Bệnh nhân nhận hai email, hai SMS | Chống trùng ngoài cùng, đọc bảng | `duplicateReminderIsSkippedAndRecorded`, `duplicateReminderSendsNoSecondSms` |
| 2 | Bệnh nhân không có số điện thoại | Lịch sử đầy SMS FAILED giả | Ghi SKIPPED_NO_PHONE | `bookingRecordsTheEmailAndSkipsSmsForPatientWithoutPhone` |
| 3 | Nhà cung cấp SMS ném lỗi | Đặt lịch thất bại vì kênh phụ | Bắt lỗi, ghi SMS FAILED | `bookingAndEmailSucceedWhenSmsCrashes` |
| 4 | Thông điệp lỗi rất dài | Lỗi khi lưu cột `note` | Cắt còn 255 ký tự | Mã nguồn `NotificationLog` |
| 5 | Server khởi động lại giữa hai lần gửi | Chống trùng bằng bộ nhớ quên mất | Đọc bảng `notification_log` | `duplicateReminderIsSkippedAndRecorded` |
| 6 | Lịch hẹn bị xóa / đổi | Khóa ngoại chặn xóa hoặc xóa luôn lịch sử | Không khóa ngoại, liên kết qua `booking_code` | Schema `001-notification-log.sql` |
| 7 | Giao dịch đặt lịch rollback | Lịch sử ghi "đã gửi" cho lịch không tồn tại | Ghi trong cùng giao dịch | Thiết kế (xem QĐ6) |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `NotificationDecoratorIntegrationTests` | 2 | Qua API: đặt lịch → EMAIL SENT + SMS SKIPPED_NO_PHONE; nhắc lịch hai lần → 1 email, có dòng SKIPPED_DUPLICATE |
| `SmsNotificationIntegrationTests` | 2 | Có số → nhà cung cấp nhận 1 tin, SMS SENT; nhắc trùng → không có SMS thứ hai (khóa thứ tự chuỗi) |
| `SmsFailureIntegrationTests` | 1 | Nhà cung cấp luôn ném lỗi → đặt lịch 201, email vẫn đi, SMS FAILED |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Decorator là **chuỗi xử lý tất định**: cùng lịch sử và cùng yêu cầu luôn cho cùng kết quả. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Test qua API quan sát ở biên: bảng `notification_log`, client SMS giả, `RabbitTemplate` |
| **Hiệu chuẩn** | Không | "Lịch sử ghi lại" phải khớp "điều đã xảy ra": email thật sự xếp hàng ↔ EMAIL SENT; bị chặn ↔ SKIPPED_DUPLICATE |
| **Bootstrap** | Không | Thử các tổ hợp: có/không số, SMS sập, gửi trùng; đảo thứ tự chuỗi để chứng minh test bắt được |
| **Công bằng giới** | Decorator không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Mọi bệnh nhân đi qua cùng chuỗi bọc; chỉ khác ở việc có số điện thoại hay không |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **OCP** | Thêm hành vi (ví dụ thử lại, giới hạn tần suất) = thêm một lớp bọc; không sửa email gốc |
| **SRP** | Mỗi Decorator một việc: chống trùng, SMS, ghi lịch sử |
| **LSP** | Bản đã bọc dùng thay bản gốc ở mọi nơi, cùng giao diện |
| **DIP** | Observer phụ thuộc `AsyncEmailNotificationService`, không biết có bao nhiêu lớp bọc |
| Linh hoạt | Đổi thứ tự hay bỏ một lớp chỉ sửa `NotificationDecoratorConfig` |
| Truy vết | Bảng lịch sử trả lời được "đã gửi chưa, qua kênh nào, vì sao không gửi" |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| SMS đi trước commit, email đi sau commit | Nếu giao dịch hỏng ở bước commit cuối, SMS đã gửi mà lịch không được lưu. Các nhánh từ chối đã kiểm thử đều xảy ra trước khi gửi |
| "SENT" chưa phải "đã nhận" | EMAIL SENT nghĩa là đã đưa vào hàng đợi |
| Khó debug | Chuỗi nhiều lớp bọc khó lần theo hơn một lớp duy nhất |
| Thứ tự dễ vỡ | Đảo thứ tự là sai hành vi; chỉ test bảo vệ |
| Phải chuyển tiếp đủ hàm | Thêm hàm vào Component phải thêm vào mọi Decorator |
| Ngoài phạm vi | Chưa có màn hình Admin xem lịch sử |

---

## 9. Hướng phát triển

- Dời SMS sang sau commit bằng `TransactionSynchronization` để hết rủi ro "SMS đi mà lịch không lưu".
- Thêm Decorator thử lại có giới hạn cho email lỗi tạm thời.
- Thêm màn hình Admin tra cứu `notification_log` theo mã lịch hẹn.
- Thêm Decorator giới hạn tần suất (ví dụ tối đa N SMS / bệnh nhân / ngày).

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Decorator, cấu hình, lịch sử | `backend/src/main/java/com/datlichkhambenh/backend/decorator/` |
| Component, ConcreteComponent | `backend/src/main/java/com/datlichkhambenh/backend/service/AsyncEmailNotificationService.java`, `service/impl/AsyncEmailNotificationServiceImpl.java` |
| SQL nâng cấp | `backend/src/main/resources/db/upgrade/decorator/001-notification-log.sql` |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/decorator/` |
| Sơ đồ | `docs/decorator/diagrams/` |
| Script sinh sơ đồ | `scripts/decorator/generate-diagrams.py` |
