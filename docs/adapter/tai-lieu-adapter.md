# Tài liệu tính năng: Adapter kênh SMS của nhà cung cấp

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Adapter** để cắm dịch vụ SMS của nhà cung cấp bên ngoài (eSMS) vào hệ thống thông báo mà không để chi tiết của nhà cung cấp lan vào nghiệp vụ.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Adapter là gì

### 1.1 Định nghĩa

Adapter là một mẫu **cấu trúc (structural)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Chuyển giao diện của một lớp thành giao diện khác mà client mong đợi, để các lớp vốn không tương thích làm việc được với nhau.

Nói đơn giản: **hai bên đều đúng nhưng không khớp nhau; thay vì sửa một trong hai, ta đặt một "bộ chuyển đổi" ở giữa.**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Target** | Giao diện client muốn dùng |
| **Adaptee** | Lớp có sẵn với giao diện khác (thường của bên thứ ba) |
| **Adapter** | Cài đặt Target, bên trong gọi Adaptee và dịch dữ liệu qua lại |
| **Client** | Chỉ biết Target |

### 1.3 Adapter ngoài đời và trong phần mềm

| Ví dụ | Bên cần dùng | Bên có sẵn | Bộ chuyển đổi |
|---|---|---|---|
| Củ sạc du lịch | Phích cắm laptop châu Âu | Ổ điện Việt Nam | Củ chuyển đổi chân cắm |
| Đầu đọc thẻ nhớ | Cổng USB của máy tính | Thẻ microSD | Đầu đọc USB |
| Phiên dịch viên | Khách nói tiếng Việt | Bác sĩ nói tiếng Anh | Người phiên dịch |
| Java chuẩn | Code đọc ký tự (`Reader`) | Luồng byte (`InputStream`) | `InputStreamReader` |
| Java chuẩn | Code dùng `List` | Mảng | `Arrays.asList(...)` |

Điểm chung: **không sửa bên nào, chỉ thêm lớp ở giữa; đổi bên có sẵn thì chỉ đổi bộ chuyển đổi.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Phải dùng thư viện/dịch vụ bên ngoài có giao diện không khớp với hệ thống.
- Muốn đổi nhà cung cấp mà không sửa nghiệp vụ.
- Muốn kiểm thử nghiệp vụ mà không gọi dịch vụ thật (thay Adaptee bằng bản giả).

Không nên dùng khi:
- Hai bên đã khớp sẵn (Adapter chỉ chuyển tiếp, thêm lớp vô ích).
- Ta sở hữu cả hai bên và có thể sửa trực tiếp cho khớp.

---

## 2. Bài toán thực tế và vì sao chọn Adapter

### 2.1 Hiện trạng trước khi làm

- Hệ thống chỉ gửi **email**. Phòng khám muốn gửi thêm **SMS** khi đặt/hủy/nhắc lịch.
- API của nhà cung cấp SMS (eSMS) có giao diện riêng, không khớp với hệ thống:
  - nhận số dạng **quốc tế** (`84912345678`) trong khi phòng khám lưu `0912 345 678`;
  - nhận **văn bản thô**, không biết `EmailNotificationMessage`;
  - trả **mã kết quả** (`CodeResult = "100"` là đã nhận) thay vì ném ngoại lệ;
  - cần khóa API, khóa bí mật, loại tin, cờ sandbox.
- Nếu gọi thẳng eSMS ở chỗ gửi thông báo, mọi chi tiết trên lan vào nghiệp vụ; đổi nhà cung cấp là sửa nghiệp vụ.

### 2.2 Ví dụ ngoài đời: phiên dịch viên ở phòng khám

Bác sĩ (hệ thống) chỉ nói "báo cho bệnh nhân lịch khám này". Bệnh nhân ở nước ngoài, chỉ hiểu tiếng Anh và số điện thoại có mã quốc gia. Phòng khám thuê **phiên dịch viên**: nhận lời bác sĩ, dịch sang đúng định dạng bên kia hiểu, rồi báo lại bác sĩ "đã chuyển" hay "không liên lạc được". Đổi phiên dịch viên thì bác sĩ vẫn nói câu cũ.

