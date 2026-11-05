# Spec: Strategy tính phí khám với quyền hưởng đã xác minh

Status: ready-for-agent

## Problem Statement

Người thực hiện phụ trách mẫu Strategy Pattern trong đồ án đặt lịch khám bệnh. Cần chứng minh việc thay thế thuật toán tính phí bằng chức năng chạy được, kiểm thử, thuyết minh và UML draw.io đen trắng đúng với code. UML chỉ dành cho chức năng trực tiếp áp dụng Strategy.

Đặc tả này được rà lại từ repository MinhQuy06/CK_MTKPM, branch thaihoa, bản gốc đã commit 2be71ae. Phần code báo giá và các tài liệu chưa commit vừa được chuyển vào không được tính là chức năng có sẵn của bản gốc.

Hiện trạng đã kiểm chứng:

- Đặt lịch lấy phí niêm yết của bác sĩ và lưu trực tiếp vào giá lịch hẹn. Request chỉ có ca khám và triệu chứng; chưa có báo giá hoặc lựa chọn loại tính phí.
- Thẻ/danh sách bác sĩ hiển thị phí niêm yết; modal đặt lịch chưa có phần chọn chính sách giá và báo giá từ server.
- Chưa có Strategy tính phí, dữ liệu quyền hưởng Bảo hiểm/VIP, yêu cầu xác minh hoặc quy trình duyệt quyền hưởng.
- Giá lịch hẹn đã là dữ liệu lưu riêng. Giá bác sĩ và giá lịch hẹn dùng BigDecimal với giới hạn DECIMAL(12,2), hiện có thể thiếu giá trị; chưa có quy tắc tiền đầy đủ cho Strategy.
- Giao dịch đặt lịch đã khóa ca bằng pessimistic lock, kiểm tra ca trống/tương lai, bác sĩ được duyệt/hoạt động, lưu lịch và vô hiệu hóa ca.
- Thông báo đã đi qua sự kiện, listener bất đồng bộ AFTER_COMMIT và RabbitMQ. Đây là cơ chế phải giữ và kiểm thử, không phải một tính năng mới chưa có.
- Danh mục dịch vụ có giá riêng nhưng chưa tham gia luồng đặt lịch. Nguồn giá của phạm vi này là phí bác sĩ.
- Frontend có bản riêng và bản static được backend phục vụ; cần cập nhật đồng bộ phần thay đổi. Backend dùng Java 21, Spring Boot 3.3.2.
- Kiểm thử hiện có gồm HTTP/security với MockMvc và các test service về ownership/hủy lịch; chưa có bộ kiểm thử hoàn chỉnh cho báo giá, tính phí khi tạo lịch, tranh chấp đặt cùng ca hoặc commit/rollback của tính phí.

Tài liệu Word là nguồn góp ý. Những công thức ví dụ, định dạng UML hoặc đề xuất pattern trong đó không tự trở thành yêu cầu; các quyết định người dùng chốt dưới đây là căn cứ thực hiện.

## Solution

Triển khai ba thuật toán tính phí chung một giao diện Strategy: Tiêu chuẩn, Bảo hiểm và VIP. Context chọn Strategy theo loại được yêu cầu cho từng request; mỗi lịch dùng một Strategy, không cộng dồn.

| Strategy | Người bệnh trả mặc định | Giảm mặc định | Phí gốc 300.000đ |
|---|---:|---:|---:|
| STANDARD — Tiêu chuẩn | 100% | 0% | 300.000đ |
| INSURANCE — Bảo hiểm | 20% | 80% | 60.000đ |
| VIP | 90% | 10% | 270.000đ |

Tỷ lệ cấu hình được, dùng BigDecimal và HALF_UP đến đồng. Đây là chính sách giá nội bộ của phòng khám dành cho người được duyệt, không phải cam kết về khoản cơ quan bảo hiểm hoàn trả.

