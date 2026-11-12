# Tài liệu tính năng: Factory Method tạo tài khoản theo vai trò

Hệ thống đặt lịch khám bệnh (repo CK_MTKPM). Tài liệu này mô tả việc áp dụng mẫu thiết kế **Factory Method** để gom việc mở tài khoản (bệnh nhân, bác sĩ) về một quy trình chung, thay cho đoạn code lặp ở ba chỗ.

Mỗi quyết định trong tài liệu được ghi theo thứ tự: **trước đó xảy ra vấn đề gì → các phương án → chọn gì → vì sao (ví dụ ngoài đời) → cái giá phải trả**.

---

## 1. Mẫu Factory Method là gì

### 1.1 Định nghĩa

Factory Method là một mẫu **khởi tạo (creational)** trong 23 mẫu của nhóm GoF (Gamma, Helm, Johnson, Vlissides, *Design Patterns*, 1994):

> Định nghĩa một giao diện để tạo đối tượng, nhưng để lớp con quyết định tạo lớp nào. Factory Method cho phép lớp cha hoãn việc khởi tạo sang lớp con.

Nói đơn giản: **quy trình chung nằm ở lớp cha; đúng một bước "tạo ra cái gì" để trống cho lớp con điền.**

### 1.2 Bốn thành phần

| Thành phần | Vai trò chung |
|---|---|
| **Creator** | Lớp cha chứa quy trình chung và khai báo factory method |
| **ConcreteCreator** | Lớp con cài factory method, tạo sản phẩm cụ thể |
| **Product** | Giao diện chung của sản phẩm |
| **ConcreteProduct** | Sản phẩm cụ thể |

### 1.3 Factory Method ngoài đời và trong phần mềm

| Ví dụ | Quy trình chung | Bước khác nhau |
|---|---|---|
| Quầy mở thẻ bệnh viện | Lấy giấy tờ, chụp ảnh, cấp mã | Thẻ bệnh nhân ghi tiền sử; thẻ bác sĩ ghi chuyên khoa |
| Mở tài khoản ngân hàng | Xác minh danh tính, cấp số tài khoản | Tài khoản thanh toán, tiết kiệm, doanh nghiệp |
| Công ty logistics | Nhận đơn, tính phí, lên lịch | Giao bằng xe tải hay tàu biển |
| Java chuẩn | `Collection.iterator()` | Mỗi loại collection trả iterator riêng |
| Java chuẩn | `Calendar.getInstance()` | Lịch Gregorian, Phật lịch, Nhật… theo locale |

Điểm chung: **phần giống nhau viết một lần; thêm loại mới chỉ thêm một lớp con.**

### 1.4 Khi nào nên và không nên dùng

Nên dùng khi:
- Có quy trình tạo chung với một bước khác nhau theo loại.
- Số loại có thể tăng (vai trò mới).
- Muốn bảo đảm bước chung (ví dụ mã hóa mật khẩu) không ai bỏ qua được.

Không nên dùng khi:
- Chỉ có một loại sản phẩm.
- Các loại khác nhau gần như toàn bộ (không có quy trình chung để gom).

---

## 2. Bài toán thực tế và vì sao chọn Factory Method

### 2.1 Hiện trạng trước khi làm

Tạo tài khoản lặp ở **ba chỗ**:

- `AuthServiceImpl.register`: một khối `if PATIENT … else if DOCTOR …`, mỗi nhánh tự dựng `User`, mã hóa mật khẩu, gán vai trò rồi tạo hồ sơ;
- `AdminManagementServiceImpl.createDoctor` và `createPatient`: lặp lại việc dựng `User`, mã hóa mật khẩu, gán vai trò.

Hậu quả:
- Thêm vai trò mới (ví dụ y tá) phải sửa cả ba chỗ.
- Chỉ một chỗ quên mã hóa mật khẩu hay gán sai vai trò là sinh **lỗ hổng bảo mật**.
- Chặn đăng ký Admin chỉ dựa vào lớp kiểm tra đầu vào; vượt qua được là tạo được Admin.

### 2.2 Ví dụ ngoài đời: quầy mở thẻ bệnh viện

