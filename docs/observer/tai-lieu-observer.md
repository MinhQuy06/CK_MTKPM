# Tài liệu tính năng: Observer phản ứng khi đặt / hủy lịch

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Observer** để tách các phản ứng sau khi đặt/hủy lịch (gửi email, làm mới cache ca trống) khỏi dịch vụ đặt lịch.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Observer là gì

### 1.1 Định nghĩa

Observer là một mẫu **hành vi (behavioral)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Định nghĩa quan hệ một–nhiều giữa các đối tượng, để khi một đối tượng đổi trạng thái thì mọi đối tượng phụ thuộc được báo và tự cập nhật.

Nói đơn giản: **một nơi xảy ra chuyện, nhiều nơi cần biết; nơi xảy ra chuyện chỉ "thông báo", không cần biết ai nghe và họ làm gì.**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Subject** | Nơi trạng thái thay đổi; giữ danh sách observer và báo cho họ |
| **Observer** | Giao diện "nhận thông báo" |
| **ConcreteSubject** | Subject cụ thể phát thông báo khi có chuyện |
| **ConcreteObserver** | Mỗi lớp là một phản ứng cụ thể |

### 1.3 Observer ngoài đời và trong phần mềm

| Ví dụ | "Chuyện xảy ra" | "Ai cần biết" |
|---|---|---|
| Kênh YouTube | Đăng video mới | Người đăng ký nhận thông báo |
| Nhóm Zalo lớp | Giáo viên đăng lịch thi | Mọi thành viên nhóm |
| Báo giấy | Số báo mới phát hành | Người đặt báo dài hạn |
| Giao diện | Bấm nút | Các `ActionListener` đã đăng ký |
| Spring | `publishEvent(...)` | Các `@EventListener` khớp kiểu sự kiện |

Điểm chung: **người phát không biết danh sách người nghe; thêm người nghe mới không làm người phát thay đổi.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Một thay đổi kéo theo nhiều phản ứng độc lập, và danh sách phản ứng có thể dài thêm.
- Muốn nơi phát sự kiện không phụ thuộc vào nơi phản ứng.

Không nên dùng khi:
- Chỉ có một phản ứng và gần như không thay đổi (gọi thẳng dễ đọc hơn).
- Các phản ứng phải chạy **đúng thứ tự** hoặc phải **chặn** được việc chính (khi đó Decorator hay Chain of Responsibility hợp hơn).

---

## 2. Bài toán thực tế và vì sao chọn Observer

### 2.1 Hiện trạng trước khi làm

- `BookingServiceImpl` sau khi lưu lịch hẹn **tự gọi** `redisCacheService.evictSlotCache(...)` rồi `emailNotificationService.sendBookingConfirmation(...)`; hủy lịch cũng vậy.
- Dịch vụ đặt lịch phụ thuộc trực tiếp email và cache, dù việc của nó chỉ là đặt lịch (khóa ca, kiểm quyền hưởng, tính giá, lưu).
- Muốn thêm một phản ứng (SMS, thống kê…) phải mở và sửa đúng hàm đang giữ **khóa bi quan chống đặt trùng ca**, rất dễ gây lỗi tranh chấp.

### 2.2 Ví dụ ngoài đời: thông báo trong phòng khám

Khi lễ tân chốt một lịch khám, họ không tự chạy đi báo từng nơi. Họ **bấm một nút "đã đặt"** trên hệ thống; bảng ca trống ở sảnh tự cập nhật, bệnh nhân tự nhận tin nhắn xác nhận. Phòng khám muốn thêm "báo cho phòng xét nghiệm chuẩn bị" thì chỉ cần cho phòng xét nghiệm **đăng ký nhận tin**, lễ tân làm việc như cũ.

