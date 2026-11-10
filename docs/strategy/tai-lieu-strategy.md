# Tài liệu tính năng: Strategy tính phí khám

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM, nhánh `thaihoa`). Tài liệu này mô tả phần em phụ trách: áp dụng mẫu thiết kế **Strategy** cho việc tính phí khám, kèm quy trình quyền hưởng Bảo hiểm/VIP cần để Strategy dùng được.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Strategy là gì

### 1.1 Định nghĩa

Strategy là một mẫu **hành vi (behavioral)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Định nghĩa một họ thuật toán, đóng gói từng thuật toán vào một lớp riêng và làm cho chúng có thể thay thế cho nhau. Strategy cho phép thuật toán thay đổi độc lập với nơi sử dụng nó.

Nói đơn giản: **cùng một việc, có nhiều cách làm; chọn cách nào tùy tình huống, và nơi gọi không cần biết chi tiết từng cách.**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Strategy** | Interface chung mà mọi cách làm phải tuân theo |
| **Concrete Strategy** | Mỗi lớp là một cách làm cụ thể |
| **Context** | Giữ tham chiếu tới Strategy và gọi nó; không chứa công thức |
| **Client** | Quyết định dùng Strategy nào |

### 1.3 Strategy ngoài đời và trong phần mềm

| Ví dụ | "Một việc" | "Nhiều cách làm" |
|---|---|---|
| Ứng dụng bản đồ | Tìm đường tới điểm đến | Đi bộ, xe máy, ô tô, xe buýt |
| Thanh toán online | Thanh toán đơn hàng | Thẻ, ví điện tử, chuyển khoản, COD |
| Phí vận chuyển | Tính phí giao hàng | Tiết kiệm, nhanh, hỏa tốc |
| Nén file | Nén dữ liệu | ZIP, RAR, 7z |
| Java chuẩn | Sắp xếp danh sách | `Comparator` truyền vào `sort` |
| Java chuẩn | Làm tròn số | `RoundingMode.HALF_UP`, `FLOOR`… |

Điểm chung: **một mục tiêu, nhiều cách thực hiện, chọn lúc chạy**. Khi có thêm cách mới (ví dụ ví điện tử mới), chỉ cần thêm một "cách làm" chứ không viết lại cả màn hình thanh toán.

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Có nhiều biến thể của cùng một thuật toán và có khả năng thêm biến thể mới.
- Muốn tránh chuỗi `if/else`/`switch` lớn rải rác trong nghiệp vụ.
- Muốn kiểm thử từng thuật toán riêng.

Không nên dùng khi:
- Chỉ có một hoặc hai cách làm và gần như không bao giờ thay đổi (thêm lớp là lãng phí).
- Các quy tắc cần **kết hợp** với nhau (cộng dồn giảm giá, phụ thu theo nhiều điều kiện) — khi đó Decorator, Chain of Responsibility hoặc rule engine phù hợp hơn.

---

## 2. Bài toán thực tế và vì sao chọn Strategy

### 2.1 Hiện trạng trước khi làm (commit gốc `2be71ae`)

- Đặt lịch lấy **phí niêm yết của bác sĩ** và chép thẳng vào giá lịch hẹn.
- Bệnh nhân **không xem được giá trước** khi đặt.
- **Không có** khái niệm Bảo hiểm, VIP, hay dữ liệu quyền hưởng.
- Muốn thêm một loại giá thì phải sửa trực tiếp trong `BookingServiceImpl.createBooking`, nơi đang có **khóa bi quan chống hai người đặt trùng ca**. Sửa ở đây rất dễ gây lỗi tranh chấp.

### 2.2 Ví dụ ngoài đời: quầy thu ngân phòng khám

Thu ngân ở phòng khám không học thuộc mọi cách tính. Họ có sẵn **nhiều bảng giá** (giá thường, giá có thẻ bảo hiểm, giá thẻ VIP). Khách đưa thẻ gì thì rút đúng bảng đó ra tính. Khi phòng khám ra gói mới, chỉ cần **in thêm một bảng giá**, không phải đào tạo lại thu ngân.