| Ngoài đời | Trong hệ thống |
|---|---|
| Câu bác sĩ quen nói | `NotificationChannel.send(patientPhone, message)` (Target) |
| Bên chỉ hiểu định dạng riêng | `SmsGatewayClient.submitSms(msisdn, text)` (Adaptee) |
| Nhà cung cấp cụ thể | `EsmsGatewayClient` (eSMS thật), `DisabledSmsGatewayClient` (đang tắt) |
| Phiên dịch viên | `SmsChannelAdapter` |
| Báo lại "đã chuyển / không liên lạc được" | `DeliveryResult` (SENT / SKIPPED_NO_PHONE / SKIPPED_DISABLED / FAILED) |
| Người nhờ chuyển lời | `SmsNotificationDecorator` (Client) |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Gọi eSMS trực tiếp trong Decorator SMS | Bác sĩ tự học tiếng của từng bệnh nhân | **Loại**: định dạng số, mã lỗi, khóa API của nhà cung cấp lẫn vào logic thông báo |
| Class adapter (kế thừa client eSMS) | Phiên dịch viên "là" người nước ngoài | **Loại**: Java đơn kế thừa; gắn chặt vào một nhà cung cấp, khó thay bằng bản giả khi test |
| Sửa hệ thống cho khớp eSMS | Bác sĩ đổi cả cách làm việc theo một bệnh nhân | **Loại**: đổi nhà cung cấp là sửa lại toàn bộ |
| **Object adapter (giữ client bên trong)** | **Thuê phiên dịch viên** | **Chọn**: Target là interface của phòng khám; thay eSMS bằng nhà khác hoặc bản giả chỉ là đổi bean |

### 2.4 Vì sao Adapter hợp với tính năng này

1. eSMS là **dịch vụ bên ngoài**, ta không sửa được giao diện của nó.
2. Có **ba chỗ không khớp** rõ ràng: định dạng số, kiểu dữ liệu bản tin, cách báo lỗi.
3. Nhà cung cấp SMS **có thể đổi** (Viettel, VNPT, Twilio…) vì lý do giá.
4. SMS tốn tiền theo tin: phải **kiểm thử được mà không gửi thật**.

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Target | `adapter/NotificationChannel` | `name()`, `send(patientPhone, message): DeliveryResult` |
| Adaptee (giao diện) | `adapter/SmsGatewayClient` | `submitSms(msisdn, text): int` với `OK`, `REJECTED`, `DISABLED` |
| Adaptee thật | `adapter/EsmsGatewayClient` | Gọi REST eSMS (`SendMultipleMessage_V4_post_json`), đọc `CodeResult` |
| Adaptee khi tắt | `adapter/DisabledSmsGatewayClient` | Không gửi gì, ghi log, trả `DISABLED` |
| Adapter | `adapter/SmsChannelAdapter` | Bỏ qua khi không có số, chuẩn hóa số, soạn SMS ngắn, dịch mã → `DeliveryResult` |
| Chọn Adaptee | `adapter/SmsGatewayConfig` | `app.sms.enabled=true` → eSMS; ngược lại → bản đã tắt |
| Hỗ trợ | `adapter/VietnamPhone`, `adapter/DeliveryResult` | Chuẩn hóa `+84`; kết quả gửi |
| Client | `decorator/SmsNotificationDecorator` | Chỉ biết `NotificationChannel` |
| Bản giả (chỉ test) | `src/test/.../adapter/SimulatedSmsGatewayClient` | Ghi lại tin đã nhận để test kiểm tra |

### 3.2 Ba chỗ "dịch" của Adapter

| Chiều | Hệ thống | Nhà cung cấp | Ai dịch |
|---|---|---|---|
| Số điện thoại | `0912 345 678`, `84912345678`, `+84912345678` | `+84912345678` (eSMS bỏ dấu `+` khi gửi) | `VietnamPhone.toInternational` |
| Nội dung | `EmailNotificationMessage` 9 trường | Văn bản ngắn, không dấu | `SmsChannelAdapter.text` |
| Kết quả | `DeliveryResult` | `int` (`OK` / `REJECTED` / `DISABLED`), hoặc ngoại lệ khi lỗi mạng/HTTP | `SmsChannelAdapter.send` |