| Ngoài đời | Trong hệ thống |
|---|---|
| Lễ tân bấm nút "đã đặt / đã hủy" | `BookingServiceImpl` gọi `events.publishEvent(...)` |
| Hệ thống phát tin | `ApplicationEventPublisher` của Spring |
| Tin "đã đặt", "đã hủy" | `AppointmentBookedEvent`, `AppointmentCancelledEvent` |
| Bộ phận gửi tin nhắn cho bệnh nhân | `AppointmentEmailObserver` |
| Bảng ca trống ở sảnh | `SlotCacheObserver` |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Giữ gọi trực tiếp | Lễ tân tự chạy đi báo từng nơi | **Loại**: mỗi phản ứng mới là một lần sửa dịch vụ đặt lịch |
| Tự viết danh sách listener trong Subject | Lễ tân tự giữ sổ danh bạ người cần báo | **Loại**: làm lại thứ Spring đã có, phải tự lo đăng ký/hủy đăng ký |
| `@TransactionalEventListener(AFTER_COMMIT)` cho observer | Chỉ báo sau khi sổ sách đã đóng | **Loại**: observer chạy ngoài giao dịch, mất khả năng ghi lịch sử thông báo cùng giao dịch (Decorator cần) |
| **Sự kiện miền + `@EventListener` của Spring** | **Bấm nút, ai đăng ký thì nhận** | **Chọn**: Spring giữ danh sách observer; observer chạy đồng bộ trong giao dịch; email vẫn đi sau commit nhờ cơ chế hàng đợi có sẵn |

### 2.4 Vì sao Observer hợp với tính năng này

1. Đặt/hủy lịch kéo theo **nhiều phản ứng độc lập** (email, cache) và danh sách này **sẽ dài thêm**.
2. Dịch vụ đặt lịch là chỗ **nhạy cảm nhất** (khóa, giá, quyền hưởng); càng ít lý do để sửa càng tốt.
3. Các phản ứng **không cần chặn** việc đặt lịch và **không phụ thuộc thứ tự**.
4. Spring đã có sẵn hạ tầng sự kiện, không phải tự viết Subject.

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| ConcreteSubject | `service/impl/BookingServiceImpl` | Gọi `events.publishEvent(...)` ở cuối `createBooking` và `cancelBooking`; không còn phụ thuộc email, cache |
| Subject (danh sách observer) | `ApplicationEventPublisher` (Spring) | Gọi đồng bộ mọi `@EventListener` khớp kiểu sự kiện |
| Sự kiện | `observer/AppointmentBookedEvent`, `observer/AppointmentCancelledEvent` | record mang lịch hẹn vừa lưu |
| ConcreteObserver | `observer/AppointmentEmailObserver` | Gửi xác nhận / thông báo hủy; lỗi chỉ ghi log |
| ConcreteObserver | `observer/SlotCacheObserver` | Xóa cache ca trống theo bác sĩ + ngày làm việc |

### 3.2 Hai sự kiện, hai phản ứng

| Sự kiện | Phát khi | `AppointmentEmailObserver` | `SlotCacheObserver` |
|---|---|---|---|
| `AppointmentBookedEvent` | Lịch đã lưu, sau mọi kiểm tra | `sendBookingConfirmation` | `evictSlotCache(doctorUserId, workDate)` |
| `AppointmentCancelledEvent` | Lịch đã chuyển CANCELLED | `sendCancellationNotice` | `evictSlotCache(doctorUserId, workDate)` (giờ vừa trống mở lại thành ca mới) |

### 3.3 Luồng chạy

Bệnh nhân đặt lịch → `BookingServiceImpl` khóa ca, kiểm quyền hưởng, tính giá, lưu → `publishEvent(AppointmentBookedEvent)` → Spring gọi lần lượt hai observer **trong cùng giao dịch** → observer email gọi dịch vụ thông báo (đã Decorator bọc) → bản tin được giữ lại tới khi commit (`EmailQueuePublisher`, `AFTER_COMMIT`) rồi mới vào RabbitMQ.

### 3.4 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/observer/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `observer-class` | Subject, hai sự kiện, hai observer |
| Tuần tự | `observer-sequence` | Đặt lịch → phát sự kiện → email + cache → commit → hàng đợi |
| ERD | `observer-erd` | Bảng `appointments`, `schedules`; observer không thêm bảng |
| DFD | `observer-dfd` | Sự kiện lịch hẹn → hàng đợi email / Redis cache |
| Use Case | `observer-use-case` | Đặt / hủy lịch «include» phát sự kiện; sự kiện «include» gửi email và làm mới ca trống |

