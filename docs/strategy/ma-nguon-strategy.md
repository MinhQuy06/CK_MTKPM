# Bản đồ mã nguồn: Strategy tính phí khám

Danh sách toàn bộ file code liên quan đến mẫu Strategy, kèm vai trò, hàm chính và số dòng để tra nhanh. Đường dẫn tính từ thư mục gốc repo; bấm vào link để mở file.

Ký hiệu cột **Vai trò**: ⭐ = thành phần chính của mẫu Strategy, 🔗 = điểm tích hợp, 🛂 = điều kiện sử dụng (quyền hưởng), 🧪 = kiểm thử.

Thư mục gốc backend: `backend/src/main/java/com/datlichkhambenh/backend/` (viết tắt **`B/`**).

---

## 1. Thứ tự nên đọc khi ôn bài

1. [ConsultationPricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/ConsultationPricingStrategy.java): interface, 23 dòng kể cả chú thích.
2. [StandardPricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/StandardPricingStrategy.java): một Concrete Strategy mẫu.
3. [PricingContext.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/PricingContext.java): Context, hàm `calculate` ở dòng 47.
4. [BookingOffer.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/BookingOffer.java): Client duy nhất của Strategy (giá chào): `quote` dòng 61, `lockAndConfirm` dòng 78.
5. [BookingServiceImpl.java](../../backend/src/main/java/com/datlichkhambenh/backend/service/impl/BookingServiceImpl.java): đặt lịch gọi `lockAndConfirm` rồi lưu, dòng 44–79.
6. [BenefitService.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/service/BenefitService.java): điều kiện sử dụng (quyền hưởng còn hiệu lực), dòng 141–172.

---

## 2. Lõi mẫu Strategy — `B/strategy/pricing/`

| File | Vai trò | Nội dung chính | Dòng |
| --- | --- | --- | --- |
| [ConsultationPricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/ConsultationPricingStrategy.java) | ⭐ Strategy | Interface: `key()`, `name()`, `discountRate()`, `calculate(basePrice)`, `simulated()` | 9–23 |
| [StandardPricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/StandardPricingStrategy.java) | ⭐ Concrete Strategy | Key `STANDARD`, giảm mặc định 0; kiểm tra tỷ lệ trong [0,1] ở constructor | 13–18, 26 |
| [InsurancePricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/InsurancePricingStrategy.java) | ⭐ Concrete Strategy | Key `INSURANCE`, giảm mặc định 0.8 | 11, 13, 20 |
| [VipPricingStrategy.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/VipPricingStrategy.java) | ⭐ Concrete Strategy | Key `VIP`, giảm mặc định 0.1 | 11, 13, 20 |
| [PricingContext.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/PricingContext.java) | ⭐ Context | Constructor nhận `List<ConsultationPricingStrategy>` từ Spring, dựng registry bất biến, báo lỗi trùng key (22–30); `supports` (32); `choices` (37–43); `calculate(key, fee)` (47–65); `wholeVnd` làm tròn HALF_UP và kiểm tra giới hạn (68–77) | 18–78 |
| [PricingAmounts.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/PricingAmounts.java) | ⭐ Kết quả | Record: key, tên, phí gốc, tỷ lệ, giá cuối | 5 |
| [PricingChoice.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/pricing/PricingChoice.java) | ⭐ Kết quả | Record: key + tên loại phí để hiện trên UI | 3 |

### Đoạn code cốt lõi (để chiếu khi bị hỏi)

