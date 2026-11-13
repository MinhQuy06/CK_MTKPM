# Kịch bản thuyết trình: Adapter và Decorator

Phần mẫu cấu trúc, slide 16–24 trong bộ slide chung. Thời lượng nói khoảng **7 phút**, thêm **2 phút** demo nếu được phép.

[README Adapter](adapter/README.md) · [Tài liệu Adapter](adapter/tai-lieu-adapter.md) · [README Decorator](decorator/README.md) · [Tài liệu Decorator](decorator/tai-lieu-decorator.md)

---

## Tóm tắt một dòng (thuộc lòng)

> **Decorator thêm hành vi vào việc gửi thông báo mà không sửa lớp gốc; Adapter cắm nhà cung cấp SMS bên ngoài vào mà không để định dạng của họ lan vào nghiệp vụ. Hai mẫu nối nhau: lớp bọc SMS của Decorator chính là Client của Adapter.**

## Thời gian

| Slide | Nội dung | Thời lượng | Cộng dồn |
|---|---|---:|---:|
| 16 | Mở nhóm mẫu cấu trúc | 0:15 | 0:15 |
| 17 | Adapter: vấn đề và ý định | 0:45 | 1:00 |
| 18 | Adapter: cấu trúc GoF | 0:40 | 1:40 |
| 19 | Adapter: kênh SMS của nhà cung cấp | 1:00 | 2:40 |
| 20 | Adapter: luồng gửi và đánh giá | 0:50 | 3:30 |
| 21 | Decorator: vấn đề và ý định | 0:45 | 4:15 |
| 22 | Decorator: cấu trúc GoF | 0:40 | 4:55 |
| 23 | Decorator: bọc dịch vụ thông báo | 1:00 | 5:55 |
| 24 | Decorator: chuỗi bọc và đánh giá | 1:00 | 6:55 |
| (demo) | Chạy test hoặc đặt lịch thật | 2:00 | 8:55 |

Nếu bị ép thời gian: bỏ demo, rút slide 18 và 22 còn một câu mỗi slide ("Target – Adaptee – Adapter – Client" / "Component – Decorator – ConcreteDecorator"), tổng còn khoảng 5 phút 30.

---

## Slide 16 · Mở nhóm mẫu cấu trúc (≈ 15 giây)

**Nói:**

> "Em xin trình bày nhóm thứ hai, các mẫu cấu trúc. Nhóm này lo việc ghép các lớp và đối tượng thành cấu trúc lớn hơn mà từng phần vẫn đơn giản. Em sẽ trình bày hai mẫu đi liền nhau trong luồng thông báo: Adapter và Decorator."

**Chuyển ý:** "Em bắt đầu với Adapter, vì đó là chỗ hệ thống chạm ra thế giới bên ngoài."

---

## Slide 17 · Adapter: vấn đề và ý định (≈ 45 giây)

**Nói:**

> "Adapter chuyển giao diện của một lớp thành giao diện khác mà client mong đợi, để các lớp vốn không tương thích làm việc được với nhau.
>
> Ví dụ quen thuộc là củ sạc du lịch: phích cắm laptop không vừa ổ điện Việt Nam. Ta không sửa laptop, cũng không đục lại ổ điện, mà gắn một củ chuyển đổi ở giữa.
>
> Trong phần mềm, tình huống điển hình là tích hợp dịch vụ của bên thứ ba. Ta không sửa được mã của họ, và cũng không muốn định dạng của họ lan vào nghiệp vụ của mình."

**Chỉ vào:** hình củ sạc / câu định nghĩa.

---

## Slide 18 · Adapter: cấu trúc và vai trò (≈ 40 giây)

**Nói:**

