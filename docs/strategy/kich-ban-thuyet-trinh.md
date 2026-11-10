# Kịch bản thuyết trình: Strategy tính phí khám

Đọc kèm bộ slide (20 slide). Thời lượng gợi ý: 12–15 phút + hỏi đáp. Chữ **in đậm** là ý bắt buộc phải nói.

---

## Slide 1 — Bìa (30 giây)

Em chào thầy cô và các bạn. Em trình bày phần em phụ trách trong đồ án hệ thống đặt lịch khám bệnh: **áp dụng mẫu Strategy để tính phí khám**. Hình ảnh xuyên suốt bài là: **một quầy thu ngân, ba bảng giá**.

## Slide 2 — Mẫu Strategy là gì (1 phút)

Trước hết em giới thiệu mẫu Strategy. Đây là **mẫu hành vi** trong 23 mẫu của nhóm GoF. Ý tưởng là: **khi cùng một việc có nhiều cách làm, ta đóng gói mỗi cách vào một lớp riêng**, các lớp tuân theo chung một interface, và chương trình **chọn cách làm lúc chạy** mà nơi gọi không phải sửa.

Mẫu có bốn thành phần: interface **Strategy**, các **Concrete Strategy** là từng cách làm, **Context** là nơi giữ và gọi Strategy, và **Client** là bên quyết định chọn cách nào.

## Slide 3 — Ứng dụng thực tế (1 phút)

Ngoài đời Strategy rất phổ biến. Ứng dụng bản đồ: cùng một điểm đến, chọn đi bộ, xe máy hay ô tô là ba thuật toán tìm đường khác nhau. Thanh toán online: thẻ, ví, chuyển khoản, nhưng chỉ có một nút "Thanh toán". Phí vận chuyển: giao tiết kiệm, giao nhanh, mỗi loại một công thức.

Ngay trong Java, `Comparator` truyền vào hàm sort là một Strategy, và `RoundingMode.HALF_UP` em dùng để làm tròn tiền cũng là một Strategy.

Điểm chung: **một mục tiêu, nhiều cách làm, chọn lúc chạy.**

## Slide 4 — Hiện trạng (45 giây)

Trước khi em làm, hệ thống chỉ biết một cách tính: **giá bằng phí niêm yết của bác sĩ**, chép thẳng vào lịch hẹn. Bệnh nhân không xem được giá trước, không có bảo hiểm hay VIP. Và nếu muốn thêm loại giá, ta phải sửa ngay trong hàm đặt lịch — nơi đang có **khóa chống hai người đặt trùng ca**. Sửa ở đó rất dễ gây lỗi.

## Slide 5 — Ví dụ đời thực (1 phút)

Em lấy ví dụ quầy thu ngân. Thu ngân **không học thuộc mọi công thức**; họ có sẵn nhiều bảng giá, khách đưa thẻ gì thì rút bảng đó. Phòng khám có gói mới thì **in thêm bảng giá**, không phải đào tạo lại thu ngân.

Bảng bên phải là ánh xạ: bảng giá là Concrete Strategy, thu ngân là Context. Lưu ý: **người kiểm thẻ là lễ tân, không phải thu ngân** — đó là lý do em tách BenefitService ra riêng.

## Slide 6 — So sánh phương án (1 phút)

Em đã cân nhắc bốn phương án:
- **if/else trong hàm đặt lịch**: như bắt thu ngân thuộc lòng mọi giá. Loại, vì mỗi lần thêm giá phải sửa giao dịch có khóa.
- **Bảng luật trong cơ sở dữ liệu**: như cuốn sổ quy tắc khổng lồ. Loại, vì quá nặng cho ba công thức.
- **Để client tự gửi giá**: như khách tự ghi giá lên hóa đơn — ngoài đời không ai chấp nhận.
- **Strategy**: nhiều bảng giá, một thu ngân. **Em chọn phương án này.**

## Slide 7 — Vai trò trong code (45 giây)

Interface `ConsultationPricingStrategy` là hợp đồng chung. Ba lớp Standard, Insurance, Vip là ba bảng giá. `PricingContext` là thu ngân: nhận danh sách Strategy do Spring tiêm vào, chọn theo key, làm tròn tiền. Client là hai service báo giá và đặt lịch. **BenefitService không phải thuật toán thứ tư** mà là điều kiện sử dụng.

## Slide 8 — Ba thuật toán (45 giây)

Cùng phí gốc 300 nghìn: **Tiêu chuẩn 300 nghìn, Bảo hiểm giảm 80% còn 60 nghìn, VIP giảm 10% còn 270 nghìn.** Công thức: phí gốc nhân (1 trừ tỷ lệ giảm). Tỷ lệ nằm trong file cấu hình nên đổi chính sách không cần sửa code. **Mỗi lịch một loại, không cộng dồn.** Đây là chính sách giá nội bộ, không phải chi trả của cơ quan bảo hiểm.