Ví dụ nội dung SMS: `Da dat lich kham BK123: 2026-11-20 08:00-08:30, <tên bác sĩ>.` Phần chữ cố định không dấu; tên bác sĩ lấy nguyên như đang lưu (chưa bỏ dấu).

### 3.3 Cấu hình

| Biến môi trường | Mặc định | Ý nghĩa |
|---|---|---|
| `SMS_ENABLED` | `false` | Bật SMS thật (tốn phí theo tin) |
| `ESMS_API_KEY`, `ESMS_SECRET_KEY` | rỗng | Bắt buộc khi bật; thiếu thì ứng dụng **không khởi động** |
| `ESMS_SMS_TYPE` | `8` | Loại tin eSMS |
| `ESMS_SANDBOX` | `false` | `true`: eSMS nhận nhưng không gửi, không trừ tiền |
| `ESMS_ENDPOINT` | REST eSMS | Đổi khi nhà cung cấp đổi địa chỉ |

### 3.4 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/adapter/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `adapter-class` | Target, Adapter, Adaptee (eSMS, tắt), Client |
| Tuần tự | `adapter-sequence` | Decorator SMS → Adapter → chuẩn hóa số → eSMS → `DeliveryResult` |
| ERD | `adapter-erd` | Adapter đọc `patients.phone`; kết quả được Decorator ghi vào `notification_log` |
| DFD | `adapter-dfd` | Bản tin + số điện thoại → Adapter → nhà cung cấp SMS |
| Use Case | `adapter-use-case` | Nhận SMS «include» gửi qua kênh chung; «extend» bỏ qua khi không có số / SMS tắt |