Mọi người đến quầy đều làm cùng thủ tục: đưa giấy tờ, chụp ảnh, được cấp mã. **Chỉ bước cuối khác nhau**: bệnh nhân điền phiếu tiền sử bệnh, bác sĩ điền chuyên khoa và bằng cấp. Quầy không có mẫu thẻ "Giám đốc", nên dù ai đó điền tay "Giám đốc" vào đơn, quầy cũng không làm được thẻ đó.

| Ngoài đời | Trong hệ thống |
|---|---|
| Thủ tục chung ở quầy | `AccountFactory.create()` (final): lưu `User`, mã hóa mật khẩu, gán vai trò |
| Bước cuối khác nhau | `createProfile()` (factory method) |
| Nhân viên làm thẻ bệnh nhân | `PatientAccountFactory` |
| Nhân viên làm thẻ bác sĩ | `DoctorAccountFactory` |
| Danh sách loại thẻ quầy làm được | `AccountFactories.forRole(role)` |
| Không có mẫu thẻ "Giám đốc" | Không có factory ADMIN |
| Tấm thẻ | `ClinicAccount` (`PatientAccount`, `DoctorAccount`) |

### 2.3 So sánh phương án

| Phương án | Ngoài đời giống như | Đánh giá |
|---|---|---|
| Hàm tiện ích `createUser(...)` | Một tờ hướng dẫn chung, mỗi quầy tự làm tiếp | **Loại**: gom được bước chung nhưng nhánh `if` theo vai trò vẫn nằm ở nơi gọi |
| Simple Factory (một lớp có `switch(role)`) | Một nhân viên làm mọi loại thẻ | **Loại**: thêm vai trò vẫn phải sửa `switch` |
| Abstract Factory | Cả dây chuyền làm bộ sản phẩm | **Loại**: quá nặng; mỗi vai trò chỉ tạo một sản phẩm |
| **Factory Method + danh bạ theo vai trò** | **Thủ tục chung + nhân viên chuyên từng loại thẻ** | **Chọn**: bước chung ở Creator, bước riêng ở lớp con; thêm vai trò = thêm lớp, Spring tự đưa vào danh bạ |

### 2.4 Vì sao Factory Method hợp với tính năng này

1. Có **quy trình chung rõ ràng** (lưu login, mã hóa, gán vai trò) và **một bước khác** (hồ sơ).
2. Bước chung liên quan **bảo mật**: cần khóa lại để lớp con không bỏ qua được (`final`).
3. Vai trò **có thể tăng** (y tá, kỹ thuật viên).
4. Có **ba nơi gọi** (tự đăng ký, Admin tạo bác sĩ, Admin tạo bệnh nhân) cần cùng một quy trình.

---

## 3. Thiết kế

### 3.1 Vai trò trong code

| Vai trò | Lớp | Trách nhiệm |
|---|---|---|
| Creator | `factory/AccountFactory` | `role()` (abstract), `create()` (final), `createProfile()` (factory method) |
| ConcreteCreator | `factory/PatientAccountFactory` | Hồ sơ bệnh nhân: tên, số điện thoại, ngày sinh, giới tính, địa chỉ, tiền sử |
| ConcreteCreator | `factory/DoctorAccountFactory` | Hồ sơ bác sĩ: bắt buộc chuyên khoa, bằng cấp, kinh nghiệm, phí khám, `approved` |
| Product | `factory/ClinicAccount` | sealed interface; hai record `PatientAccount`, `DoctorAccount` |
| Đầu vào | `factory/AccountRequest` | record dùng chung cho tự đăng ký và Admin tạo |
| Danh bạ | `factory/AccountFactories` | `forRole(role)`; tạo từ mọi `AccountFactory` Spring biết; không có ADMIN |
| Client | `service/impl/AuthServiceImpl`, `service/impl/AdminManagementServiceImpl` | Chỉ tìm factory theo vai trò rồi gọi `create()` |

### 3.2 Bốn cách mở tài khoản, một quy trình