> "Mẫu có bốn vai. **Target** là giao diện mà client mong đợi. **Adaptee** là lớp có sẵn nhưng giao diện khác. **Adapter** hiện thực Target, bên trong gọi Adaptee và dịch dữ liệu qua lại. **Client** chỉ biết Target.
>
> GoF có hai kiểu: class adapter kế thừa Adaptee, và object adapter giữ Adaptee bên trong. Nhóm em chọn object adapter, vì Java chỉ cho kế thừa đơn, và giữ Adaptee bên trong thì thay nhà cung cấp, hay thay bằng bản giả khi test, chỉ là đổi một bean."

**Chỉ vào:** sơ đồ lớp GoF, mũi tên từ Adapter tới Adaptee là quan hệ "giữ", không phải kế thừa.

---

## Slide 19 · Adapter: kênh SMS của nhà cung cấp (≈ 1 phút)

**Nói:**

> "Bài toán thật của nhóm: phòng khám muốn gửi thêm SMS khi đặt, hủy và nhắc lịch. Nhà cung cấp là eSMS, và API của họ không khớp với hệ thống ở ba chỗ.
>
> Một, **số điện thoại**: phòng khám lưu kiểu `0912 345 678`, còn eSMS cần số có mã quốc gia 84.
> Hai, **dữ liệu**: hệ thống làm việc với bản tin thông báo 9 trường, còn eSMS chỉ nhận một chuỗi văn bản.
> Ba, **kết quả**: eSMS trả mã kết quả, `100` là đã nhận, chứ không ném ngoại lệ.
>
> Ánh xạ vào mẫu: Target là `NotificationChannel`, giao diện của phòng khám, chỉ có hàm `send(số điện thoại, bản tin)`. Adaptee là `SmsGatewayClient`, giao diện của nhà cung cấp, có hai bản: `EsmsGatewayClient` gọi REST thật, và `DisabledSmsGatewayClient` dùng khi SMS đang tắt. Adapter là `SmsChannelAdapter`. Client là lớp bọc SMS của Decorator, em sẽ nói ở phần sau.
>
> Bản nào được dùng do `SmsGatewayConfig` chọn theo cờ `SMS_ENABLED`. Mặc định cờ này tắt, vì SMS thật tốn tiền theo từng tin."

**Chỉ vào:** sơ đồ lớp Adapter (`docs/adapter/diagrams/adapter-class.png`), lần lượt từ Target → Adapter → Adaptee.

---

## Slide 20 · Adapter: luồng gửi SMS và đánh giá (≈ 50 giây)

**Nói:**

> "Luồng trong Adapter có ba nhánh. Bệnh nhân **không có số** thì trả `SKIPPED_NO_PHONE` và không gọi nhà cung cấp, vì số điện thoại không bắt buộc, coi là lỗi sẽ làm lịch sử đầy lỗi giả. **Số sai định dạng** thì trả `FAILED` và cũng không gọi, đỡ tốn một lượt gửi. **Số hợp lệ** thì đổi sang `+84`, soạn một tin ngắn không dấu, gọi `submitSms`, rồi dịch mã trả về thành `DeliveryResult`: đã gửi, bị từ chối, hoặc bỏ qua vì kênh đang tắt.
>
> Ưu điểm: nghiệp vụ không thấy định dạng nào của nhà cung cấp. Muốn đổi sang nhà cung cấp khác, chỉ cần viết một lớp mới hiện thực `SmsGatewayClient`; Adapter, Decorator và luồng đặt lịch không đổi một dòng.
> Nhược điểm: thêm một tầng gián tiếp, và nhóm chưa thử với tài khoản eSMS thật, mới kiểm thử bằng máy chủ HTTP giả lập đúng định dạng eSMS.
>
> Mẫu này thể hiện **DIP**, vì client phụ thuộc giao diện chứ không phụ thuộc eSMS; **OCP**, vì thêm nhà cung cấp không sửa code cũ; và **LSP**, vì bản thật, bản tắt và bản giả thay cho nhau được."

**Chỉ vào:** sơ đồ tuần tự Adapter, hai khung `alt`.

**Chuyển ý:** "Adapter trả kết quả SMS, nhưng ai quyết định *khi nào* gửi SMS và *ghi lại* kết quả? Đó là việc của Decorator."