Khác với spec trước, Bảo hiểm/VIP không giới hạn demo/test: hệ thống phải kiểm tra quyền hưởng thực tế đã được Admin xác minh nội bộ. Bệnh nhân gửi yêu cầu với mã hồ sơ và thời hạn đề nghị; Admin đối chiếu hồ sơ ngoài hệ thống để duyệt/từ chối, có lý do và lịch sử. Admin có thể thu hồi; gia hạn bằng yêu cầu mới. Không tích hợp cơ quan bảo hiểm bên ngoài và không tải ảnh hồ sơ trong phạm vi này.

Quyền hưởng phải đã được duyệt, chưa bị thu hồi và có hiệu lực cả lúc đặt lẫn vào ngày khám đã chọn theo múi giờ Việt Nam. Server kiểm tra khi báo giá và kiểm tra lại khi đặt; không tin loại quyền, trạng thái duyệt hay mức tiền bệnh nhân tự gửi.

Hai chức năng trực tiếp dùng Strategy:

1. Bệnh nhân chọn ca và một loại phí đủ điều kiện, xem báo giá được server tính. Xem giá chưa giữ chỗ.
2. Bệnh nhân xác nhận đặt lịch; server kiểm tra ca, quyền hưởng và tính lại giá trước khi lưu. Giá thay đổi phải được người dùng xác nhận lại; giữ triệu chứng khi phục hồi. Lịch đã xác nhận giữ giá đã chốt, kể cả khi phí hoặc quyền hưởng thay đổi về sau.

Bàn giao UML đúng **4 file draw.io chỉnh sửa được và 4 PNG**: Class + Sequence cho báo giá, Class + Sequence cho đặt lịch theo Strategy. Nền trắng, chữ/đường/mũi tên đen, không theme màu. Quy trình quản lý quyền hưởng vẫn được triển khai và kiểm thử nhưng **không có bộ UML riêng**. Các sơ đồ Strategy chỉ thể hiện bước kiểm tra quyền hưởng và những lớp/luồng cần để giải thích hai chức năng trên.

## User Stories