Một người khác — **lễ tân** — mới là người kiểm tra thẻ thật hay giả, còn hạn hay không. Thu ngân không làm việc đó.

| Ngoài đời | Trong hệ thống |
|---|---|
| Mẫu bảng giá chung | `ConsultationPricingStrategy` (interface) |
| Từng bảng giá | `StandardPricingStrategy`, `InsurancePricingStrategy`, `VipPricingStrategy` |
| Thu ngân | `PricingContext` |
| Lễ tân kiểm thẻ | `BenefitService` |
| Hóa đơn đã in | Snapshot giá lưu trong `appointments` |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| `if/else` trong hàm đặt lịch | Bắt thu ngân thuộc lòng mọi giá; ra gói mới là dạy lại | **Loại**: mỗi loại giá mới phải sửa giao dịch có khóa; báo giá và đặt lịch dễ lệch công thức |
| Bảng luật trong cơ sở dữ liệu (rule engine) | Một cuốn sổ quy tắc khổng lồ ai cũng sửa được | **Loại**: quá nặng cho 3 công thức; khó kiểm thử, khó truy vết |
| Để client tự gửi giá / chọn tùy ý | Khách tự ghi giá lên hóa đơn | **Loại**: ngoài đời không ai chấp nhận; sửa request là được giảm 80% |
| **Strategy** | **Nhiều bảng giá, một thu ngân** | **Chọn**: thêm loại mới không sửa Context; mỗi công thức test riêng; báo giá và đặt lịch dùng chung |

### 2.4 Vì sao Strategy hợp với tính năng này

1. Ba loại giá là **ba thuật toán cùng mục đích** (tính số tiền phải trả từ phí gốc) — đúng định nghĩa "họ thuật toán".
2. Chính sách phòng khám **sẽ thay đổi** (thêm gói, đổi tỷ lệ) — cần mở rộng không sửa code cũ.
3. Mỗi lịch chỉ dùng **một** loại, không cộng dồn — đúng tinh thần "chọn một Strategy".
4. Cần **cùng một công thức** cho hai chức năng (báo giá và đặt lịch).

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Strategy | `strategy/pricing/ConsultationPricingStrategy` | `key()`, `name()`, `discountRate()`, `calculate(basePrice)` |
| Concrete Strategy | `StandardPricingStrategy`, `InsurancePricingStrategy`, `VipPricingStrategy` | Mỗi lớp một công thức, tỷ lệ đọc từ cấu hình |
| Context | `strategy/pricing/PricingContext` | Registry bất biến, chọn theo key, kiểm tra tỷ lệ, làm tròn VND, chặn giá vượt gốc |
| Client | `strategy/service/ConsultationQuoteService`, `service/impl/BookingServiceImpl` | Báo giá; chốt giá khi đặt |
| Cộng tác viên | `strategy/service/BenefitService` | Kiểm tra quyền hưởng đã được Admin duyệt |

### 3.2 Ba thuật toán (phí gốc 300.000đ)

| Loại | Key | Giảm mặc định | Bệnh nhân trả | Cấu hình |
|---|---|---:|---:|---|
| Tiêu chuẩn | `STANDARD` | 0% | 300.000đ | `app.pricing.standard-discount-rate` |
| Bảo hiểm | `INSURANCE` | 80% | 60.000đ | `app.pricing.insurance-discount-rate` |
| VIP | `VIP` | 10% | 270.000đ | `app.pricing.vip-discount-rate` |

Công thức: `giá cuối = phí gốc × (1 − tỷ lệ giảm)`, làm tròn HALF_UP đến đồng. Đây là **chính sách giá nội bộ phòng khám**, không phải khoản bảo hiểm chi trả.

### 3.3 API

