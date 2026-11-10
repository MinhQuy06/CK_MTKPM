# Spec: Thiết kế lại hệ thống đặt lịch khám bằng 7 mẫu thiết kế

Status: ready-for-agent

## Problem Statement

Đề cuối kỳ yêu cầu áp dụng tối thiểu 6 mẫu thiết kế vào hệ thống đặt lịch khám bệnh, mỗi mẫu có 4 sơ đồ (class, sequence, ERD, DFD). Hiện hệ thống mới có một mẫu được làm bài bản là Strategy tính phí khám. Các phần còn lại viết theo kiểu dồn việc vào service:

- Dịch vụ đặt lịch gọi trực tiếp gửi email và xóa cache slot ngay trong luồng nghiệp vụ, nên muốn thêm một phản ứng mới với việc đặt/hủy lịch thì phải sửa dịch vụ đặt lịch.
- Bản tin email được dựng bằng một constructor 9 tham số, dễ truyền nhầm thứ tự, không kiểm tra trường bắt buộc.
- Thông báo chỉ có kênh email. Muốn thêm SMS từ một nhà cung cấp có giao diện khác thì không có chỗ cắm.
- Không có lịch sử thông báo đã gửi, không chống gửi trùng.
- Việc tạo tài khoản Bệnh nhân/Bác sĩ (lúc đăng ký và lúc Admin tạo) lặp lại logic dựng User + hồ sơ ở nhiều nơi.
- Controller đặt lịch phải biết chi tiết các dịch vụ bên dưới.

Hạn nộp là 12/11/2026 và một người làm toàn bộ, nên thay đổi phải nhỏ, không đổi hành vi người dùng đang thấy, có test chứng minh.

## Solution

Refactor hệ thống đang chạy để áp dụng 7 mẫu, mỗi mẫu giải quyết một vấn đề có thật ở trên và nối vào đúng một luồng. Người dùng (Bệnh nhân, Bác sĩ, Admin) dùng các URL, API, giao diện như cũ; thứ mới nhìn thấy được là SMS giả lập và lịch sử thông báo.

| Mẫu | Áp dụng ở đâu |
|---|---|
| Strategy | Tính phí khám (đã xong, giữ nguyên) |
| Builder | Dựng bản tin thông báo; Director có sẵn công thức cho đặt lịch/hủy/nhắc lịch |
| Observer | Đặt/hủy lịch phát sự kiện; observer gửi email và observer xóa cache slot phản ứng |
| Adapter | Client SMS giả lập (giao diện khác) được bọc thành kênh thông báo chung |
| Decorator | Bọc dịch vụ thông báo: ghi lịch sử → chống gửi trùng → gửi thêm SMS → email gốc |
| Factory | Tạo tài khoản Bệnh nhân/Bác sĩ theo vai trò, dùng chung cho đăng ký và Admin tạo |
| Facade | Một cửa đặt lịch đứng trước controller đặt lịch |

Mỗi mẫu có 4 sơ đồ draw.io + PNG đen trắng sinh bằng script, một README ngắn và một kịch bản nói. Có thêm một trang tổng thể với bản đồ 7 mẫu.

## User Stories

### Bệnh nhân