1. Là bệnh nhân, tôi muốn thấy Tiêu chuẩn mặc định để tiếp tục đặt khám khi chưa có ưu đãi.
2. Là bệnh nhân, tôi muốn gửi yêu cầu xác minh Bảo hiểm/VIP với mã hồ sơ và thời hạn đề nghị để phòng khám xét quyền hưởng.
3. Là bệnh nhân, tôi muốn xem yêu cầu của mình đang chờ, đã duyệt hay bị từ chối để biết có thể sử dụng chính sách nào.
4. Là bệnh nhân, tôi muốn thấy lý do từ chối hoặc thu hồi phù hợp để hiểu vì sao chưa được áp dụng giá ưu đãi.
5. Là bệnh nhân, tôi muốn gửi yêu cầu mới khi cần gia hạn để giữ được lịch sử xác minh trước đó.
6. Là bệnh nhân, tôi muốn chỉ thấy các loại phí được phép dùng cho ca đang chọn để tránh xác nhận một loại không hợp lệ.
7. Là bệnh nhân, tôi muốn chọn một trong các loại phí đủ điều kiện khi có cả Bảo hiểm và VIP, không cộng dồn hai mức giảm.
8. Là bệnh nhân, tôi muốn giá gốc lấy từ phí niêm yết của bác sĩ để biết nguồn tính tiền.
9. Là bệnh nhân, tôi muốn xem tên loại phí, giá gốc, tỷ lệ giảm, số tiền phải trả và đơn vị VND trước khi xác nhận.
10. Là bệnh nhân, tôi muốn đổi ca hoặc loại phí rồi nhận báo giá mới để không dùng nhầm báo giá cũ.
11. Là bệnh nhân, tôi muốn xem giá mà chưa phát sinh lịch hẹn hoặc giữ chỗ.
12. Là bệnh nhân, tôi muốn được báo rõ khi ca đã qua, bị đặt hoặc bác sĩ không nhận khám để chọn lại.
13. Là bệnh nhân, tôi muốn quyền hưởng được kiểm tra cho ngày khám, không chỉ ngày gửi yêu cầu, để giá báo trước áp dụng đúng ca.
14. Là bệnh nhân, tôi muốn server từ chối loại phí tôi không đủ điều kiện dù trình duyệt bị sửa request.
15. Là bệnh nhân, tôi muốn được yêu cầu chọn lại nếu quyền hết hạn hoặc bị thu hồi trước lúc xác nhận, không bị âm thầm đổi loại giá.
16. Là bệnh nhân, tôi muốn biết giá mới khi phí hoặc tỷ lệ thay đổi sau khi xem báo giá và được tự quyết định xác nhận lại.
17. Là bệnh nhân, tôi muốn giữ triệu chứng và các lựa chọn còn hợp lệ khi xem lại giá.
18. Là bệnh nhân, tôi muốn số tiền client gửi chỉ dùng đối chiếu, không thể quyết định số tiền server lưu.
19. Là bệnh nhân, tôi muốn một xác nhận hợp lệ tạo đúng một lịch hẹn có giá đã chốt.
20. Là bệnh nhân, tôi muốn chi tiết lịch hẹn mới hiển thị loại phí, phí gốc, tỷ lệ và giá chốt để kiểm tra lại.
21. Là bệnh nhân, tôi muốn giá lịch đã xác nhận giữ nguyên khi bác sĩ đổi phí hoặc quyền hưởng bị thu hồi sau đó.
22. Là bệnh nhân, tôi muốn lịch cũ vẫn đọc được mà không bị gán loại ưu đãi hoặc quyền hưởng chưa từng được ghi nhận.
23. Là bệnh nhân, tôi muốn email xác nhận lấy giá lịch hẹn đã lưu để thống nhất với chi tiết lịch.
24. Là bệnh nhân, tôi muốn các khoản tiền được làm tròn nhất quán đến đồng.
25. Là Admin, tôi muốn xem yêu cầu quyền hưởng của đúng bệnh nhân để đối chiếu hồ sơ.
26. Là Admin, tôi muốn duyệt hoặc từ chối yêu cầu với quyết định và lý do được lưu để truy nguyên.
27. Là Admin, tôi muốn chốt thời hạn hiệu lực khi duyệt để hệ thống xét đúng thời điểm.
28. Là Admin, tôi muốn thu hồi quyền đã cấp kèm lý do mà không xóa lịch sử quyết định.
29. Là Admin, tôi muốn biết người duyệt và thời điểm quyết định để tránh quyền lợi được cấp vô danh.
30. Là người vận hành, tôi muốn phí 0 hợp lệ nhưng phí thiếu, âm hoặc vượt giới hạn bị từ chối.
31. Là người vận hành, tôi muốn tỷ lệ ngoài miền hợp lệ bị phát hiện thay vì sinh giá sai.
32. Là lập trình viên, tôi muốn từng công thức nằm trong Concrete Strategy riêng và Context phụ thuộc giao diện.
33. Là lập trình viên, tôi muốn có thể đăng ký Strategy mới mà không sửa công thức điều phối.
34. Là lập trình viên, tôi muốn lựa chọn Strategy không bị chia sẻ nhầm giữa các request đồng thời.
35. Là lập trình viên, tôi muốn giữ khóa chống trùng, ownership, giao dịch và hàng đợi email đang có khi thêm tính phí.
36. Là người kiểm thử, tôi muốn kiểm tra chủ yếu qua API và bổ sung trình duyệt để thấy đầy đủ hành vi người dùng.
37. Là người thực hiện đồ án, tôi muốn bốn UML thể hiện Strategy thật, đúng chữ ký và luồng code cuối.
38. Là người thực hiện đồ án, tôi muốn sơ đồ chỉ bao phủ báo giá/đặt lịch dùng Strategy, không bị mở rộng thành UML toàn hệ thống hoặc quản lý quyền hưởng riêng.
39. Là người thực hiện đồ án, tôi muốn draw.io chỉnh sửa được và ảnh PNG đen trắng, rõ chữ, không cắt nội dung.
40. Là người thực hiện đồ án, tôi muốn thuyết minh bài toán, vai trò Strategy, lợi ích/hạn chế, SOLID và test case để bảo vệ phần mình làm.