Sinh lại bằng `python3 scripts/observer/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/observer/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Phát sự kiện ở cuối, sau mọi kiểm tra

- **Vấn đề**: Nếu phát sự kiện trước khi kiểm tra xong, lượt đặt bị từ chối (ca kín, giá đổi, mất quyền hưởng) vẫn sinh email và xóa cache.
- **Ngoài đời**: Lễ tân chỉ bấm "đã đặt" khi giấy tờ đã đủ.
- **Giải pháp**: `publishEvent` là dòng cuối của `createBooking` / `cancelBooking`; mọi nhánh từ chối ném lỗi trước đó.
- **Bằng chứng**: `AppointmentObserverIntegrationTests.changedPriceIsRejectedWithoutAnyReaction`, `unavailableBenefitIsRejectedWithoutAnyReaction`, `takenSlotIsRejectedWithoutASecondReaction`.

### QĐ2. Observer email tự bắt lỗi

- **Vấn đề**: Observer chạy đồng bộ trong giao dịch. Lỗi chưa bắt sẽ rollback cả lịch đã đặt.
- **Ngoài đời**: Máy nhắn tin hỏng không có nghĩa là hủy lịch khám của bệnh nhân.
- **Giải pháp**: `try/catch` trong `onBooked` / `onCancelled`, chỉ `log.error`. Giữ đúng hành vi cũ (trước đây lời gọi email cũng nằm trong `try/catch`).
- **Cái giá**: lỗi gửi email chỉ thấy trong log (và bảng lịch sử của Decorator).

### QĐ3. Không dùng `AFTER_COMMIT` cho observer

- **Vấn đề**: Nếu observer chạy sau commit, Decorator không ghi được lịch sử thông báo trong cùng giao dịch, và chống trùng không còn nhất quán với dữ liệu lịch.
- **Giải pháp**: `@EventListener` thường (đồng bộ, trong giao dịch). Email vẫn chỉ rời hệ thống sau commit vì tầng hàng đợi đã có `@TransactionalEventListener(AFTER_COMMIT)`.
- **Bằng chứng**: `bookingSendsExactlyOneConfirmationAfterCommit`.

### QĐ4. Không có observer ghi log riêng

- **Vấn đề**: Ghi lịch sử thông báo có thể làm bằng một observer thứ ba, nhưng observer không biết lần gửi đó có bị bỏ qua do trùng hay không.
- **Giải pháp**: Ranh giới rõ: **Observer quyết định *khi nào, ai phản ứng*; Decorator quyết định *gửi thông báo thì thêm hành vi gì*.**

### QĐ5. Không dựa vào thứ tự giữa các observer

- **Lý do**: Email và cache độc lập; nếu sau này cần thứ tự thì dùng `@Order`, không viết code giả định thứ tự.

### QĐ6. Sự kiện là record mang `Appointment`

- **Vấn đề**: Observer cần cả bệnh nhân (email), bác sĩ và ngày (cache).
- **Giải pháp**: Gửi lịch hẹn vừa lưu thay vì chỉ ID, tránh mỗi observer phải truy vấn lại.
- **Cái giá**: Observer phụ thuộc entity JPA; chỉ an toàn vì chạy trong cùng giao dịch.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu và tình huống thật** quanh sự kiện lịch hẹn có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu / tình huống thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Lượt đặt bị từ chối giữa chừng | Email "đặt thành công" cho lịch không tồn tại | Phát sự kiện sau mọi kiểm tra | `changedPriceIsRejectedWithoutAnyReaction` |
| 2 | Hai người cùng đặt một ca | Người thứ hai nhận email, cache bị xóa thừa | Người thứ hai bị từ chối trước khi phát | `takenSlotIsRejectedWithoutASecondReaction` |
| 3 | Giao dịch rollback sau khi observer đã chạy | Email gửi cho lịch không được lưu | Email chỉ rời hệ thống sau commit | `bookingSendsExactlyOneConfirmationAfterCommit` |
| 4 | Cache ca trống cũ sau khi đặt/hủy | Bệnh nhân khác thấy ca đã kín là còn trống, hoặc ngược lại | Xóa cache đúng bác sĩ + ngày | `bookingEvictsTheBookedDoctorsSlotCacheOnce`, `cancellingSendsOneNoticeAndEvictsTheSlotCache` |
| 5 | Lịch hẹn không gắn ca (dữ liệu lỗi) | `NullPointerException` làm hỏng giao dịch | `SlotCacheObserver.evict` bỏ qua khi `slot == null` | Mã nguồn `SlotCacheObserver` |
| 6 | Redis hoặc dịch vụ email lỗi | Lịch đặt xong bị rollback | Email observer bắt lỗi; `RedisCacheServiceImpl` tự bắt lỗi Redis | Mã nguồn `AppointmentEmailObserver`, `RedisCacheServiceImpl` |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `AppointmentObserverIntegrationTests` | 6 | Qua API (MockMvc, H2): đặt lịch → đúng 1 email sau commit và 1 lần xóa cache đúng bác sĩ + ngày; hủy → 1 email hủy + 1 lần xóa cache; đổi giá / mất quyền hưởng / ca kín → không email, không xóa cache |
| `BookingServiceImplTests` (test cũ, đã sửa có lý do) | — | Nay kiểm tra hủy lịch **phát** `AppointmentCancelledEvent` thay vì gọi email (collaborator bị bỏ có chủ đích) |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Observer là **cơ chế tất định**: cùng sự kiện luôn kích hoạt cùng tập phản ứng. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Test qua API đếm **chính xác** số email và số lần xóa cache (đúng 1, không phải "ít nhất 1") |
| **Hiệu chuẩn** | Không | "Sự kiện xảy ra" phải khớp "phản ứng": thành công → có phản ứng; bị từ chối → không phản ứng nào |
| **Bootstrap** | Không | Kiểm thử các nhánh từ chối (giá đổi, mất quyền hưởng, ca kín) và tình huống tranh chấp (hai người cùng ca) |
| **Công bằng giới** | Observer không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Mọi lịch hẹn phát cùng loại sự kiện và nhận cùng phản ứng |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **SRP** | Dịch vụ đặt lịch chỉ đặt lịch; gửi email và làm mới cache ở lớp riêng |
| **OCP** | Thêm phản ứng mới (thống kê, báo phòng xét nghiệm…) = thêm một lớp `@EventListener`, không sửa `BookingServiceImpl` |
| **DIP** | `BookingServiceImpl` chỉ phụ thuộc `ApplicationEventPublisher`, không phụ thuộc email hay Redis |
| Giảm rủi ro | Hàm có khóa bi quan ít lý do bị sửa hơn |
| Dễ kiểm thử | Kiểm tra từng phản ứng độc lập; kiểm tra dịch vụ đặt lịch chỉ cần xem có phát sự kiện không |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Khó lần theo | Đọc `BookingServiceImpl` không thấy ngay ai phản ứng |
| Chạy đồng bộ | Một observer chậm làm chậm việc đặt lịch |
| Phụ thuộc kỷ luật | Observer mới ném lỗi chưa bắt sẽ làm hỏng cả giao dịch đặt lịch; mỗi observer phải tự lo lỗi |
| Thứ tự không đảm bảo | Không dùng được cho phản ứng cần chạy trước/sau nhau |
| Gắn với entity | Sự kiện mang `Appointment` JPA; không dùng an toàn ngoài giao dịch |

---

## 9. Hướng phát triển

- Chuyển các observer nặng (gửi thống kê, gọi hệ thống ngoài) sang `@Async` hoặc `AFTER_COMMIT` khi không cần chung giao dịch.
- Thêm observer ghi số liệu (số lịch đặt/hủy theo ngày) cho màn hình thống kê Admin.
- Thêm outbox để sự kiện không mất khi hàng đợi lỗi.
- Nếu cần thứ tự, đánh `@Order` rõ ràng và có test khóa thứ tự.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Sự kiện, observer | `backend/src/main/java/com/datlichkhambenh/backend/observer/` |
| Subject | `backend/src/main/java/com/datlichkhambenh/backend/service/impl/BookingServiceImpl.java` |
| Hàng đợi sau commit | `backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailQueuePublisher.java` |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/observer/` |
| Sơ đồ | `docs/observer/diagrams/` |
| Script sinh sơ đồ | `scripts/observer/generate-diagrams.py` |