| Cách | API | Factory | `approved` |
|---|---|---|---|
| Bệnh nhân tự đăng ký | `POST /api/v1/auth/register` (role PATIENT) | `PatientAccountFactory` | — |
| Bác sĩ tự đăng ký | `POST /api/v1/auth/register` (role DOCTOR) | `DoctorAccountFactory` | `false` (chờ Admin duyệt) |
| Admin tạo bác sĩ | `POST /api/v1/admin/doctors` | `DoctorAccountFactory` | `true` mặc định (Admin vẫn chọn được chờ duyệt) |
| Admin tạo bệnh nhân | `POST /api/v1/admin/patients` | `PatientAccountFactory` | — |
| Admin duyệt bác sĩ tự đăng ký | `PUT /api/v1/admin/doctors/{id}/approve` | (không tạo tài khoản) | chuyển `true` |

### 3.3 Quy trình `create()`

1. Lưu `User` với email đã chuẩn hóa, `passwordEncoder.encode(rawPassword)`, `role()` của factory, `isActive = true`.
2. Gọi `createProfile(user, request)` của lớp con.
3. Trả `ClinicAccount` (login + hồ sơ).

Cả hai bước nằm trong giao dịch của service gọi; lỗi ở bước 2 rollback cả bước 1.

### 3.4 UML

Năm sơ đồ draw.io chỉnh sửa được, kèm PNG đen trắng, trong `docs/factory/diagrams/`:

| Sơ đồ | File | Nội dung |
|---|---|---|
| Lớp | `factory-class` | Creator, 2 ConcreteCreator, Product sealed, danh bạ, Client |
| Tuần tự | `factory-sequence` | Đăng ký → danh bạ → `create()` → lưu user → `createProfile()` |
| ERD | `factory-erd` | `users` 1–1 `patients` / `doctors`, `doctors` n–1 `specialties` |
| DFD | `factory-dfd` | Yêu cầu mở tài khoản → chọn factory → lưu login + hồ sơ |
| Use Case | `factory-use-case` | 4 cách mở tài khoản «include» mở theo vai trò; «extend» từ chối vai trò không có factory; Admin duyệt bác sĩ |

Sinh lại bằng `python3 scripts/factory/generate-diagrams.py`; xuất PNG bằng `bash scripts/common/export-diagrams.sh docs/factory/diagrams`.

---

## 4. Các quyết định thiết kế (vấn đề → giải pháp → lý do)

### QĐ1. Không có factory cho ADMIN

- **Vấn đề**: Trước đây chặn đăng ký Admin chỉ dựa vào kiểm tra đầu vào. Gọi thẳng service (hoặc lớp kiểm tra bị sửa nhầm) là tạo được Admin.
- **Ngoài đời**: Quầy không có mẫu thẻ "Giám đốc", nên không ai làm được thẻ đó tại quầy.
- **Giải pháp**: Vai trò chỉ hợp lệ khi có factory. `forRole("ADMIN")` trả rỗng → cùng thông báo lỗi như trước, không tạo user.
- **Bằng chứng**: `registeringAsAdminIsRejectedAtTheApi`, `serviceRejectsRolesWithoutAFactoryWithTheSameMessage` (ADMIN, NURSE, `admin` viết thường).
- **Cái giá**: tạo Admin phải làm ngoài luồng đăng ký (seed dữ liệu).

### QĐ2. `create()` là `final`

- **Vấn đề**: Lớp con có thể ghi đè và quên mã hóa mật khẩu hay gán sai vai trò.
- **Ngoài đời**: Nhân viên chuyên từng loại thẻ không được bỏ bước chụp ảnh và xác minh.
- **Giải pháp**: Bước chung khóa ở Creator; lớp con chỉ cài `role()` và `createProfile()`.

### QĐ3. Duyệt bác sĩ là tham số, không phải lớp riêng

- **Vấn đề**: Bác sĩ tự đăng ký phải chờ duyệt; bác sĩ Admin tạo thì đã duyệt. Tách hai lớp factory là trùng code.
- **Giải pháp**: Trường `approved` trong `AccountRequest`.
- **Bằng chứng**: `doctorRegistrationWaitsForAdminApproval`, `adminCreatesAnApprovedDoctorWhoCanLogIn`, `adminMayCreateADoctorStillWaitingForApproval`.

### QĐ4. Kiểm tra email/số điện thoại trùng giữ ở service

- **Lý do**: Đăng ký và Admin có thông báo lỗi khác nhau; factory chỉ lo dựng tài khoản, không lo thông điệp.

### QĐ5. Mọi bước trong một giao dịch