## Implementation Decisions

### Quyết định đã chốt với người dùng

- Làm tại checkout người dùng chỉ định của MinhQuy06/CK_MTKPM, branch thaihoa. Người dùng tự mở PR; không tự push, merge hoặc deploy.
- Strategy tính phí cho hai chức năng báo giá và đặt lịch; ba loại và mức mặc định như bảng trong Solution; mỗi lịch một loại.
- Phí gốc từ bác sĩ; tỷ lệ cấu hình được; BigDecimal, HALF_UP đến đồng; 0 hợp lệ, thiếu/âm/vượt giới hạn bị từ chối.
- Bệnh nhân gửi yêu cầu quyền hưởng, Admin xác minh ngoài hệ thống rồi duyệt/từ chối; có lý do, người duyệt, thời hạn và lịch sử; có thu hồi, gia hạn bằng yêu cầu mới.
- Bệnh nhân không thể tự cấp quyền. Server kiểm tra quyền hưởng khi báo giá/đặt lịch; quyền phải hợp lệ lúc đặt và vào ngày khám theo múi giờ Việt Nam.
- Trả danh sách loại phí bệnh nhân đủ điều kiện. Yêu cầu loại không hợp lệ bị từ chối, không tự áp ưu đãi hoặc đổi về Tiêu chuẩn.
- Chính sách Bảo hiểm/VIP áp dụng khi đủ điều kiện trong môi trường vận hành; bỏ quy tắc chỉ demo/test của spec cũ. Đây là chính sách giá phòng khám, chưa tích hợp chi trả bảo hiểm bên ngoài.
- Giá thay đổi cần xác nhận lại; giữ triệu chứng. Lưu snapshot giá gốc, loại Strategy, tỷ lệ và giá cuối; được bổ sung dữ liệu cần thiết và cập nhật schema, giữ dữ liệu lịch cũ.
- Giá lịch đã xác nhận không thay đổi khi phí, cấu hình hoặc quyền hưởng thay đổi sau đó.
- Quản lý quyền hưởng là phần hỗ trợ phải xây dựng; UML riêng chỉ cho hai chức năng dùng Strategy, tổng 4 draw.io + 4 PNG đen trắng, theo chỉ dẫn cuối cùng thay cho phương án 6 sơ đồ đã trao đổi.
- Kiểm thử chính qua API, thêm luồng trình duyệt, đối chiếu UML với code và vai trò Strategy.

### Quy ước kỹ thuật đề xuất để hiện thực