1. Là Bệnh nhân, tôi muốn đặt lịch qua đúng trang và API như trước, để không phải học lại cách dùng sau khi hệ thống được thiết kế lại.
2. Là Bệnh nhân, tôi muốn vẫn nhận email xác nhận khi đặt lịch thành công, để biết lịch đã được ghi nhận.
3. Là Bệnh nhân, tôi muốn vẫn nhận email khi hủy lịch, để có bằng chứng đã hủy.
4. Là Bệnh nhân có số điện thoại, tôi muốn nhận thêm tin SMS khi đặt/hủy lịch, để biết ngay cả khi không mở email.
5. Là Bệnh nhân không khai số điện thoại, tôi muốn việc đặt lịch vẫn thành công và vẫn nhận email, để thiếu số điện thoại không chặn tôi.
6. Là Bệnh nhân, tôi muốn không nhận hai thông báo giống hệt nhau cho cùng một lịch hẹn, để không bị làm phiền.
7. Là Bệnh nhân, tôi muốn khi đặt lịch thất bại (hết ca, đổi giá, mất quyền hưởng) thì không nhận email hay SMS nào, để không bị báo nhầm.
8. Là Bệnh nhân, tôi muốn ca vừa đặt biến mất khỏi danh sách ca trống ngay, để người khác không thấy ca đã kín.
9. Là Bệnh nhân, tôi muốn ca vừa hủy xuất hiện lại trong danh sách ca trống, để người khác đặt được.
10. Là Bệnh nhân, tôi muốn giá khám trong email đúng với giá đã chốt theo Strategy, để không có hai con số khác nhau.
11. Là Bệnh nhân, tôi muốn đăng ký tài khoản như cũ, để hồ sơ bệnh nhân được tạo cùng tài khoản.
12. Là Bệnh nhân, tôi muốn xem chi tiết và danh sách lịch hẹn của mình như cũ, để theo dõi lịch khám.

### Bác sĩ

13. Là Bác sĩ, tôi muốn tự đăng ký tài khoản như cũ, để hồ sơ bác sĩ chờ Admin duyệt được tạo cùng tài khoản.
14. Là Bác sĩ, tôi muốn xem chi tiết lịch hẹn của mình như cũ, để chuẩn bị buổi khám.

### Admin

15. Là Admin, tôi muốn tạo tài khoản Bác sĩ như cũ, để thêm bác sĩ mà không cần họ tự đăng ký.
16. Là Admin, tôi muốn tạo tài khoản Bệnh nhân như cũ, để hỗ trợ bệnh nhân tại quầy.
17. Là Admin, tôi muốn mỗi thông báo đã gửi được lưu lại (loại, mã lịch hẹn, kênh, kết quả, thời điểm), để tra cứu khi bệnh nhân khiếu nại không nhận được tin.
18. Là Admin, tôi muốn biết thông báo nào bị bỏ qua vì trùng hoặc vì thiếu số điện thoại, để phân biệt với lỗi thật.

### Người bảo trì

19. Là người bảo trì, tôi muốn thêm một phản ứng mới với việc đặt/hủy lịch bằng cách thêm một observer, để không phải sửa dịch vụ đặt lịch.
20. Là người bảo trì, tôi muốn dịch vụ đặt lịch không còn phụ thuộc trực tiếp vào email và cache, để nó chỉ lo nghiệp vụ đặt lịch.
21. Là người bảo trì, tôi muốn bản tin thông báo được dựng qua builder có tên trường, để không truyền nhầm thứ tự 9 tham số.
22. Là người bảo trì, tôi muốn builder từ chối bản tin thiếu trường bắt buộc, để lỗi lộ ra lúc dựng thay vì lúc gửi.
23. Là người bảo trì, tôi muốn các bản tin đặt lịch/hủy/nhắc lịch được dựng ở một Director, để công thức dựng nằm một chỗ.
24. Là người bảo trì, tôi muốn thay nhà cung cấp SMS bằng cách viết một adapter mới, để phần còn lại của hệ thống không đổi.
25. Là người bảo trì, tôi muốn adapter chuẩn hóa số điện thoại Việt Nam về dạng nhà cung cấp yêu cầu, để chênh lệch định dạng chỉ nằm ở adapter.
26. Là người bảo trì, tôi muốn bật/tắt hoặc đổi thứ tự từng lớp bọc thông báo ở một chỗ cấu hình, để thêm hành vi mà không sửa dịch vụ email gốc.
27. Là người bảo trì, tôi muốn lỗi SMS không làm hỏng email hay việc đặt lịch, để một kênh phụ không kéo sập luồng chính.
28. Là người bảo trì, tôi muốn việc tạo tài khoản theo vai trò nằm ở một factory dùng chung, để đăng ký và Admin tạo không lặp logic.
29. Là người bảo trì, tôi muốn vai trò không hợp lệ bị từ chối như hiện nay, để refactor không mở thêm lỗ hổng.
30. Là người bảo trì, tôi muốn controller đặt lịch chỉ nói chuyện với một Facade, để controller mỏng và dễ đọc.
31. Là người bảo trì, tôi muốn các endpoint Strategy (báo giá, quyền hưởng) giữ nguyên, để không phá phần đã kiểm thử kỹ.
32. Là người bảo trì, tôi muốn toàn bộ test hiện có (71 test Java, test Node, test trình duyệt Strategy) vẫn pass mà không sửa, để chứng minh hành vi không đổi.