- **Vấn đề**: Lưu login xong mà tạo hồ sơ lỗi (chuyên khoa không tồn tại) sẽ để lại login mồ côi.
- **Giải pháp**: Giao dịch của service bao cả `create()`; lỗi ở `createProfile` rollback cả login.
- **Bằng chứng**: `doctorRegistrationWithoutSpecialtyIsRejectedAsBefore`.

### QĐ6. Product là sealed interface

- **Lý do**: Chỉ có đúng hai loại tài khoản; trình biên dịch biết hết các trường hợp, không ai thêm loại thứ ba ngoài gói `factory`.

### QĐ7. Danh bạ tự dựng từ Spring

- **Vấn đề**: Thêm vai trò mà quên đăng ký vào danh bạ thì vai trò không dùng được.
- **Giải pháp**: `AccountFactories` nhận `List<AccountFactory>` từ Spring; thêm lớp `@Component` là tự có trong danh bạ. Hai factory trùng vai trò → lỗi khi khởi động (`toUnmodifiableMap`).

---

## 5. Dữ liệu thô: vấn đề và cách xử lý

Đây không phải dự án học máy nên không có dataset huấn luyện. Tuy vậy, **dữ liệu đầu vào thật** của việc mở tài khoản có nhiều vấn đề khi chưa xử lý:

| # | Dữ liệu thô | Hậu quả nếu để nguyên | Giải pháp | Bằng chứng |
|---|---|---|---|---|
| 1 | Vai trò `ADMIN` gửi từ client | Ai cũng tự cấp quyền Admin | Không có factory ADMIN | `registeringAsAdminIsRejectedAtTheApi` |
| 2 | Vai trò lạ (`NURSE`) hoặc sai hoa thường (`admin`) | Tạo user vai trò không xác định | Danh bạ không có → từ chối, không tạo user | `serviceRejectsRolesWithoutAFactoryWithTheSameMessage` |
| 3 | Vai trò `null` | `NullPointerException` | `forRole(null)` trả rỗng | Mã nguồn `AccountFactories` |
| 4 | Bác sĩ thiếu chuyên khoa | Bác sĩ không thuộc khoa nào | Lỗi "Bác sĩ phải đăng ký thuộc 1 chuyên khoa", rollback login | `doctorRegistrationWithoutSpecialtyIsRejectedAsBefore` |
| 5 | Chuyên khoa không tồn tại | Login mồ côi không có hồ sơ | `ResourceNotFoundException`, rollback | Thiết kế giao dịch (QĐ5) |
| 6 | Mật khẩu thô | Lộ mật khẩu nếu lộ DB | Mã hóa trong `create()` (final) | `patientRegistrationCreatesALoginAndAPatientProfile` (đăng nhập được bằng mật khẩu gốc) |
| 7 | Tên có khoảng trắng thừa (Admin nhập) | Dữ liệu bẩn | Cắt khoảng trắng ở service trước khi tạo `AccountRequest` | `adminCreatesAPatientWhoCanLogIn` |
| 8 | Email viết hoa / có khoảng trắng | Hai tài khoản cho cùng một email | Chuẩn hóa ở service (bỏ khoảng trắng, chữ thường) | `AccountRequest` (tài liệu tham số `email`) |

---

## 6. Kiểm thử và đánh giá

### 6.1 Kết quả

| Lớp test | Số test | Kiểm tra gì |
|---|---:|---|
| `AccountFactoryIntegrationTests` | 8 (một test tham số hóa ADMIN / NURSE / admin) | Qua API: bệnh nhân đăng ký → đăng nhập được, có hồ sơ; bác sĩ đăng ký → vào danh sách chờ duyệt; thiếu chuyên khoa → cùng lỗi cũ; Admin tạo bác sĩ (đã duyệt / chờ duyệt) và bệnh nhân → đăng nhập được; ADMIN qua API → 400, không tạo user; gọi thẳng service với vai trò không có factory → cùng lỗi, không tạo user |
| `AuthServiceImplTests` (test cũ, đã sửa có lý do) | — | Chỉ đổi lời gọi constructor để truyền danh bạ factory |

Toàn bộ backend: `bash scripts/maven.sh -f backend/pom.xml clean test` → 170 chạy, 0 lỗi, 17 bỏ qua (nhóm MySQL cần `CK_MYSQL_URL`).