- Giữ kiến trúc phân tầng; controller nhận/validate và gọi service, không chứa công thức hay nghiệp vụ duyệt quyền.
- Context/registry nhận danh sách Strategy qua dependency injection, chọn theo khóa trên từng lần tính. Không dùng setter lưu Strategy đang chọn trong singleton; không hardcode công thức từng loại trong service đặt lịch.
- Tách kiểm tra quyền hưởng khỏi công thức tiền: dữ liệu quyền hưởng được service tin cậy xác minh trước khi Context gọi Strategy. Khóa Strategy không đồng nghĩa với quyền được dùng.
- Dữ liệu yêu cầu có bệnh nhân, loại quyền, mã tham chiếu, thời hạn đề nghị và trạng thái. Quyền đã cấp có thời hạn Admin xác nhận và tham chiếu quyết định. Lưu người/thời điểm/lý do quyết định và thu hồi; không ghi đè lịch sử để gia hạn.
- Các trạng thái nghiệp vụ đề xuất: chờ duyệt, đã duyệt, từ chối, đã thu hồi; hết hạn được suy ra từ ngày hiệu lực. Thao tác chuyển trạng thái phải nguyên tử, tránh hai Admin đưa ra quyết định mâu thuẫn cho cùng yêu cầu.
- Quy ước ngày hiệu lực bao gồm cả ngày bắt đầu và kết thúc, diễn giải tại Asia/Ho_Chi_Minh. Kiểm tra quyền cho cả ngày hiện tại và ngày khám. Không cần tự động sửa giá của lịch đã đặt khi quyền hết hạn.
- Bệnh nhân chỉ gửi/đọc yêu cầu của mình; Admin mới duyệt/từ chối/thu hồi. Danh tính người bệnh và người duyệt lấy từ principal, không nhận quyền hay người duyệt do client khai. Không trả mã hồ sơ của bệnh nhân khác.
- Nếu có nhiều hồ sơ, eligibility cần trả một quyền hợp lệ xác định cho loại/ngày được chọn; lưu tham chiếu quyền dùng lúc đặt để truy nguyên. Nếu có cả Bảo hiểm và VIP, người dùng chọn một, không tự chọn mức có lợi nhất.
- Báo giá nhận ca và loại phí, gắn với người bệnh đã xác thực; trả loại/tên Strategy, giá gốc, tỷ lệ, giá cuối, VND, các loại đủ điều kiện và thông tin cần xác nhận. Đây là dữ liệu đọc, không giữ chỗ hoặc gửi email.
- Chuẩn hóa phí gốc đến đồng bằng HALF_UP, sau đó áp dụng tỷ lệ và HALF_UP giá cuối đến đồng. Tỷ lệ giảm dùng số thập phân trong [0,1]. Kiểm tra giới hạn DECIMAL(12,2) cả trước và sau làm tròn; không dùng số thực nhị phân.
- Client mới gửi loại phí và thông tin báo giá đã xem để đối chiếu. Server đọc phí/quyền hiện tại và tính lại; không dùng giá client gửi làm nguồn tiền. Lỗi quyền hưởng, giá thay đổi và mất ca có mã phân biệt để giao diện phục hồi đúng.
- Với client cũ chưa gửi loại phí, mặc định Tiêu chuẩn theo hợp đồng tương thích được ghi rõ; loại đã gửi nhưng không hỗ trợ hoặc rỗng phải bị từ chối. Không cho bỏ thông tin xác nhận để vượt kiểm tra giá của luồng mới.
- Trong giao dịch đặt lịch, giữ khóa ca hiện có, kiểm tra lại quyền và giá trước khi lưu, bảo đảm thu hồi quyền đồng thời không chen giữa lần xác minh cuối và việc chốt quyền/giá theo cách làm sai điều kiện đã cam kết. Giải pháp khóa/version cụ thể được chọn ở kế hoạch thực hiện và kiểm thử.
- Snapshot mới bổ sung theo cách tương thích; lịch cũ chỉ hiển thị dữ liệu thật đã có, không giả định mọi lịch cũ là Bảo hiểm/VIP hay có quyết định duyệt. Cập nhật tài liệu dữ liệu và có cách nâng cấp database hiện hữu; không chỉ dựa vào tạo bảng mới trong test.
- Giữ cơ chế after-commit + hàng đợi email đang có và nội dung lấy từ giá đã lưu. Không mở rộng sang xây outbox hoặc thay hệ thống thông báo trong hạng mục này.
- Giao diện có trạng thái tải/lỗi/thử lại; loại bỏ response của ca/loại cũ khi đổi lựa chọn hoặc đóng modal. Sau đổi giá, chỉ đặt khi người dùng chủ động xác nhận lại.
- Dùng origin backend đúng với port dự án khi phục vụ static; giữ hỗ trợ API base cấu hình cho frontend riêng. Dùng port trống khác 8080 để không đụng dự án khác; không áp trạng thái từ checkout tạm thành yêu cầu hạ tầng mới.
- Hai Class Diagram thể hiện interface, Concrete Strategy, Context và các lớp cộng tác cần thiết. Hai Sequence Diagram thể hiện chọn thuật toán, kiểm tra quyền, tính giá, xác nhận lại và lưu lịch/nhánh lỗi liên quan; không vẽ quy trình Admin thành bộ sơ đồ riêng.