---

## Slide 21 · Decorator: vấn đề và ý định (≈ 45 giây)

**Nói:**

> "Decorator gắn thêm trách nhiệm cho một đối tượng một cách động, là cách mở rộng chức năng linh hoạt thay cho kế thừa.
>
> Ví dụ là ly cà phê: cà phê đen là gốc, thêm sữa, thêm đường, thêm kem là các lớp bọc. Món nào cũng vẫn là một ly cà phê, và chồng theo tổ hợp nào cũng được.
>
> Nếu dùng kế thừa, mỗi tổ hợp là một lớp: email có lịch sử, email có lịch sử và SMS, email có SMS và chống trùng… Số lớp bùng nổ, và không đổi được thứ tự. Decorator giải quyết bằng cách bọc từng lớp một."

---

## Slide 22 · Decorator: cấu trúc và vai trò (≈ 40 giây)

**Nói:**

> "**Component** là giao diện chung. **ConcreteComponent** là đối tượng gốc. **Decorator** có cùng giao diện, giữ một Component bên trong và mặc định chuyển tiếp lời gọi. **ConcreteDecorator** thêm hành vi trước hoặc sau khi chuyển tiếp.
>
> Có hai điểm cần nhớ. Một, **thứ tự bọc là một quyết định thiết kế**, đổi thứ tự là đổi hành vi. Hai, khác với Adapter, **Decorator giữ nguyên giao diện**: bên ngoài vẫn thấy đúng một dịch vụ thông báo."

---

## Slide 23 · Decorator: bọc dịch vụ thông báo (≈ 1 phút)

**Nói:**

> "Trong hệ thống, dịch vụ thông báo gốc `AsyncEmailNotificationServiceImpl` chỉ biết dựng bản tin và xếp email vào hàng đợi. Phòng khám cần thêm ba việc: **chống gửi trùng** khi nhắc lịch bị gọi hai lần; **gửi thêm SMS**; và **ghi lịch sử thông báo** để tra cứu khi bệnh nhân nói không nhận được email.
>
> Mỗi việc là một lớp bọc: `DeduplicationDecorator`, `SmsNotificationDecorator`, `AuditLogDecorator`. Cả ba kế thừa `NotificationServiceDecorator`, lớp trừu tượng giữ biến `inner` và mặc định chuyển tiếp. Lớp gốc không bị sửa.
>
> Thứ tự từ ngoài vào trong là: **chống trùng, rồi SMS, rồi ghi lịch sử, cuối cùng là email**. Lịch sử nằm ở bảng mới `notification_log`: loại tin, mã lịch hẹn, kênh, kết quả, ghi chú và thời điểm. Bảng này không có khóa ngoại sang lịch hẹn, chỉ liên kết qua mã lịch, để lịch sử độc lập với vòng đời lịch hẹn."

**Chỉ vào:** sơ đồ lớp Decorator, rồi ERD (`notification_log`).

---

## Slide 24 · Decorator: chuỗi bọc khi gửi thông báo (≈ 1 phút)

**Nói (vừa chỉ đoạn code):**

> "Đây là toàn bộ việc lắp ráp, trong `NotificationDecoratorConfig`:"

```java
return new DeduplicationDecorator(
        new SmsNotificationDecorator(
                new AuditLogDecorator(email, history),
                sms, director, history),
        history);
```