### Người thực hiện đồ án

33. Là người thực hiện đồ án, tôi muốn mỗi mẫu có class, sequence, ERD, DFD dạng draw.io + PNG đen trắng, để đáp ứng yêu cầu 4 sơ đồ.
34. Là người thực hiện đồ án, tôi muốn sơ đồ sinh bằng script dùng chung như Strategy, để sửa và xuất lại nhanh, phong cách đồng nhất.
35. Là người thực hiện đồ án, tôi muốn tên lớp và bảng trong sơ đồ khớp đúng code, để không bị hỏi vặn khi thuyết trình.
36. Là người thực hiện đồ án, tôi muốn một bản đồ tổng thể cho thấy 7 mẫu nằm ở đâu và gọi nhau thế nào, để trình bày "thiết kế lại hệ thống" trong một trang.
37. Là người thực hiện đồ án, tôi muốn mỗi mẫu có README ngắn (định nghĩa, bài toán, vai trò trong code, sơ đồ, hạn chế) và kịch bản nói khoảng 5 phút, để chuẩn bị thuyết trình.
38. Là nhóm, tôi muốn một bảng thành viên để trống trong README chung, để nhóm tự điền phân công.
39. Là người thực hiện đồ án, tôi muốn chọn 4 sơ đồ chính của Strategy trong 11 sơ đồ hiện có, để Strategy cùng chuẩn với 6 mẫu còn lại.
40. Là người thực hiện đồ án, tôi muốn mỗi mẫu là một hoặc vài commit nhỏ trên nhánh riêng, để duyệt và quay lui dễ.

## Implementation Decisions

### Chung

- Refactor tại chỗ, không viết lại hệ thống. Mỗi mẫu có package riêng cùng cấp với package `strategy` hiện có (`builder`, `observer`, `adapter`, `decorator`, `factory`, `facade`).
- Không đổi URL, hợp đồng API, phân quyền, công thức giá, giao diện hiện có.
- Làm trên một nhánh `design-patterns` tách từ `thaihoa`; mỗi ticket một commit; gộp vào `main` khi xong.
- Thứ tự làm: Builder → Observer → Adapter → Decorator → Factory → Facade → sơ đồ → tài liệu.

### Builder

- Bản tin thông báo (record 9 trường: loại, email người nhận, tên bệnh nhân, mã lịch hẹn, tên bác sĩ, ngày, giờ bắt đầu, giờ kết thúc, giá) có một builder viết tay với các hàm theo tên trường.
- `build()` từ chối khi thiếu loại, email người nhận hoặc mã lịch hẹn. Giá được phép trống (lịch cũ không có giá).
- Director nhận một lịch hẹn và trả bản tin cho ba trường hợp: đặt lịch thành công, hủy lịch, nhắc lịch. Dịch vụ gửi email chỉ gọi Director.
- Nội dung bản tin gửi ra RabbitMQ giống hệt hiện nay.

### Observer