## Slide 9 — Quyết định: không dùng setStrategy (1 phút)

Sách thường vẽ Context có hàm `setStrategy`. Nhưng trong Spring, **Context là singleton dùng chung cho mọi request**. Nếu em gọi `setStrategy(VIP)` cho bệnh nhân A, bệnh nhân B đặt cùng lúc có thể bị tính giá VIP. Giống thu ngân treo biển "đang áp giá VIP" lên quầy, người sau bị áp nhầm.

Nên em **truyền key vào mỗi lần tính**, registry bất biến, trùng key thì báo lỗi ngay khi khởi động. Có test chứng minh **thêm Strategy mới mà Context không đổi**.

## Slide 10 — Quyết định: tách quyền hưởng (1 phút)

Biết công thức VIP không có nghĩa là được dùng VIP. Nếu server tin key do trình duyệt gửi, **ai sửa request cũng được giảm 80%**. Nên em làm quy trình: bệnh nhân gửi mã hồ sơ, **Admin đối chiếu và duyệt** kèm lý do, thời hạn. Server kiểm tra quyền còn hiệu lực **cả hôm nay lẫn ngày khám**, theo giờ Việt Nam.

## Slide 11 — UML lớp (30 giây)

Sơ đồ lớp báo giá: interface ở giữa, ba lớp hiện thực, Context phụ thuộc interface, service là client. Em có **bốn sơ đồ**: lớp và tuần tự cho báo giá, lớp và tuần tự cho đặt lịch.

## Slide 12 — UML tuần tự đặt lịch (1 phút)

Khi bệnh nhân xác nhận, server làm sáu bước trong một giao dịch: **khóa ca** (hai người không thể ngồi cùng một ghế), **khóa bác sĩ** (phí không đổi giữa chừng), **khóa quyền hưởng** (Admin không thu hồi chen vào), rồi mới **gọi Strategy tính lại**, so với giá đã báo, khớp thì lưu snapshot, và **email chỉ gửi sau khi commit**.

## Slide 13 — Server tính lại và snapshot (45 giây)

Hai nguyên tắc. Một: **giá trình duyệt gửi chỉ để so sánh**, server luôn tự tính lại — thu ngân không để khách tự ghi tiền. Hai: khi đặt xong em **lưu snapshot** gồm loại giá, phí gốc, tỷ lệ, giá cuối và quyền đã dùng. Bác sĩ tăng phí hay quyền bị thu hồi sau đó, lịch cũ vẫn giữ giá — như **hóa đơn đã in thì không in lại**.

## Slide 14 — Dữ liệu thô (1 phút 30 giây)

Đây không phải dự án học máy nên không có dataset huấn luyện, nhưng **dữ liệu đầu vào thật sự có vấn đề**:
- Phí bác sĩ có thể trống, âm hoặc quá lớn → chặn trước và sau làm tròn.
- Giảm giá sinh số lẻ đồng → BigDecimal, làm tròn HALF_UP, không dùng double.
- Tỷ lệ cấu hình gõ sai → ép trong khoảng 0–1, sai thì ứng dụng không khởi động.
- JavaScript mất chữ số → thêm trường tỷ lệ dạng chuỗi.
- Lịch cũ không có loại giá → giữ NULL, **không đoán bừa**.
- Máy khách khác múi giờ → mọi ngày tính theo giờ Việt Nam. Lỗi này em bắt được nhờ test Chrome giả lập Honolulu lúc nửa đêm giờ Việt Nam.

## Slide 15 — Khi có sự cố (45 giây)

Nguyên tắc: **không tự quyết thay bệnh nhân**. Giá đổi: hiện cũ → mới, giữ triệu chứng, chờ bấm xác nhận lại. Mất quyền: bắt chọn lại, **không âm thầm hạ về giá Tiêu chuẩn**. Mất ca: chọn ca mới. Mọi nhánh lỗi đều rollback, không gửi email.

## Slide 16 — Bằng chứng kiểm thử (45 giây)

**118 test Java** trên H2 và MySQL thật, **16 kịch bản Chrome**, **11 test Node**, **5 SQL nâng cấp**. Một reviewer độc lập tìm ra hai lỗi quan trọng; em viết test tái hiện, thấy đỏ, sửa xong thấy xanh.

## Slide 17 — Đánh giá theo yêu cầu đề (1 phút 30 giây)