| Endpoint | Mục đích |
|---|---|
| `GET /api/v1/appointments/pricing-options?scheduleId=` | Chỉ trả các loại phí bệnh nhân **đang đủ điều kiện** |
| `GET /api/v1/appointments/quote?scheduleId=&strategyKey=` | Báo giá; không giữ ca, không tạo lịch, không gửi email |
| `POST /api/v1/appointments/confirm` | Đặt lịch có đối chiếu báo giá (giá, tỷ lệ, ngày/giờ ca) |
| `POST /api/v1/appointments` | Luồng cũ, giữ tương thích, mặc định Tiêu chuẩn |
| API `PatientBenefitController` / `AdminBenefitController` | Gửi, xem, duyệt, từ chối, thu hồi, gia hạn quyền hưởng |

### 3.4 UML

Mười một sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/strategy/diagrams/`:

| Nhóm | Sơ đồ | File |
|---|---|---|
| Chi tiết theo luồng | Lớp + tuần tự cho **báo giá** | `consultation-quote-class`, `consultation-quote-sequence` |
| Chi tiết theo luồng | Lớp + tuần tự cho **đặt lịch** | `consultation-booking-class`, `consultation-booking-sequence` |
| Tổng quan | Use Case (Bệnh nhân, Admin; «include»/«extend») | `strategy-use-case` |
| Tổng quan | ERD vật lý, crow's foot (bảng/cột «new» của migration 001–005) | `strategy-erd` |
| Tổng quan | DFD mức 0 (ngữ cảnh) và mức 1 (báo giá, quyền hưởng, đặt lịch) | `strategy-dfd-level0`, `strategy-dfd-level1` |
| Tổng quan | State Machine của `BenefitRequest` | `strategy-state-machine` |
| Tổng quan | Activity đặt lịch có tính giá (409 `PRICE_CHANGED`, 403 mất quyền hưởng) | `strategy-activity` |
| Tổng quan | Package `strategy.*` và vai trò trong mẫu Strategy | `strategy-package` |

Sinh lại bằng `generate-quote-diagrams.py`, `generate-booking-diagrams.py`, `generate-overview-diagrams.py`; xuất PNG bằng `export-diagrams.sh`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Context chọn Strategy theo từng yêu cầu, không dùng `setStrategy`

- **Vấn đề**: Sách thường vẽ Context có `setStrategy()`. Nhưng trong Spring, Context là **singleton dùng chung** cho mọi request. Gọi `setStrategy(VIP)` cho bệnh nhân A thì bệnh nhân B đặt cùng lúc có thể bị tính giá VIP.
- **Ngoài đời**: Như thu ngân treo biển "đang áp giá VIP" lên quầy — người xếp hàng sau bị áp nhầm.
- **Giải pháp**: `calculate(key, fee)` nhận key mỗi lần gọi. Registry tạo bằng `Map.copyOf` (bất biến). Hai Strategy trùng key → báo lỗi ngay lúc khởi động.
- **Bằng chứng**: `PricingContextTests.registeredStrategyIsExecutedWithoutChangingContext`, `duplicateKeysFailRatherThanSilentlySelectOneStrategy`.
- **Cái giá**: phải truyền key ở mọi nơi gọi.

### QĐ2. Tách điều kiện sử dụng (quyền hưởng) khỏi công thức

- **Vấn đề**: Biết công thức VIP không có nghĩa là được dùng VIP. Nếu tin key client gửi, ai sửa request cũng được giảm.
- **Ngoài đời**: Lễ tân soát thẻ, thu ngân tính tiền — hai việc, hai người.
- **Giải pháp**: Bệnh nhân gửi yêu cầu kèm mã hồ sơ; Admin đối chiếu ngoài hệ thống, duyệt/từ chối có lý do, thời hạn và lịch sử. Server kiểm tra quyền còn hiệu lực **cả hôm nay và ngày khám** (giờ Việt Nam, tính cả ngày biên) trước khi gọi Strategy. Quyền hưởng **không** phải Strategy thứ tư.
- **Cái giá**: xác minh thủ công, chưa kết nối cơ quan bảo hiểm.

### QĐ3. Server tính lại; giá client chỉ để đối chiếu

- **Vấn đề**: Nếu lưu số tiền trình duyệt gửi, người dùng sửa request là đổi được giá.
- **Ngoài đời**: Thu ngân không để khách tự ghi số tiền.
- **Giải pháp**: `/confirm` gửi giá đã xem; server tính lại bằng `PricingContext`. Lệch → `409 PRICE_CHANGED`, không lưu gì.

### QĐ4. Snapshot giá khi chốt

- **Vấn đề**: Nếu lịch hẹn chỉ tham chiếu phí bác sĩ hiện tại, bác sĩ tăng phí là lịch cũ đổi giá theo.
- **Ngoài đời**: Bảng giá đổi, hóa đơn đã in không in lại.
- **Giải pháp**: Lưu `pricingKey`, `basePrice`, `discountRate`, `price`, `benefitRequestId` (migration 004, 005). Thu hồi quyền hay đổi phí sau đó không ảnh hưởng.

### QĐ5. Giá đổi phải xác nhận lại, không tự đặt

- **Vấn đề**: Giữa lúc xem giá và bấm đặt, bác sĩ có thể đổi phí.
- **Ngoài đời**: Giá ở kệ khác giá ở quầy, nhân viên phải hỏi khách.
- **Giải pháp**: UI lấy báo giá mới, hiển thị cũ → mới, giữ triệu chứng, đổi nút thành "Xác nhận giá mới và đặt lịch". Không tự POST lại.

### QĐ6. Mất quyền hưởng: bắt chọn lại, không tự hạ về Tiêu chuẩn

- **Lý do**: Bệnh nhân có thể muốn đổi ngày khám để vẫn được hưởng bảo hiểm; tự đổi về giá đầy đủ là quyết định thay họ.
- **Mã**: `403 BENEFIT_UNAVAILABLE`.

### QĐ7. Khóa theo thứ tự Schedule → Doctor → BenefitRequest

- **Vấn đề**: Hai người cùng đặt một ca; Admin thu hồi quyền hoặc bác sĩ đổi phí đúng lúc bệnh nhân đang đặt.
- **Ngoài đời**: Hai người không thể cùng ngồi một ghế; phải có người "giữ ghế" trong lúc làm thủ tục.
- **Giải pháp**: Khóa bi quan theo thứ tự cố định, kiểm tra lại **dưới khóa**. Đọc ID quyền hưởng trước rồi mới khóa để tránh dùng bản ghi cũ trong persistence context.
- **Cái giá**: các lượt đặt cùng một bác sĩ chờ nhau ngắn hạn.

### QĐ8. Endpoint `/confirm` mới + giữ `POST /appointments` cũ

- **Vấn đề**: Nếu thêm trường báo giá vào endpoint cũ dưới dạng tùy chọn, client có thể bỏ hết trường để vượt kiểm tra giá.
- **Giải pháp**: Controller `/confirm` tự gắn cờ `quoteRequired` nội bộ; JSON client không tắt được. Endpoint cũ giữ cho client chưa cập nhật.

### QĐ9. Không cộng dồn; bệnh nhân tự chọn khi có cả Bảo hiểm và VIP

- **Lý do**: Mỗi hóa đơn ngoài đời thường chỉ áp một chương trình; hệ thống không tự chọn mức có lợi nhất vì đó là quyết định của bệnh nhân.

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu đầu vào thật** của hệ thống có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Phí bác sĩ `NULL`, âm, hoặc vượt `DECIMAL(12,2)` | Giá âm, lỗi khi lưu | Phí 0 hợp lệ; thiếu/âm/tràn bị từ chối **trước và sau** làm tròn | `feesOutsideStorageCapacityAreRejectedBeforeRounding` |
| 2 | Phép giảm sinh số lẻ đồng (ví dụ 59.999,5) | Không thể thu tiền lẻ | `BigDecimal` + `HALF_UP` đến đồng; không dùng `double` | `standardRateIsConfigurableAndAmountsRoundHalfUp` |
| 3 | Tỷ lệ cấu hình gõ sai (âm, > 1, `null`) | Giá âm hoặc lớn hơn phí gốc | Ép miền [0,1], tối đa 18 chữ số lẻ; sai thì ứng dụng không khởi động; Context chặn giá cuối > gốc | `nullDiscountIsRejected`, `zeroAndFullDiscountAreValid` |
| 4 | JavaScript `Number` mất chữ số của tỷ lệ dài | Báo `PRICE_CHANGED` oan | Thêm trường `discountRateExact` dạng chuỗi; cột `DECIMAL(19,18)` | `configuredRateSurvivesJavaScriptRoundTripAsExactString` |
| 5 | Lịch hẹn cũ không có loại giá / tỷ lệ | Gán nhầm Bảo hiểm/VIP cho lịch cũ | Giữ `NULL`, **không backfill** dữ liệu không biết | `test-mysql-upgrades.py` |
| 6 | Máy khách ở múi giờ khác Việt Nam | Quyền hết hạn/chưa hiệu lực sai ngày | Mọi ngày tính theo `Asia/Ho_Chi_Minh` | `browser-vietnam-date.cjs` (Chrome giả lập Honolulu 00:30 giờ VN) |
| 7 | Mã hồ sơ có khoảng trắng; ngày kết thúc trước ngày bắt đầu | Dữ liệu bẩn, quyền vô nghĩa | `trim()`, kiểm tra khoảng ngày khi gửi và khi duyệt | `BenefitRequestIntegrationTests` |
| 8 | Ca bị xóa hoặc đổi giờ sau khi báo giá | Đặt nhầm giờ khác giờ bệnh nhân đã xem | Bắt buộc gửi ngày/giờ đã báo; so dưới khóa; 404/409 `SLOT_UNAVAILABLE` | `deletedQuotedSlot…`, `shiftedQuotedSlot…` |
| 9 | Nâng cấp schema bằng `ddl-auto` | Mất/không kiểm soát dữ liệu thật | 5 file SQL nâng cấp 001–005, chạy theo thứ tự sau sao lưu | `test-mysql-upgrades.py` |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả (clean build ngày 05/11/2026, `docs/strategy/execution.md`)

| Tầng | Số lượng | Kiểm tra gì |
|---|---:|---|
| Java (JUnit, MockMvc, H2 + MySQL thật) | 118 | Công thức, API, phân quyền, khóa, tranh chấp, commit/rollback, email sau commit |
| Chrome (trình duyệt thật) | 16 kịch bản | Toàn luồng: gửi quyền → Admin duyệt → báo giá → đặt → đọc snapshot; giá đổi; mất quyền; mất ca |
| Node | 11 | Phản hồi trễ bị bỏ, không dùng báo giá cũ |
| SQL nâng cấp MySQL | 5 | Giữ nguyên giá lịch cũ, metadata `NULL` |

Review độc lập tìm ra 2 lỗi quan trọng (ca bị xóa không phục hồi; ca đổi giờ vẫn chấp nhận báo giá cũ). Cả hai được tái hiện bằng test đỏ trước khi sửa, sau đó xanh.

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy (confidence), hiệu chuẩn (calibration), bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Tính năng này là **quy tắc tất định**: cùng đầu vào luôn cho cùng một giá. Vì vậy tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không — không có xác suất dự đoán | Mức tin cậy dựa trên bằng chứng: 4 tầng kiểm thử, MySQL thật, trình duyệt thật, review độc lập |
| **Hiệu chuẩn** | Không — không có điểm số cần hiệu chuẩn | "Giá báo" phải khớp "giá chốt". Mọi sai lệch bị phát hiện bằng `PRICE_CHANGED`; test round-trip tỷ lệ 18 chữ số qua JavaScript |
| **Bootstrap** | Không — không có mẫu thống kê để lấy lại mẫu | Kiểm thử biên (0, âm nhỏ, trần sau làm tròn, HALF_UP, ngày biên hiệu lực) và kiểm thử tranh chấp đồng thời (hai người cùng ca, thu hồi đang giữ khóa, đổi phí đang giữ khóa) |
| **Công bằng giới** | Công thức không đọc giới tính, tuổi hay dữ liệu nhân khẩu học | Giá chỉ phụ thuộc **phí bác sĩ + loại quyền đã duyệt**: cùng điều kiện ⇒ cùng giá. Rủi ro còn lại là khâu Admin duyệt thủ công; mọi quyết định lưu lý do, người duyệt, thời điểm để kiểm toán. Đề xuất: định kỳ thống kê tỷ lệ duyệt/từ chối theo nhóm để phát hiện thiên lệch |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **OCP** (mở để mở rộng, đóng để sửa) | Ticket #13 thêm VIP chỉ bằng một lớp `@Component`; `PricingContext` không đổi |
| **SRP** (một trách nhiệm) | Công thức (Strategy), điều phối/làm tròn (Context), điều kiện (BenefitService) tách riêng |
| **DIP** (phụ thuộc trừu tượng) | Service gọi interface; Spring tiêm danh sách Strategy |
| Nhất quán | Báo giá và đặt lịch gọi cùng Context → giá xem và giá chốt cùng một công thức |
| Dễ kiểm thử | Test từng công thức không cần dựng cả luồng đặt lịch |
| An toàn đồng thời | Không có trạng thái "Strategy đang chọn" trong singleton |
| Truy vết | Snapshot giải thích được vì sao lịch cũ có giá đó |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Nhiều lớp hơn | 3 công thức gần giống nhau vẫn cần interface + Context + 3 lớp; với bài nhỏ là cồng kềnh |
| Client phải biết chọn Strategy | Phải làm thêm `BenefitService` và API `pricing-options` |
| Không hợp quy tắc kết hợp | Cộng dồn ưu đãi, phụ thu cuối tuần/ngoài giờ sẽ cần Decorator hoặc rule engine |
| Cấu hình phải nhất quán | Nhiều instance khác tỷ lệ → `PRICE_CHANGED` liên tục |
| Khóa gây chờ | Khóa bác sĩ làm các lượt đặt cùng bác sĩ chờ nhau ngắn hạn |
| Ngoài phạm vi | Chưa thanh toán, chưa kết nối cơ quan bảo hiểm, xác minh hồ sơ thủ công, chưa outbox cho email |
| Còn để lại | Chưa có test cố ý rollback *sau khi* đã đăng ký sự kiện email |

---

## 9. Hướng phát triển

- Kết nối API cơ quan bảo hiểm để xác minh tự động thay vì Admin đối chiếu tay.
- Thêm phụ thu theo khung giờ bằng Decorator bọc quanh Strategy.
- Lưu tỷ lệ trong bảng cấu hình có phiên bản để mọi instance đọc cùng một nguồn.
- Bổ sung outbox để bảo đảm email được gửi lại khi RabbitMQ lỗi.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Thuật toán | `backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/` |
| Service | `backend/src/main/java/com/datlichkhambenh/backend/strategy/service/` |
| Tích hợp đặt lịch | `backend/src/main/java/com/datlichkhambenh/backend/service/impl/BookingServiceImpl.java` |
| SQL nâng cấp | `backend/src/main/resources/db/upgrade/strategy/001`–`005` |
| Test Java | `backend/src/test/java/com/datlichkhambenh/backend/strategy/` |
| Test trình duyệt | `frontend/tests/strategy/` |
| UML | `docs/strategy/diagrams/` |