```java
// Strategy
public interface ConsultationPricingStrategy {
    String key();
    String name();
    BigDecimal discountRate();
    BigDecimal calculate(BigDecimal basePrice);
    boolean simulated();
}

// Concrete Strategy
@Component
public class VipPricingStrategy implements ConsultationPricingStrategy {
    public String key() { return "VIP"; }
    public BigDecimal calculate(BigDecimal basePrice) {
        return basePrice.multiply(BigDecimal.ONE.subtract(rate));
    }
}

// Context: Spring tiêm mọi @Component implement interface vào List
public PricingContext(List<ConsultationPricingStrategy> strategies) { ... Map.copyOf(registered); }

public PricingAmounts calculate(String key, BigDecimal fee) {
    ConsultationPricingStrategy strategy = strategies.get(key == null ? "STANDARD" : key);
    BigDecimal base  = wholeVnd(fee);
    BigDecimal price = wholeVnd(strategy.calculate(base));
    return new PricingAmounts(strategy.key(), strategy.name(), base, rate, price, strategy.simulated());
}
```

Muốn thêm loại giá mới: tạo một lớp `@Component` implement interface. `PricingContext` tự nhận, không sửa dòng nào.

---

## 3. Client của Strategy

| File | Vai trò | Nội dung chính | Dòng |
| --- | --- | --- | --- |
| [strategy/offer/BookingOffer.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/BookingOffer.java) | ⭐ Client (giá chào) | `pricingTypes` (51): loại phí áp dụng được; `quote` (61): giá chào chỉ đọc; `lockAndConfirm` (78): khóa ca → bác sĩ → quyền hưởng, kiểm tra lại, so với `Expectation` | 30–191 |
| | | Luật dùng chung: `bookable` (104), `requireAgreedSlotTime`, `supported`, `pricingKey` (151), `requireUnchangedPrice` (164) | |
| [strategy/offer/Expectation.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/Expectation.java) | ⭐ Đầu vào | `ConfirmedQuote` (route `/confirm`), `LegacyQuote`, `LegacyStandard`; Facade tạo | |
| [strategy/offer/BookingRejection.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/BookingRejection.java) | ⭐ Lý do từ chối | 7 hằng: SLOT_MISSING/SLOT_UNAVAILABLE (`SLOT_UNAVAILABLE` 404/409), PRICE_CHANGED, BENEFIT_UNAVAILABLE, UNSUPPORTED_STRATEGY, DOCTOR_UNAVAILABLE, QUOTE_REQUIRED | |
| [strategy/offer/Offer.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/Offer.java), [LockedOffer.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/offer/LockedOffer.java) | ⭐ Kết quả | Giá chào; giá chào đã khóa (ca, bác sĩ, quyền hưởng, số tiền) | |
| [service/impl/BookingServiceImpl.java](../../backend/src/main/java/com/datlichkhambenh/backend/service/impl/BookingServiceImpl.java) | 🔗 Điều phối đặt lịch | `createBooking(patientId, scheduleId, symptoms, expectation)`: `lockAndConfirm` (46), lưu snapshot, đóng ca, phát `AppointmentBookedEvent` (77) | 44–79 |

---

## 4. API (Controller)

| File | Endpoint | Dòng |
| --- | --- | --- |
| [strategy/controller/ConsultationQuoteController.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/controller/ConsultationQuoteController.java) | `GET /api/v1/appointments/quote` → `BookingOffer.quote`, chuyển `Offer` thành `ConsultationQuoteDto` | 36–44 |
| | `GET /api/v1/appointments/pricing-options` → `BookingOffer.pricingTypes` | 46–51 |
| [controller/BookingController.java](../../backend/src/main/java/com/datlichkhambenh/backend/controller/BookingController.java) 🔗 + [facade/BookingFacade.java](../../backend/src/main/java/com/datlichkhambenh/backend/facade/BookingFacade.java) | `POST /api/v1/appointments/confirm`: Facade tạo `ConfirmedQuote` (Facade dòng 30) | 37–44 |
| | `POST /api/v1/appointments`: luồng cũ, Facade tạo `LegacyQuote`/`LegacyStandard` (Facade dòng 41) | 26–35 |
| [strategy/controller/PatientBenefitController.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/controller/PatientBenefitController.java) 🛂 | `POST/GET /api/v1/patients/me/benefit-requests`, `GET .../{id}` | 27–52 |
| [strategy/controller/AdminBenefitController.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/controller/AdminBenefitController.java) 🛂 | `GET /api/v1/admin/benefit-requests`, `GET .../{id}`, `POST .../{id}/decision`, `POST .../{id}/revoke` | 26–56 |