### 6.2 Đánh giá theo các tiêu chí đề yêu cầu

Các tiêu chí độ tin cậy, hiệu chuẩn, bootstrap và công bằng là công cụ đánh giá **mô hình xác suất**. Factory Method là **quy trình tất định**: cùng yêu cầu luôn cho cùng loại tài khoản. Tài liệu không đưa ra con số giả mà trình bày cách đánh giá tương đương:

| Tiêu chí | Có áp dụng trực tiếp? | Cách đánh giá tương đương |
|---|---|---|
| **Độ tin cậy** | Không | Test đầu-cuối: tạo tài khoản rồi **đăng nhập thật** bằng mật khẩu gốc, kiểm tra vai trò trong token |
| **Hiệu chuẩn** | Không | "Vai trò yêu cầu" phải khớp "vai trò được tạo"; vai trò không có factory → không có user nào trong DB |
| **Bootstrap** | Không | Kiểm thử bảng các vai trò không hợp lệ (ADMIN, NURSE, viết thường) và cả hai đường vào (API và gọi thẳng service) |
| **Công bằng giới** | Factory lưu giới tính bệnh nhân như dữ liệu hồ sơ nhưng **không dùng** để quyết định gì | Mọi người cùng vai trò đi qua cùng quy trình; bác sĩ tự đăng ký đều chờ duyệt như nhau. Rủi ro còn lại là khâu Admin duyệt thủ công |

---

## 7. Hiệu quả

| Nguyên tắc | Biểu hiện cụ thể |
|---|---|
| **OCP** | Thêm vai trò y tá = thêm `NurseAccountFactory` (`@Component`); không sửa service hay danh bạ |
| **SRP** | Creator lo quy trình chung; mỗi ConcreteCreator lo một loại hồ sơ; service lo kiểm tra trùng và thông điệp |
| **DIP** | Service phụ thuộc `AccountFactories` / `AccountFactory`, không phụ thuộc lớp cụ thể |
| Bảo mật | Mã hóa mật khẩu và gán vai trò không thể bị bỏ qua (`final`); Admin không thể tự đăng ký |
| Bớt lặp | Ba đoạn dựng `User` gom về một chỗ; `register()` chỉ còn một dòng tạo tài khoản |

---

## 8. Nhược điểm và giới hạn

| Nhược điểm | Biểu hiện trong dự án |
|---|---|
| Nhiều lớp hơn | Creator, 2 ConcreteCreator, Product, đầu vào, danh bạ: 6 lớp cho 2 loại tài khoản |
| Đầu vào gom chung | `AccountRequest` chứa cả trường bệnh nhân lẫn bác sĩ; factory nào bỏ qua trường không dùng |
| Phải ép kiểu | Client nhận `ClinicAccount` rồi ép về `DoctorAccount`/`PatientAccount` khi cần hồ sơ cụ thể |
| Còn sót | `AuthServiceImpl` vẫn giữ vài phụ thuộc không còn dùng (để không phải sửa test cũ thêm lần nữa) |
| Ngoài phạm vi | Tạo Admin chỉ qua seed dữ liệu |

---

## 9. Hướng phát triển

- Thêm vai trò y tá / kỹ thuật viên bằng factory mới.
- Tách `AccountRequest` thành đầu vào riêng cho từng vai trò nếu số trường tăng nhiều.
- Dọn các phụ thuộc thừa trong `AuthServiceImpl` cùng lúc cập nhật test cũ.
- Thêm luồng mời Admin bằng email thay vì seed dữ liệu.

## 10. Vị trí mã nguồn

| Phần | Đường dẫn |
|---|---|
| Creator, ConcreteCreator, Product, danh bạ | `backend/src/main/java/com/datlichkhambenh/backend/factory/` |
| Client | `backend/src/main/java/com/datlichkhambenh/backend/service/impl/AuthServiceImpl.java`, `AdminManagementServiceImpl.java` |
| Test | `backend/src/test/java/com/datlichkhambenh/backend/factory/` |
| Sơ đồ | `docs/factory/diagrams/` |
| Script sinh sơ đồ | `scripts/factory/generate-diagrams.py` |