Sinh lại bằng `python3 scripts/adapter/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/adapter/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Object adapter, không phải class adapter

- **Vấn đề**: Kế thừa client eSMS gắn Adapter vào đúng một nhà cung cấp và không thay được bằng bản giả khi test.
- **Ngoài đời**: Thuê phiên dịch viên, không bắt bác sĩ trở thành người nước ngoài.
- **Giải pháp**: `SmsChannelAdapter` giữ `SmsGatewayClient` qua constructor; Spring tiêm bản thật, bản tắt hoặc bản giả.
- **Bằng chứng**: `SmsChannelAdapterTests` chạy với `SimulatedSmsGatewayClient`.

### QĐ2. Không có số điện thoại là "bỏ qua", không phải lỗi

- **Vấn đề**: Số điện thoại không bắt buộc với bệnh nhân. Ghi FAILED sẽ làm lịch sử đầy lỗi giả.
- **Giải pháp**: `null` hoặc rỗng → `SKIPPED_NO_PHONE`, không gọi nhà cung cấp.
- **Bằng chứng**: `patientWithoutPhoneIsSkippedWithoutCallingTheVendor`.

### QĐ3. Số sai định dạng là FAILED và không gọi nhà cung cấp

- **Vấn đề**: Gửi số sai tốn một lượt gửi và nhận về lỗi khó hiểu.
- **Ngoài đời**: Phiên dịch viên thấy số điện thoại thiếu chữ số thì báo lại ngay, không gọi thử.
- **Giải pháp**: `VietnamPhone` trả rỗng → `FAILED` kèm lý do `Số điện thoại không hợp lệ: …`.
- **Bằng chứng**: `malformedNumberFailsWithoutCallingTheVendor`, `VietnamPhoneTests.malformedNumbersAreRejected`.

### QĐ4. SMS tắt là trạng thái riêng `SKIPPED_DISABLED`

- **Vấn đề**: SMS tắt mặc định (tốn phí). Nếu trả FAILED, người vận hành tưởng hệ thống lỗi.
- **Giải pháp**: `DisabledSmsGatewayClient` trả `DISABLED`; Adapter đổi thành `SKIPPED_DISABLED`.
- **Bằng chứng**: `DisabledSmsChannelTests.disabledGatewayYieldsSkippedNotFailed`.

### QĐ5. Thiếu khóa API thì không khởi động

- **Vấn đề**: Bật `SMS_ENABLED=true` mà quên khóa sẽ chỉ lộ ra khi bệnh nhân đầu tiên đặt lịch.
- **Giải pháp**: Constructor `EsmsGatewayClient` ném `IllegalStateException` → ứng dụng dừng ngay lúc khởi động.
- **Bằng chứng**: `EsmsGatewayClientTests.refusesToStartWithoutCredentials`.

### QĐ6. Lỗi mạng/HTTP ném ngoại lệ, từ chối nghiệp vụ trả mã

- **Vấn đề**: Hai loại lỗi khác nhau: nhà cung cấp **từ chối** tin (sai số, hết tiền) và **không gọi được** (mạng, HTTP 5xx).
- **Giải pháp**: `CodeResult` khác `100` → `REJECTED` → `FAILED` có mã; lỗi mạng/HTTP → ngoại lệ, Decorator bắt và ghi FAILED.
- **Bằng chứng**: `vendorRejectionBecomesRejectedCode`, `httpFailureIsThrownSoTheDecoratorCanLogIt`.

### QĐ7. SMS không dấu

- **Lý do**: Tin có dấu phải mã hóa Unicode, mỗi tin chỉ chứa khoảng 70 ký tự thay vì 160; tin không dấu gọn trong một tin, rẻ hơn. eSMS gửi với `IsUnicode = 0`.
- **Cái giá**: tên bác sĩ chưa được bỏ dấu; nếu tên có dấu, nhà cung cấp có thể hiển thị sai ký tự.

### QĐ8. Adapter không ghi lịch sử

- **Lý do**: Adapter chỉ dịch và trả `DeliveryResult`; ghi gì vào lịch sử là việc của Decorator SMS. Mỗi lớp một trách nhiệm.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu đầu vào thật** của kênh SMS có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Số có khoảng trắng, dấu chấm, gạch (`0912.345.678`) | Nhà cung cấp từ chối | Bỏ ký tự phân cách | `commonFormatsBecomeInternational` |
| 2 | Số dạng nội địa `0912…` | Nhà cung cấp cần mã quốc gia | Đổi sang `+84912…` | `localNumberIsConvertedBeforeReachingTheVendor` |
| 3 | Số đã có `84` hoặc `+84` | Thêm `+84` lần nữa thành số sai | Nhận diện và giữ nguyên phần thuê bao | `commonFormatsBecomeInternational` |
| 4 | Số thiếu/thừa chữ số, bắt đầu bằng `0` sau mã vùng | Mất một lượt gửi, lỗi khó hiểu | Thuê bao phải đúng 9 chữ số, không bắt đầu bằng 0 → FAILED, không gọi | `malformedNumbersAreRejected`, `malformedNumberFailsWithoutCallingTheVendor` |
| 5 | Bệnh nhân không có số (`null`, rỗng) | Lịch sử đầy FAILED giả | `SKIPPED_NO_PHONE` | `patientWithoutPhoneIsSkippedWithoutCallingTheVendor` |
| 6 | Nội dung email 9 trường có dấu | Tin dài, tách nhiều tin, tốn phí | Soạn tin ngắn không dấu | `sendingThroughTheChannelReachesTheVendorInItsOwnFormat` |
| 7 | eSMS trả `CodeResult` khác `100` | Tưởng đã gửi | Đổi thành `FAILED` kèm mã | `vendorRejectionBecomesRejectedCode` |
| 8 | eSMS trả HTTP lỗi / mất mạng | Lỗi làm hỏng đặt lịch | Ném ngoại lệ, Decorator bắt và ghi FAILED | `httpFailureIsThrownSoTheDecoratorCanLogIt`, `SmsFailureIntegrationTests` |
| 9 | Ghi chú lỗi dài hơn cột | Lỗi khi lưu lịch sử | `NotificationLog` cắt còn 255 ký tự | Mã nguồn `NotificationLog` |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `SmsChannelAdapterTests` | 4 (một test tham số hóa) | Qua bean `NotificationChannel`: nhà cung cấp nhận đúng `+84…` và nội dung có mã lịch; số nội địa được đổi; số `null`/rỗng → SKIPPED_NO_PHONE; số sai → FAILED; hai trường hợp sau không gọi nhà cung cấp |
| `VietnamPhoneTests` | 2 (tham số hóa) | Các dạng số phổ biến và các dạng sai |
| `EsmsGatewayClientTests` | 3 | Máy chủ HTTP giả lập eSMS: từ chối → `REJECTED`; HTTP lỗi → ngoại lệ; thiếu khóa → không khởi tạo |
| `DisabledSmsChannelTests` | 1 | SMS tắt → `SKIPPED_DISABLED`, không phải FAILED |
| `SmsNotificationIntegrationTests` (chung với Decorator) | 2 | Qua API: bệnh nhân có số đặt lịch → nhà cung cấp nhận đúng 1 tin |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Adapter là **phép dịch tất định**: cùng số và cùng bản tin luôn cho cùng yêu cầu gửi. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Kiểm thử ở cả hai phía: bản giả trong bộ nhớ và máy chủ HTTP giả lập đúng định dạng eSMS |
| **Hiệu chuẩn** | Không | "Kết quả báo về" phải khớp "điều thật sự xảy ra": nhà cung cấp nhận → SENT; không gọi → SKIPPED; gọi mà bị từ chối → FAILED |
| **Bootstrap** | Không | Kiểm thử bảng các dạng số (đúng và sai), số rỗng, nhà cung cấp từ chối, HTTP lỗi, thiếu khóa |
| **Công bằng giới** | Adapter không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Mọi bệnh nhân có số hợp lệ nhận cùng mẫu tin; ai không có số đều được bỏ qua như nhau |

Giới hạn trung thực: **chưa thử với tài khoản eSMS thật**. Bước tiếp theo an toàn là bật `ESMS_SANDBOX=true`.

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **SRP** | Adapter chỉ dịch; client eSMS chỉ gọi HTTP; Decorator chỉ quyết định khi nào gửi và ghi gì |
| **OCP** | Đổi nhà cung cấp = viết một lớp `SmsGatewayClient` mới và đăng ký bean; không sửa Adapter hay Decorator |
| **DIP** | Decorator phụ thuộc `NotificationChannel`; Adapter phụ thuộc `SmsGatewayClient`, không phụ thuộc eSMS |
| **ISP** | `NotificationChannel` chỉ có 2 hàm, đủ cho mọi kênh (SMS, Zalo…) |
| Kiểm thử không tốn phí | Bản giả và máy chủ HTTP giả; SMS thật tắt mặc định |
| An toàn vận hành | Thiếu khóa thì dừng khi khởi động; SMS tắt không bị nhầm là lỗi |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Thêm lớp | Target, Adapter, Adaptee, cấu hình, bản tắt: 5 lớp cho một lời gọi HTTP |
| Chưa thử thật | Chưa gửi với tài khoản eSMS thật |
| Thiếu độ bền | Chưa có retry, giới hạn tốc độ, xác nhận bệnh nhân đã nhận (chỉ biết nhà cung cấp đã nhận) |
| Số điện thoại hẹp | Chỉ nhận số Việt Nam 10 chữ số kiểu hiện hành; số nước ngoài bị coi là sai |
| Nội dung cố định | Mẫu tin nằm trong Adapter; muốn nhiều mẫu cần tách ra. Tên bác sĩ chưa bỏ dấu |

---

## 9. Hướng phát triển

- Thử với `ESMS_SANDBOX=true` rồi tài khoản thật; ghi `SMSID` vào lịch sử để tra cứu.
- Thêm retry có giới hạn cho lỗi mạng tạm thời.
- Thêm kênh mới (Zalo OA, Firebase push) bằng một Adapter `NotificationChannel` khác.
- Tách mẫu nội dung SMS ra cấu hình để đổi lời không phải build lại; bỏ dấu cả tên bác sĩ.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Target, Adapter, Adaptee | `backend/src/main/java/com/datlichkhambenh/backend/adapter/` |
| Client | `backend/src/main/java/com/datlichkhambenh/backend/decorator/SmsNotificationDecorator.java` |
| Cấu hình | `backend/src/main/resources/application.yml` (khối `app.sms`) |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/adapter/` |
| Sơ đồ | `docs/adapter/diagrams/` |
| Script sinh sơ đồ | `scripts/adapter/generate-diagrams.py` |