- Hai sự kiện miền: đã đặt lịch, đã hủy lịch; mang lịch hẹn đã lưu.
- Dịch vụ đặt lịch chỉ phát sự kiện; bỏ phụ thuộc trực tiếp vào dịch vụ email và dịch vụ cache.
- Observer email gọi dịch vụ thông báo (bản đã được Decorator bọc). Observer cache xóa cache slot của bác sĩ + ngày làm việc.
- Dùng cơ chế sự kiện của Spring. Thời điểm gửi email giữ như hiện nay: bản tin chỉ ra RabbitMQ sau khi giao dịch commit (cơ chế AFTER_COMMIT đang có). Luồng thất bại rollback thì không có email, SMS hay bản ghi lịch sử nào.
- Không có observer ghi log riêng (việc ghi lịch sử thuộc Decorator).

### Adapter

- Giao diện đích: kênh thông báo, gửi một thông báo tới một bệnh nhân, trả kết quả gửi/bỏ qua/lỗi.
- Adaptee: client SMS giả lập của "nhà cung cấp", chữ ký khác (số điện thoại quốc tế + nội dung văn bản), chỉ ghi nhận tin chứ không gửi thật.
- Adapter chuyển bản tin thành nội dung SMS ngắn, chuẩn hóa số Việt Nam (0xxxxxxxxx → +84xxxxxxxxx), bỏ qua khi bệnh nhân không có số điện thoại.
- Adapter dùng composition (giữ tham chiếu client), không kế thừa client.

### Decorator

- Lớp bọc trừu tượng cài đặt giao diện dịch vụ thông báo hiện có (đặt lịch/hủy/nhắc lịch) và giữ tham chiếu tới thành phần được bọc.
- Ba decorator cụ thể, thứ tự từ ngoài vào trong: ghi lịch sử → chống gửi trùng → gửi thêm SMS → dịch vụ email gốc.
  - Ghi lịch sử: mỗi lần gọi ghi một dòng.
  - Chống gửi trùng: nếu đã có dòng cùng loại + cùng mã lịch hẹn với kết quả đã gửi thì bỏ qua và ghi kết quả "bỏ qua do trùng".
  - Gửi thêm SMS: gọi email bên trong trước, rồi gửi SMS qua kênh Adapter; lỗi SMS chỉ được ghi lại, không ném ra ngoài.
- Chuỗi được ghép ở một lớp cấu hình và đánh dấu là bean chính; mọi nơi đang tiêm dịch vụ thông báo tự nhận bản đã bọc.
- Bảng mới `notification_log`: id, loại thông báo, mã lịch hẹn, kênh (EMAIL/SMS), kết quả (SENT/SKIPPED_DUPLICATE/SKIPPED_NO_PHONE/FAILED), ghi chú, thời điểm tạo. Thêm vào `schema.sql`; môi trường dev tạo bảng qua JPA.

### Factory

- Factory tạo tài khoản theo vai trò: Bệnh nhân, Bác sĩ. Mỗi sản phẩm dựng User đúng vai trò + hồ sơ đi kèm (Bác sĩ tự đăng ký ở trạng thái chờ duyệt, Admin tạo thì đã duyệt như hiện nay).
- Đăng ký và Admin tạo Bệnh nhân/Bác sĩ đều dùng factory này.
- Vai trò không hợp lệ bị từ chối với cùng lỗi như hiện nay. Không có API tạo Admin (Admin chỉ có qua dữ liệu mẫu), nên Factory không có sản phẩm Admin.

### Facade

- Một Facade đặt lịch phủ năm endpoint của controller đặt lịch: tạo lịch cũ (tương thích), xác nhận đặt lịch, hủy, xem chi tiết, danh sách lịch của tôi.
- Facade gom dịch vụ đặt lịch và dịch vụ người dùng hiện tại; controller đặt lịch chỉ phụ thuộc Facade.
- Các controller Strategy (báo giá, quyền hưởng) không đi qua Facade.

### Sơ đồ và tài liệu