---

## 5. Điều kiện sử dụng (quyền hưởng) 🛂

| File | Nội dung chính | Dòng |
| --- | --- | --- |
| [strategy/service/BenefitService.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/service/BenefitService.java) | `submit`: bệnh nhân gửi yêu cầu, kiểm tra gia hạn | 48–71 |
| | `decide`: Admin duyệt/từ chối, khóa dòng, ghi lịch sử | 98–118 |
| | `revoke`: thu hồi, không xóa lịch sử | 122–132 |
| | `covering`: quyền hưởng còn hiệu lực hôm nay và ngày khám (loại phí, báo giá) | 141–145 |
| | `lockCovering`: cùng quy tắc, khóa dòng quyền (đặt lịch) | 149–159 |
| | `covers` / `inWindow`: một quy tắc ngày duy nhất | 165–172 |
| | `effectiveStatus`: suy ra `EXPIRED` / `NOT_YET_VALID` | 175–190 |
| [strategy/entity/BenefitRequest.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/entity/BenefitRequest.java) | Bảng `benefit_requests`: loại, mã hồ sơ, thời hạn, trạng thái, `@Version` | 32–78 |
| [strategy/entity/BenefitDecision.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/entity/BenefitDecision.java) | Lịch sử quyết định: hành động, lý do, người duyệt, thời điểm | 23–52 |
| [strategy/repository/BenefitRequestRepository.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/repository/BenefitRequestRepository.java) | `findForUpdate` (khóa bi quan, 18), `findApprovedIds` (23) | 13–32 |
| [strategy/repository/BenefitDecisionRepository.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/repository/BenefitDecisionRepository.java) | Đọc lịch sử theo yêu cầu | 8–12 |

---

## 6. DTO và dữ liệu lịch hẹn

| File | Nội dung |
| --- | --- |
| [strategy/dto/request/ConsultationQuoteRequest.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/request/ConsultationQuoteRequest.java) | `scheduleId`, `strategyKey` |
| [strategy/dto/response/ConsultationQuoteDto.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/response/ConsultationQuoteDto.java) | Kết quả báo giá; `discountRateExact()` trả chuỗi chính xác (dòng 16) |
| [strategy/dto/request/SubmitBenefitRequest.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/request/SubmitBenefitRequest.java) | Bệnh nhân gửi: loại, mã hồ sơ, thời hạn, `renewalOfId` |
| [strategy/dto/request/BenefitDecisionInput.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/request/BenefitDecisionInput.java) | Admin duyệt: hành động, lý do, khoảng hiệu lực |
| [strategy/dto/request/BenefitRevokeInput.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/request/BenefitRevokeInput.java) | Lý do thu hồi |
| [strategy/dto/response/BenefitRequestDto.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/response/BenefitRequestDto.java), [BenefitDecisionDto.java](../../backend/src/main/java/com/datlichkhambenh/backend/strategy/dto/response/BenefitDecisionDto.java) | Dữ liệu trả về cho UI |
| 🔗 [dto/request/CreateBookingDto.java](../../backend/src/main/java/com/datlichkhambenh/backend/dto/request/CreateBookingDto.java) | Thêm `strategyKey`, `expectedBasePrice`, `expectedDiscountRate`, `expectedPrice`, `expectedWorkDate/StartTime/EndTime` (dòng 14–19); không còn cờ `quoteRequired` |
| 🔗 [entity/Appointment.java](../../backend/src/main/java/com/datlichkhambenh/backend/entity/Appointment.java) | Snapshot: `pricingKey`, `basePrice`, `discountRate` DECIMAL(19,18), `benefitRequestId` (dòng 61–65) |
| 🔗 [dto/response/AppointmentDto.java](../../backend/src/main/java/com/datlichkhambenh/backend/dto/response/AppointmentDto.java) | Trả snapshot cho UI (dòng 31–34) |