> "Các lớp bọc lồng vào nhau bằng `new`, và bean này được đánh dấu `@Primary`, nên mọi nơi tiêm dịch vụ thông báo, kể cả observer email, tự nhận bản đã bọc. Muốn đổi thứ tự hay bỏ một hành vi, chỉ sửa đúng chỗ này.
>
> Tại sao thứ tự này? Ban đầu spec đặt ghi lịch sử ngoài cùng. Nhưng khi đó nó không biết lớp trong đã bỏ qua vì trùng, nên sẽ ghi `SENT` sai. Đặt ghi lịch sử sát email thì nó chỉ ghi lần gửi thật. Chống trùng đứng trước SMS nên lần gửi trùng không sinh SMS thứ hai. Nhóm em đã thử đảo thứ tự và test đỏ ngay.
>
> Ưu điểm: mỗi hành vi một lớp nhỏ; **lỗi SMS chỉ được ghi lại**, không làm hỏng email hay việc đặt lịch. Nhược điểm: nhiều lớp bọc thì khó lần theo khi debug, và thứ tự sai thì ghi lịch sử sai. Một hạn chế nhóm biết rõ: SMS gửi ngay trong giao dịch, còn email chỉ đi sau commit, nên nếu commit hỏng ở bước cuối thì SMS đã đi. Cách sửa là dời SMS sang sau commit.
>
> Mẫu này thể hiện **OCP**, thêm hành vi là thêm lớp bọc; **SRP**, mỗi lớp một việc; và **LSP**, bản đã bọc thay được bản gốc ở mọi nơi."

**Kết phần:**

> "Tóm lại: Adapter cắm nhà cung cấp SMS bên ngoài vào qua `NotificationChannel`; Decorator dùng chính kênh đó trong một lớp bọc, cùng với chống trùng và ghi lịch sử, mà không sửa dịch vụ email gốc."

**Chỉ vào:** bản đồ 7 mẫu (`docs/diagrams/design-patterns-map.png`), hai khung «Decorator» và «Adapter» nối nhau bằng mũi tên `send(phone, message)`.

---

## Demo (≈ 2 phút, tùy chọn)

### Cách 1: chạy test (chắc chắn, không cần mạng)

```bash
bash scripts/maven.sh -q -f backend/pom.xml test -Dtest='SmsChannelAdapterTests,VietnamPhoneTests,EsmsGatewayClientTests,DisabledSmsChannelTests,NotificationDecoratorIntegrationTests,SmsNotificationIntegrationTests,SmsFailureIntegrationTests' -Dsurefire.failIfNoSpecifiedTests=false
```

Kết quả ngày 13/11/2026: **29 test, 0 lỗi** (Adapter 24, Decorator 5). Vừa chạy vừa nói:

> "`SmsChannelAdapterTests`: nhà cung cấp nhận đúng số `+84` và nội dung có mã lịch hẹn; số rỗng hoặc sai thì nhà cung cấp không bị gọi. `VietnamPhoneTests`: 13 dạng số đúng và sai. `EsmsGatewayClientTests`: máy chủ HTTP giả lập đúng định dạng eSMS, mã 100 là gửi được, mã khác là bị từ chối, HTTP lỗi thì ném ngoại lệ, thiếu khóa API thì không khởi động.
> Bên Decorator: nhắc lịch hai lần chỉ ra một email và có dòng `SKIPPED_DUPLICATE`; cho nhà cung cấp SMS sập thì đặt lịch vẫn trả 201, email vẫn đi, SMS ghi `FAILED`."

### Cách 2: đặt lịch thật rồi xem bảng lịch sử

1. Chạy hệ thống, đăng nhập bệnh nhân, đặt một lịch.
2. Mở Mailpit (chế độ demo) để thấy email xác nhận.
3. Truy vấn bảng lịch sử:

```sql
SELECT type, booking_code, channel, status, note FROM notification_log ORDER BY id DESC LIMIT 5;
```

> "Một dòng `EMAIL SENT`, một dòng `SMS SKIPPED_DISABLED` vì SMS đang tắt ở chế độ demo, hoặc `SKIPPED_NO_PHONE` nếu bệnh nhân chưa có số."

---

## Câu hỏi có thể gặp

