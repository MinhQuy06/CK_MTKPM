# Spec: Gom luật giá chào thành module BookingOffer

Status: ready-for-agent

> Không bắt đầu trước 12/11/2026 (ngày nộp bài). Code đang chạy phải khớp sơ đồ và kịch bản thuyết trình đến hết ngày đó.

## Problem Statement

Câu hỏi "ca này có đặt được không, với loại phí này bệnh nhân trả bao nhiêu" đang được trả lời ở hai nơi: lúc báo giá và lúc xác nhận đặt lịch. Hai bản sao kiểm tra cùng các luật (ca đặt được, loại phí mặc định và được hỗ trợ, quyền hưởng còn hiệu lực, tính giá) nhưng đã lệch nhau:

- Loại phí không hỗ trợ: báo giá trả 400 không mã, đặt lịch trả `UNSUPPORTED_STRATEGY`.
- Bác sĩ không nhận lịch: báo giá trả 409 không mã, đặt lịch trả 400 không mã.
- Luật quyền hưởng còn hiệu lực được viết hai lần trong cùng module quyền hưởng.
- Thứ tự khóa ca → bác sĩ → quyền hưởng chỉ tồn tại ngầm qua thứ tự câu lệnh.
- Việc đặt lịch kiểu cũ hay có giá đã đồng ý được quyết định bằng một cờ trên dữ liệu request, do Facade bật lên.

Người bảo trì sửa một luật phải nhớ sửa hai chỗ; người dùng nhận mã lỗi khác nhau cho cùng một tình huống.

## Solution

Một deep module **giá chào (BookingOffer)** trả lời câu hỏi trên ở đúng một chỗ, với interface ba thao tác: xem loại phí áp dụng được cho một ca, báo giá một ca theo một loại phí, và khóa rồi xác nhận một giá chào so với điều bệnh nhân đã đồng ý. Báo giá, danh sách loại phí và đặt lịch đều gọi module này. Việc lưu lịch hẹn, đánh dấu ca đã kín và phát sự kiện vẫn ở dịch vụ đặt lịch.

Người dùng không thấy khác biệt ngoài hai mã lỗi được thống nhất.

## User Stories

1. Là bệnh nhân, tôi muốn nhận cùng một lý do từ chối khi chọn loại phí không hỗ trợ, dù ở bước báo giá hay đặt lịch, để thông báo nhất quán.
2. Là bệnh nhân, tôi muốn nhận cùng một lý do khi bác sĩ ngừng nhận lịch, dù ở bước nào, để biết phải chọn bác sĩ khác.
3. Là bệnh nhân, tôi muốn danh sách loại phí, giá chào và kết quả đặt lịch dùng cùng một luật quyền hưởng, để không thấy Bảo hiểm ở bước chọn rồi bị từ chối ở bước đặt mà không có lý do thật.
4. Là bệnh nhân, tôi muốn vẫn được yêu cầu xác nhận lại khi giá đổi giữa lúc báo giá và lúc đặt, như hiện nay.
5. Là bệnh nhân, tôi muốn vẫn được báo ca không còn đặt được khi ngày giờ ca đổi sau lúc tôi xem giá, như hiện nay.
6. Là client cũ, tôi muốn đặt lịch kiểu cũ vẫn chạy với loại phí Tiêu chuẩn, để không phải nâng cấp ngay.
7. Là người bảo trì, tôi muốn mọi luật ca đặt được nằm ở một module, để sửa một chỗ là đúng cho cả báo giá và đặt lịch.
8. Là người bảo trì, tôi muốn danh sách lý do từ chối là một tập có tên kèm mã và HTTP status, để không còn chuỗi rời rạc.
9. Là người bảo trì, tôi muốn thứ tự khóa được ghi trong interface của module và chỉ thực hiện ở module, để không ai vô tình khóa ngược thứ tự.
10. Là người bảo trì, tôi muốn thao tác khóa và xác nhận báo lỗi ngay khi bị gọi ngoài giao dịch, thay vì âm thầm không khóa.
11. Là người bảo trì, tôi muốn luật quyền hưởng còn hiệu lực chỉ có một bản, dùng chung cho báo giá, đặt lịch và trạng thái hiển thị.
12. Là người bảo trì, tôi muốn dịch vụ đặt lịch chỉ còn điều phối (gọi module, lưu, phát sự kiện), để đọc một lượt là hiểu.
13. Là người bảo trì, tôi muốn Facade quyết định "có giá đã đồng ý hay đặt kiểu cũ" bằng một giá trị rõ ràng, để bỏ cờ trên dữ liệu request.
14. Là người kiểm thử, tôi muốn test gọi thẳng interface module cho từng lý do từ chối, để không phải dựng request HTTP cho mỗi luật.
15. Là người kiểm thử, tôi muốn mọi test HTTP và trình duyệt hiện có vẫn pass (trừ mã lỗi kỳ vọng đã thống nhất), để chứng minh hành vi không đổi.
16. Là người thực hiện đồ án, tôi muốn sơ đồ class và sequence của Strategy và Facade được sinh lại theo code mới, để tài liệu không lệch.