- Tách thư viện draw.io của Strategy thành thư viện dùng chung có tham số thư mục đầu ra; script Strategy vẫn sinh ra sơ đồ giống hệt.
- Mỗi mẫu một script sinh đủ class, sequence, ERD, DFD; xuất PNG bằng script xuất chung.
- Mẫu không thêm bảng thì ERD vẽ các bảng nó đọc/ghi và ghi chú rõ.
- Strategy chọn 4 sơ đồ chính trong 11 sơ đồ hiện có; số còn lại là phụ lục.
- Mỗi mẫu mới: một README + một kịch bản nói. Strategy giữ bộ tài liệu đầy đủ.
- README chung ở `docs/` có bản đồ 7 mẫu và bảng thành viên để trống. Tài liệu không ghi tên người phụ trách.

## Testing Decisions

- Test chỉ kiểm tra hành vi nhìn thấy được ở biên, không kiểm tra số lần gọi phương thức nội bộ. Một refactor đúng phải để test cũ pass nguyên vẹn.
- **Seam chính: HTTP API** qua MockMvc với profile `test` (H2), quan sát ở các biên ra ngoài:
  - Email: bắt bản tin tại RabbitTemplate (tái dùng cách bắt của hợp đồng kiểm thử đặt lịch Strategy).
  - SMS: thay client SMS giả lập bằng bean ghi nhận tin.
  - Lịch sử thông báo: đọc bảng `notification_log`.
  - Cache: thay dịch vụ cache bằng bản ghi nhận các lần xóa.
  - Tài khoản: đăng ký và Admin tạo tài khoản qua API, rồi đăng nhập/đọc hồ sơ.
- **Seam phụ: bean dịch vụ thông báo** chỉ dùng cho chống gửi trùng và thứ tự lớp bọc, vì nhắc lịch hiện không có chỗ gọi qua API.
- Builder và Adapter có thêm test đơn vị nhỏ cho quy tắc thuần (trường bắt buộc, chuẩn hóa số điện thoại) vì đó là đầu vào/đầu ra rõ ràng.
- Prior art: hợp đồng kiểm thử đặt lịch Strategy (MockMvc + bắt RabbitTemplate), test tích hợp bảo mật xác thực (đăng ký/đăng nhập qua API), test đơn vị dịch vụ đặt lịch (Mockito), test Context Strategy.
- Toàn bộ 71 test Java hiện có, test Node và test trình duyệt Strategy phải pass sau mỗi mẫu.

## Out of Scope

- Gửi SMS thật hoặc tích hợp nhà cung cấp SMS thật.
- Giao diện mới (kể cả trang xem lịch sử thông báo).
- Lịch nhắc tự động (job chạy định kỳ gọi nhắc lịch).
- Đổi công thức giá, quyền hưởng hay bất kỳ endpoint Strategy nào.
- Tạo Admin qua API.
- Migration nâng cấp MySQL có kiểm thử như Strategy; chỉ thêm bảng vào `schema.sql`.
- Bộ tài liệu 6 file như Strategy cho các mẫu mới.
- Phân công theo người.

## Further Notes

- Hạn nộp 12/11/2026: khóa code chiều 11/11, sáng 12/11 chỉ còn sơ đồ, tài liệu và tập nói.
- Nhiều entity đã có builder viết tay (Bệnh nhân, Hồ sơ bệnh án); mẫu Builder được đặt ở bản tin thông báo vì đó là chỗ duy nhất còn constructor nhiều tham số.
- Ranh giới Observer/Decorator: Observer quyết định "khi nào và ai phản ứng" với đặt/hủy lịch; Decorator quyết định "gửi thông báo thì thêm hành vi gì". Khi thuyết trình cần nói rõ để không bị cho là trùng nhau.
- Remote còn các nhánh `minhquy`, `yenkhoa` của thành viên khác; spec này không gộp hay sửa các nhánh đó.