Đề yêu cầu độ tin cậy, hiệu chuẩn, bootstrap, công bằng giới. Em nói thẳng: **các chỉ số này dành cho mô hình xác suất**. Hệ thống của em là quy tắc tất định — cùng đầu vào luôn ra cùng giá — nên **em không bịa số liệu**. Em đánh giá tương đương:
- Độ tin cậy → bằng chứng kiểm thử bốn tầng.
- Hiệu chuẩn → giá báo phải khớp giá chốt.
- Bootstrap → kiểm thử biên và tranh chấp đồng thời.
- Công bằng → công thức **không đọc giới tính hay tuổi**; cùng phí, cùng quyền thì cùng giá. Rủi ro còn lại ở khâu Admin duyệt tay, nên mọi quyết định đều có lý do, người duyệt và lịch sử để kiểm toán.

## Slide 18 — Hiệu quả (1 phút)

- **OCP**: thêm VIP chỉ là thêm một lớp, Context không đổi dòng nào.
- Báo giá và đặt lịch **dùng chung một Context** nên không thể lệch công thức.
- **SRP**: mỗi lớp một việc, test gọn.
- **DIP**: service chỉ phụ thuộc interface.

## Slide 19 — Nhược điểm (1 phút)

Em cũng thấy nhược điểm:
- **Nhiều lớp**: ba công thức khác nhau một con số mà cần năm lớp. Em chấp nhận vì cần mở rộng và kiểm thử.
- **Client phải biết chọn**, nên phải làm thêm BenefitService.
- **Không hợp quy tắc kết hợp**: muốn cộng dồn hay phụ thu cuối tuần nên dùng Decorator hoặc rule engine.
- Tỷ lệ phải **giống nhau giữa các server**.
- **Khóa bác sĩ** làm các lượt đặt cùng bác sĩ chờ nhau một chút.
- Chưa có thanh toán, chưa kết nối cơ quan bảo hiểm.

## Slide 20 — Tổng kết (30 giây)

Tóm lại: **một thu ngân, ba bảng giá, không ai tự ghi giá.** Strategy tách công thức, Context chọn theo từng yêu cầu, quyền hưởng được xác minh, server tính lại và chốt snapshot. Em xin cảm ơn và mời thầy cô đặt câu hỏi.

---

## Chuẩn bị hỏi đáp

**Hỏi: Strategy khác State thế nào?**
Cả hai đều có interface và nhiều lớp hiện thực. State thì **đối tượng tự chuyển trạng thái** và hành vi đổi theo trạng thái bên trong. Strategy thì **bên ngoài chọn** thuật toán, các Strategy không biết nhau. Ở đây bệnh nhân chọn loại giá, không có chuyển trạng thái giữa Tiêu chuẩn và VIP, nên là Strategy.

**Hỏi: Sao không dùng Factory?**
Factory trả lời "tạo đối tượng nào", Strategy trả lời "dùng thuật toán nào". `PricingContext` có vai trò giống registry/factory để tra Strategy theo key, nhưng trọng tâm là thay thế thuật toán, nên mẫu chính là Strategy.

**Hỏi: Ba công thức giống nhau, sao không một lớp có tham số tỷ lệ?**
Đúng là hiện tại một lớp tham số hóa cũng chạy được. Em chọn tách vì chính sách sẽ khác nhau về sau (ví dụ Bảo hiểm có trần chi trả, VIP có phí cố định). Đây cũng là nhược điểm em đã nêu: hiện tại hơi cồng kềnh.

**Hỏi: Thêm loại giá "Sinh viên" làm thế nào?**
Tạo `StudentPricingStrategy` implement interface, đánh dấu `@Component`, thêm cấu hình tỷ lệ. Context tự nhận. Nếu cần điều kiện, thêm loại quyền `STUDENT` cho BenefitService.

**Hỏi: Nếu Admin thu hồi quyền đúng lúc bệnh nhân đang đặt?**
Hai bên khóa cùng bản ghi quyền hưởng. Bên nào giữ khóa trước thì bên kia chờ. Nếu thu hồi xong trước, lượt đặt kiểm tra lại dưới khóa và trả 403. Có test chạy trên cả H2 và MySQL.

**Hỏi: Tại sao không tự chọn mức giảm có lợi nhất cho bệnh nhân?**
Vì đó là quyết định của bệnh nhân. Ví dụ họ có thể muốn giữ quota bảo hiểm cho lần khám khác.

**Hỏi: Đánh giá công bằng có đo được không?**
Công thức không dùng giới tính nên không có thiên lệch ở phần tính giá. Phần có thể thiên lệch là Admin duyệt tay. Hệ thống đã lưu đủ dữ liệu (lý do, người duyệt, thời điểm) để sau này thống kê tỷ lệ duyệt theo nhóm; em chưa làm thống kê đó.