## Testing Decisions

- Điểm kiểm thử chính được giữ từ quyết định đã đồng ý: API cho yêu cầu/duyệt quyền hưởng, danh sách loại phí đủ điều kiện, báo giá và đặt lịch. Bổ sung trình duyệt cho luồng người dùng. Kiểm tra HTTP/payload, dữ liệu lưu, snapshot và tác động quan sát được; không ràng buộc test vào số lần gọi private method.
- Tận dụng mẫu SpringBootTest + MockMvc + profile H2 của kiểm thử HTTP/security hiện có. Các test ownership/hủy lịch hiện có là kiểm thử hồi quy, không được mô tả như bằng chứng đã có cho tạo lịch/giá/tranh chấp.
- Quyền hưởng: người bệnh chỉ đọc/gửi yêu cầu của mình; không thể tự duyệt, sửa trạng thái hoặc mạo danh Admin; Admin duyệt/từ chối/thu hồi có lịch sử; gia hạn không sửa mất quyết định cũ; kiểm tra thao tác quyết định đồng thời.
- Eligibility: chờ duyệt, từ chối, thu hồi, chưa đến ngày và hết hạn đều không đủ điều kiện. Kiểm tra đúng ngày bắt đầu/kết thúc, múi giờ Việt Nam, quyền có hiệu lực hôm nay nhưng hết hạn trước ngày khám, và ngày khám hợp lệ nhưng quyền chưa có hiệu lực lúc đặt.
- Bảo hiểm/VIP khi đủ điều kiện phải hoạt động ở môi trường vận hành; không giữ test cũ kỳ vọng chặn toàn bộ hai Strategy ngoài demo/test. Không đủ điều kiện phải bị server chặn kể cả sửa request; không âm thầm đổi giá.
- Tiền: 300.000đ cho kết quả 300.000đ/60.000đ/270.000đ; thêm HALF_UP, phí 0, null, âm nhỏ, mức trần sau làm tròn, tỷ lệ thiếu/sai miền và khóa không hỗ trợ.
- Có kiểm thử tập trung ba công thức và chứng minh Context thực thi một Strategy mới đăng ký mà không sửa thuật toán điều phối; kiểm tra các request khác loại không dùng nhầm lựa chọn của nhau.
- Báo giá không tạo lịch, không khóa/giữ ca, không sửa trạng thái, không phát sự kiện xác nhận. Cùng ca có thể xem lại giá nhiều lần trước khi người khác đặt.
- Đặt lịch: tiền giả mạo không được lưu; thay phí/tỷ lệ giữa báo giá và xác nhận yêu cầu xác nhận lại; quyền bị thu hồi/hết hạn trước lúc đặt bị từ chối dù số tiền có thể chưa đổi. Không có dữ liệu dở dang hoặc xác nhận email khi thất bại.
- Snapshot: sửa phí, tỷ lệ hoặc thu hồi quyền sau đặt không đổi giá lịch đã chốt; chi tiết/lịch sử/email dùng giá lưu; lịch cũ không bị chế thêm metadata. Có kiểm tra nâng cấp dữ liệu tồn tại.
- Giao dịch: hai người tranh cùng ca chỉ một lịch thành công; kiểm tra thu hồi quyền đồng thời với xác nhận; rollback không gửi email, commit hợp lệ có thông báo với snapshot đúng. Test mặc định rollback không đủ chứng minh AFTER_COMMIT; cần thực sự đi qua commit trong kiểm thử phù hợp.
- H2 dùng cho vòng kiểm thử nhanh. Bổ sung kiểm tra trên MySQL ở môi trường test cho khóa/tranh chấp; không coi kết quả H2 là bằng chứng đầy đủ cho hành vi khóa MySQL.
- Trình duyệt: bệnh nhân gửi yêu cầu → Admin duyệt → bệnh nhân thấy loại hợp lệ → báo giá → đặt → đọc giá lưu; thêm từ chối/thu hồi, giá đổi và giữ triệu chứng, response cũ về muộn, client gọi đúng backend trên port thay thế.
- UML: mở/chỉnh sửa được 4 draw.io, đối chiếu vai trò và chữ ký/luồng code cuối; kiểm tra 4 PNG trắng/đen, không cắt chữ. Parse XML thành công không thay cho kiểm tra hình và tính đúng của sơ đồ. Xuất cục bộ, không gửi nội dung sơ đồ ra dịch vụ ngoài.