| Câu hỏi | Trả lời ngắn |
|---|---|
| **Adapter khác Decorator thế nào?** | Adapter **đổi** giao diện (eSMS → `NotificationChannel`). Decorator **giữ nguyên** giao diện và thêm hành vi. |
| **Adapter khác Facade thế nào?** | Facade gom **nhiều** lớp thành một cửa đơn giản hơn. Adapter đổi **một** giao diện thành giao diện khác cho khớp. |
| **Decorator khác Observer ở đây?** | Observer quyết định *khi đặt/hủy lịch thì ai phản ứng*. Decorator thêm hành vi vào *chính việc gửi* và có thể **chặn** nó (chống trùng). Observer không chặn được. |
| **Muốn cắm nhà cung cấp SMS khác (Viettel, Twilio) thì sửa ở đâu?** | Viết một lớp mới hiện thực `SmsGatewayClient`, đăng ký bean trong `SmsGatewayConfig`. Không sửa `SmsChannelAdapter`, Decorator hay luồng đặt lịch. |
| **Muốn thêm kênh Zalo thì sao?** | Viết một Adapter mới hiện thực `NotificationChannel`, rồi thêm một lớp bọc (hoặc cho lớp bọc SMS nhận danh sách kênh). |
| **Muốn thêm hành vi "thử lại khi email lỗi"?** | Thêm một ConcreteDecorator mới và chèn vào `NotificationDecoratorConfig`. Không sửa lớp nào có sẵn. |
| **Sao không dùng AOP `@Around`?** | AOP làm được, nhưng thứ tự và việc bật/tắt nằm ẩn trong annotation. Decorator thấy rõ ở một chỗ và kiểm thử được thứ tự. |
| **Sao chọn object adapter?** | Java kế thừa đơn; giữ client bên trong thì thay bằng bản giả khi test hoặc đổi nhà cung cấp chỉ là đổi bean. |
| **Chống trùng sau khi khởi động lại server còn đúng không?** | Đúng, vì nó tra bảng `notification_log`, không nhớ trong bộ nhớ. |
| **SMS lỗi có làm hỏng đặt lịch không?** | Không. Lớp bọc SMS bắt lỗi và ghi `SMS FAILED`; có test cho nhà cung cấp luôn ném lỗi. |
| **Sao SMS không dấu?** | Tin có dấu phải mã hóa Unicode, mỗi tin chỉ chứa khoảng 70 ký tự thay vì 160, nên tốn gấp đôi. Phần chữ cố định không dấu; tên bác sĩ hiện vẫn giữ nguyên như lưu. |
| **Mã 0 / 400 / -1 ở đâu ra?** | Là mã của giao diện `SmsGatewayClient` do nhóm định nghĩa: `OK = 0`, `REJECTED = 400`, `DISABLED = -1`. `EsmsGatewayClient` dịch `CodeResult = "100"` của eSMS thành `OK`. |
| **Đã gửi SMS thật chưa?** | Chưa. Đã kiểm thử bằng máy chủ HTTP giả lập đúng định dạng eSMS; bước tiếp theo là bật `ESMS_SANDBOX=true`. |

---

## Vị trí để mở nhanh khi được hỏi

| Muốn cho xem | File |
|---|---|
| Target | `backend/src/main/java/com/datlichkhambenh/backend/adapter/NotificationChannel.java` |
| Adapter | `backend/src/main/java/com/datlichkhambenh/backend/adapter/SmsChannelAdapter.java` |
| Adaptee thật | `backend/src/main/java/com/datlichkhambenh/backend/adapter/EsmsGatewayClient.java` |
| Chọn nhà cung cấp | `backend/src/main/java/com/datlichkhambenh/backend/adapter/SmsGatewayConfig.java` |
| Chuỗi bọc | `backend/src/main/java/com/datlichkhambenh/backend/decorator/NotificationDecoratorConfig.java` |
| Chống trùng | `backend/src/main/java/com/datlichkhambenh/backend/decorator/DeduplicationDecorator.java` |
| Gửi thêm SMS | `backend/src/main/java/com/datlichkhambenh/backend/decorator/SmsNotificationDecorator.java` |
| Sơ đồ | `docs/adapter/diagrams/`, `docs/decorator/diagrams/`, `docs/diagrams/design-patterns-map.png` |