### Khóa dùng trong luồng đặt lịch 🔗

| File | Hàm | Dòng |
| --- | --- | --- |
| [repository/ScheduleRepository.java](../../backend/src/main/java/com/datlichkhambenh/backend/repository/ScheduleRepository.java) | `findByIdForUpdate`: `PESSIMISTIC_WRITE` trên ca khám | 30–32 |
| [repository/DoctorRepository.java](../../backend/src/main/java/com/datlichkhambenh/backend/repository/DoctorRepository.java) | `findByIdForUpdate`: giữ phí bác sĩ ổn định đến commit | 23–25 |

---

## 7. Cấu hình

[backend/src/main/resources/application.yml](../../backend/src/main/resources/application.yml), dòng 99–103:

```yaml
app:
  pricing:
    vip-discount-rate: ${VIP_DISCOUNT_RATE:0.1}
    insurance-discount-rate: ${INSURANCE_DISCOUNT_RATE:0.8}
    standard-discount-rate: ${STANDARD_DISCOUNT_RATE:0}
```

---

## 8. SQL nâng cấp — `backend/src/main/resources/db/upgrade/strategy/`

Chạy theo thứ tự trên database đang có, sau khi sao lưu.

| File | Thay đổi |
| --- | --- |
| [001-benefit-requests.sql](../../backend/src/main/resources/db/upgrade/strategy/001-benefit-requests.sql) | Tạo bảng `benefit_requests` |
| [002-benefit-decisions.sql](../../backend/src/main/resources/db/upgrade/strategy/002-benefit-decisions.sql) | Thêm thời hạn duyệt + bảng lịch sử quyết định |
| [003-benefit-renewal.sql](../../backend/src/main/resources/db/upgrade/strategy/003-benefit-renewal.sql) | Thêm `renewal_of_id` cho gia hạn |
| [004-appointment-pricing-snapshot.sql](../../backend/src/main/resources/db/upgrade/strategy/004-appointment-pricing-snapshot.sql) | Snapshot giá trên `appointments`; không backfill lịch cũ |
| [005-appointment-benefit-reference.sql](../../backend/src/main/resources/db/upgrade/strategy/005-appointment-benefit-reference.sql) | Liên kết lịch hẹn với quyền hưởng đã dùng |

---

## 9. Frontend

Bản gốc ở `frontend/`; bản giống hệt được Spring Boot phục vụ ở `backend/src/main/resources/static/`.

| File | Nội dung |
| --- | --- |
| [frontend/strategy/consultation-quote.js](../../frontend/strategy/consultation-quote.js) | Trạng thái báo giá: `create`, `reset`, `select`; bỏ phản hồi trễ theo phiên bản request |
| [frontend/strategy/benefits.js](../../frontend/strategy/benefits.js) | Trang quyền hưởng của bệnh nhân |
| [frontend/strategy/admin-benefits.js](../../frontend/strategy/admin-benefits.js) | Trang duyệt/thu hồi của Admin |
| [frontend/patient/dashboard.html](../../frontend/patient/dashboard.html) 🔗 | Modal đặt lịch: chọn loại phí, xem báo giá, xác nhận lại giá |
| [frontend/patient/benefits.html](../../frontend/patient/benefits.html) 🔗 | Trang gửi/xem quyền hưởng |
| [frontend/admin/benefits.html](../../frontend/admin/benefits.html) 🔗 | Trang Admin quản lý quyền hưởng |

---

## 10. Kiểm thử 🧪

### Java — `backend/src/test/java/com/datlichkhambenh/backend/strategy/`