## Out of Scope

- UML quản lý quyền hưởng riêng, UML toàn hệ thống hoặc pattern khác. Quy trình quyền hưởng vẫn nằm trong phạm vi triển khai và kiểm thử.
- Strategy sắp xếp bác sĩ, Strategy hủy lịch/hoàn tiền, phụ thu ngoài giờ/cuối tuần và cộng dồn ưu đãi.
- Cổng thanh toán, ghi nhận tiền đã thực thu, quyết toán hoặc cam kết bồi hoàn từ đơn vị bảo hiểm.
- Kết nối cơ quan bảo hiểm/đơn vị VIP bên ngoài, quét thẻ hoặc tải/lưu ảnh hồ sơ.
- Thay đổi nghiệp vụ của thành viên khác ngoài tích hợp cần thiết; nâng cấp framework, thiết kế lại toàn bộ giao diện, thay hàng đợi hoặc bổ sung outbox.
- Tự push, mở PR, merge hoặc triển khai production.

## Further Notes

- Đây là bản đặc tả viết lại sau khi đọc đúng checkout người dùng chỉ định và grilling lại. Nó thay thế giả định Bảo hiểm/VIP chỉ là demo trong bản trước.
- Chỉ dẫn cuối của người dùng giới hạn sơ đồ vào tính năng Strategy trực tiếp; vì vậy phương án 6 draw.io/6 PNG bị thay bằng 4 draw.io/4 PNG. Không có suy diễn rằng phải bỏ chức năng quản lý quyền hưởng.
- Các quyết định nghiệp vụ đã được người dùng trả lời; các quy ước kỹ thuật đề xuất ở trên là cách hiện thực và phải được kiểm chứng khi triển khai, không gán thành phát biểu nguyên văn của người dùng.
- Phần ticket #2 đang có trong working tree là công việc chưa hoàn tất, chưa được coi là đáp ứng spec mới. Giữ để xem xét lại; không tự xóa, push hoặc đóng ticket vì các test cũ từng qua.
- Sáu ticket cũ cần được rà và chia lại theo spec này trước khi tiếp tục: các ticket Bảo hiểm/VIP cũ yêu cầu demo-only, chưa có luồng yêu cầu/duyệt quyền, nên không thể dùng nguyên tiêu chí nghiệm thu cũ.
- Tài liệu tracker local và GitHub hiện cùng tồn tại theo yêu cầu trước đó. Lưu bản này tại tracker Markdown của đúng checkout và đồng bộ spec GitHub #1 trong MinhQuy06/CK_MTKPM; không tạo thêm spec trùng và không tự thay cấu hình tracker.

## Execution authorization (2026-11-05)

User instructed sequential execution of all approved tickets, with a commit and push to thaihoa after each completed ticket, and no PR. This supersedes the earlier no-push instruction.