## Implementation Decisions

- **Module giá chào** thuộc phần Strategy (phía client của Strategy). Ba thao tác:
  - *xem loại phí*: cho một bệnh nhân và một ca, trả các loại phí áp dụng được (Tiêu chuẩn luôn có; Bảo hiểm/VIP khi có quyền hưởng còn hiệu lực cho ngày khám). Chỉ đọc.
  - *báo giá*: cho bệnh nhân, ca, loại phí (mặc định Tiêu chuẩn); trả **giá chào** (phí gốc, tỷ lệ giảm, giá cuối, ngày giờ ca). Chỉ đọc, không khóa.
  - *khóa và xác nhận*: cho bệnh nhân, ca, loại phí và một **kỳ vọng**; khóa ca → bác sĩ → quyền hưởng (nếu có), kiểm tra lại mọi luật, so với kỳ vọng, trả **giá chào đã khóa** (ca, bác sĩ, quyền hưởng đã dùng hoặc không, số tiền). Bắt buộc chạy trong giao dịch của caller; gọi ngoài giao dịch là lỗi.
- **Kỳ vọng** có đúng hai dạng:
  - *giá đã đồng ý*: phí gốc, tỷ lệ giảm, giá cuối, ngày, giờ bắt đầu, giờ kết thúc. Lệch số tiền → giá đã đổi; lệch ngày giờ → ca không còn đặt được.
  - *đặt lịch kiểu cũ*: chỉ cho phép Tiêu chuẩn.
- **Facade** tạo kỳ vọng: xác nhận có báo giá tạo *giá đã đồng ý*; đặt kiểu cũ tạo *đặt lịch kiểu cũ*. Cờ "bắt buộc báo giá" trên dữ liệu request bị bỏ; việc chặn hạ cấp từ đường xác nhận xuống kiểu cũ đúng theo cấu trúc.
- **Lý do từ chối** là một tập đóng do module sở hữu, mỗi lý do có mã, HTTP status và thông điệp mặc định:
  - `SLOT_UNAVAILABLE`: 404 khi ca không tồn tại, 409 khi đã kín, đã qua hoặc đổi ngày giờ.
  - `PRICE_CHANGED`: 409.
  - `BENEFIT_UNAVAILABLE`: 403.
  - `UNSUPPORTED_STRATEGY`: 400, áp dụng cho cả báo giá.
  - `DOCTOR_UNAVAILABLE` *(mới)*: 409, cho cả báo giá và đặt lịch.