| File | Kiểm tra gì |
| --- | --- |
| [pricing/PricingContextTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/pricing/PricingContextTests.java) | Thêm Strategy mới không sửa Context; HALF_UP; tỷ lệ null/0/1; trùng key; phí vượt giới hạn |
| [QuotePrecisionTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/QuotePrecisionTests.java) | Tỷ lệ 18 chữ số qua JavaScript không mất chính xác |
| [ConsultationQuoteIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/ConsultationQuoteIntegrationTests.java) | API báo giá: phân quyền, ca không hợp lệ, không giữ chỗ |
| [EligibilityQuoteIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/EligibilityQuoteIntegrationTests.java) | Bảo hiểm/VIP chỉ khi đủ điều kiện; ngày biên; giờ Việt Nam |
| [BenefitRequestIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/BenefitRequestIntegrationTests.java) | Bệnh nhân gửi/xem yêu cầu; ownership |
| [BenefitDecisionIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/BenefitDecisionIntegrationTests.java) | Admin duyệt/từ chối/thu hồi; quyết định đồng thời |
| [BookingPricingContract.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/BookingPricingContract.java) | Bộ hợp đồng đặt lịch: snapshot, `PRICE_CHANGED`, tranh ca, thu hồi đang khóa, email sau commit |
| [BookingPricingIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/BookingPricingIntegrationTests.java) | Chạy hợp đồng trên H2 |
| [MySqlBookingPricingIntegrationTests.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/MySqlBookingPricingIntegrationTests.java) | Chạy hợp đồng trên MySQL thật (cần `CK_MYSQL_URL`) |
| [ConsultationQuoteBrowserServer.java](../../backend/src/test/java/com/datlichkhambenh/backend/strategy/ConsultationQuoteBrowserServer.java) | Server demo/fixture H2 cho trình duyệt |

### Frontend — `frontend/tests/strategy/`

| File | Kiểm tra gì |
| --- | --- |
| [consultation-quote.test.cjs](../../frontend/tests/strategy/consultation-quote.test.cjs) | Test Node: phản hồi trễ bị bỏ |
| `browser-quote`, `browser-insurance-quote`, `browser-vip-quote` | Báo giá ba loại |
| `browser-standard-booking`, `browser-discount-booking`, `browser-precision-booking` | Đặt lịch và snapshot |
| `browser-benefits`, `browser-admin-benefits`, `browser-benefit-lifecycle` | Quy trình quyền hưởng |
| `browser-price-reconfirmation`, `browser-booking-recovery` | Giá đổi, mất quyền |
| `browser-deleted-slot`, `browser-edited-slot`, `browser-quote-slot-change` | Ca bị xóa/đổi giờ |
| `browser-vietnam-date` | Múi giờ Việt Nam |
| `browser-full-strategy-flow` | Toàn luồng từ gửi quyền đến đọc snapshot |

---

## 11. Công cụ và sơ đồ

| File | Công dụng |
| --- | --- |
| [scripts/strategy/run-browser-fixture.sh](../../scripts/strategy/run-browser-fixture.sh) | Chạy server demo H2 ở cổng 18081 |
| [scripts/strategy/test-browser.py](../../scripts/strategy/test-browser.py) | Chạy 17 kịch bản Chrome |
| [scripts/strategy/test-mysql-upgrades.py](../../scripts/strategy/test-mysql-upgrades.py) | Kiểm tra 5 SQL nâng cấp trên MySQL |
| [scripts/strategy/generate-quote-diagrams.py](../../scripts/strategy/generate-quote-diagrams.py), [generate-booking-diagrams.py](../../scripts/strategy/generate-booking-diagrams.py) | Sinh file draw.io |
| [scripts/strategy/export-diagrams.sh](../../scripts/strategy/export-diagrams.sh) | Xuất PNG bằng draw.io cục bộ |
| [docs/strategy/diagrams/](diagrams/) | 4 file `.drawio` + 4 PNG: lớp/tuần tự cho báo giá và đặt lịch |

---

## 12. Tài liệu liên quan

- [Tài liệu chi tiết tính năng](tai-lieu-strategy.md)
- [Kịch bản thuyết trình](kich-ban-thuyet-trinh.md)
- [Báo cáo, demo và bằng chứng](report.md) · [Nhật ký thực hiện](execution.md)