- **Ca đặt được** (một định nghĩa, dùng cho cả ba thao tác): tồn tại, còn trống, chưa có lịch hẹn, giờ bắt đầu sau hiện tại theo giờ Việt Nam, bác sĩ đã duyệt và tài khoản đang hoạt động.
- **Quyền hưởng còn hiệu lực**: module quyền hưởng giữ đúng một quy tắc ngày (chứa cả hôm nay và ngày khám, giờ Việt Nam, kể cả biên), dùng cho tìm thường, tìm có khóa và trạng thái hiển thị.
- **Tính giá** vẫn qua Context Strategy hiện có (không đổi).
- **Dịch vụ đặt lịch** nhận giá chào đã khóa, tạo lịch hẹn với giá chốt, đánh dấu ca đã kín, phát sự kiện đã đặt lịch. Không còn tự kiểm tra luật giá hay ca.
- **Kết quả** của module là record miền (giá chào, giá chào đã khóa), không phải DTO; controller báo giá tự chuyển sang dữ liệu trả về hiện có. Hợp đồng JSON của báo giá, danh sách loại phí và đặt lịch không đổi.
- **Thuật ngữ** theo `GLOSSARY.md`: giá chào, giá đã đồng ý, đặt lịch kiểu cũ, ca đặt được, quyền hưởng còn hiệu lực, lý do từ chối.

## Testing Decisions

- Test tốt chỉ kiểm hành vi nhìn thấy ở interface: giá trị trả về, lý do từ chối (mã + status), dữ liệu đã lưu; không kiểm số lần gọi repository.
- **Lưới an toàn**: mọi test HTTP và trình duyệt hiện có (hợp đồng đặt lịch Strategy, báo giá, quyền hưởng, quyết định quyền hưởng, Facade, Observer, Decorator, bộ trình duyệt Strategy) phải pass. Chỉ sửa mã lỗi kỳ vọng ở hai tình huống đã thống nhất.
- **Test mới tại interface module**, chạy với Spring + H2 thật (không mock): mỗi lý do từ chối một test cho báo giá và một cho khóa và xác nhận; giá chào đúng cho từng loại phí; kỳ vọng giá đã đồng ý khớp / lệch số tiền / lệch ngày giờ; đặt lịch kiểu cũ từ chối loại phí khác Tiêu chuẩn; khóa và xác nhận ngoài giao dịch bị từ chối.
- Prior art: hợp đồng đặt lịch Strategy (MockMvc + bắt bản tin email), fixture đặt lịch qua API dùng cho các mẫu thiết kế, test tích hợp báo giá và quyền hưởng.
- Test đơn vị dịch vụ đặt lịch dựa trên mock (hủy lịch) giữ nguyên; nếu nó buộc phải đổi vì dịch vụ đặt lịch có collaborator mới, chỉ sửa phần dựng đối tượng.

## Out of Scope

- Đổi hợp đồng JSON hay URL nào ngoài hai mã lỗi đã thống nhất.
- Đổi công thức tính giá, cấu hình tỷ lệ giảm hay Context Strategy.
- Gom phần kiểm tra quyền hưởng trong luồng Admin duyệt/thu hồi.
- Dọn các ứng viên kiến trúc khác (interface thông báo, bản sao frontend, mở tài khoản, mapping lịch hẹn của Admin).
- Kiểm thử tranh chấp khóa trên MySQL ngoài những gì bộ test MySQL hiện có đã làm.

## Further Notes

- Kết quả của buổi rà soát kiến trúc ngày 11/11/2026 (ứng viên 1) và vòng hỏi đáp thiết kế cùng ngày.
- Sau khi code xong phải sinh lại sơ đồ class và sequence của Strategy (đặt lịch, báo giá) và Facade, cập nhật README hai mẫu và kịch bản thuyết trình nếu còn dùng.
- Tên gọi `BookingOffer`, `Expectation` (kỳ vọng), `BookingRejection` (lý do từ chối) là tên dự kiến cho code; khái niệm nằm trong `GLOSSARY.md`.
