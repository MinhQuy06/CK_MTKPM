# Spec: Tổ chức mã nguồn và tài liệu tính năng Strategy

Status: ready-for-agent

## Problem Statement

Người thực hiện phụ trách phần Strategy tính phí khám trong đồ án đặt lịch khám bệnh. Mã nguồn tính phí, kiểm tra quyền hưởng, tài liệu, UML và công cụ kiểm thử trước đây nằm rải rác theo các tầng chung của dự án, khiến khó tìm đủ phần mình phụ trách để đọc, trình bày, bàn giao và bảo trì.

Người dùng yêu cầu gom cả mã nguồn và tài liệu Strategy thành một mục dễ tìm. Việc tổ chức lại cần giữ ứng dụng chạy đúng, giữ các điểm tích hợp với đặt lịch và tránh biến phần Strategy thành một bản sao của toàn bộ hệ thống.

## Solution

Có một mục lục Strategy chung để đi đến toàn bộ mã nguồn, báo cáo, sơ đồ, kiểm thử và công cụ liên quan. Trong mỗi phần backend, frontend, dữ liệu và kiểm thử, các thành phần riêng của Strategy được gom thành nhóm rõ ràng, tương thích với cấu trúc build hiện có.

Phần backend bao gồm các thuật toán tính phí, Context, báo giá và quản lý quyền hưởng cần thiết cho Bảo hiểm/VIP. Các lớp và trang dùng chung vẫn là điểm tích hợp với nhóm Strategy. Người bệnh và Admin tiếp tục sử dụng các URL, hợp đồng API, phân quyền và hành vi đã có.

Tài liệu và đúng bốn bộ UML đen trắng nằm cùng khu vực Strategy, đi kèm hướng dẫn chạy được và liên kết đúng sau khi chuyển vị trí.

## User Stories

1. Là người thực hiện phần Strategy, tôi muốn mở một mục lục chung để tìm toàn bộ phần mình phụ trách mà không phải nhớ từng vị trí trong dự án.
2. Là người thực hiện phần Strategy, tôi muốn mã nguồn và tài liệu đều được gom nhóm để việc bàn giao không chỉ gồm báo cáo hoặc chỉ gồm code.
3. Là lập trình viên backend, tôi muốn các lớp riêng của Strategy nằm trong cùng mô-đun để nhận biết ranh giới tính năng.
4. Là lập trình viên backend, tôi muốn thấy giao diện Strategy và các Concrete Strategy ở cùng nhóm để so sánh các thuật toán tính phí.
5. Là lập trình viên backend, tôi muốn Context và kết quả tính phí ở cạnh các thuật toán để đọc được cách lựa chọn và thực thi chính sách giá.
6. Là lập trình viên backend, tôi muốn API báo giá nằm trong mô-đun Strategy để lần theo luồng từ request đến phép tính.
7. Là lập trình viên backend, tôi muốn nghiệp vụ kiểm tra quyền hưởng nằm trong nhóm hỗ trợ Strategy để hiểu điều kiện sử dụng Bảo hiểm/VIP.
8. Là lập trình viên backend, tôi muốn các controller bệnh nhân và Admin liên quan quyền hưởng được nhóm rõ ràng để không nhầm với các API quản trị khác.
9. Là lập trình viên backend, tôi muốn entity, repository và DTO riêng của quyền hưởng được tổ chức cùng tính năng để dễ bảo trì dữ liệu và hợp đồng trao đổi.
10. Là lập trình viên tích hợp, tôi muốn các lớp đặt lịch và lịch hẹn dùng chung giữ vai trò hiện tại để không tạo hai luồng đặt lịch trùng nhau.
11. Là lập trình viên tích hợp, tôi muốn import và tham chiếu kiểu được cập nhật đầy đủ để bản build sạch không phụ thuộc class cũ còn sót.
12. Là lập trình viên frontend, tôi muốn các script báo giá và quyền hưởng có nhóm Strategy riêng để dễ tìm và chỉnh sửa.
13. Là lập trình viên frontend, tôi muốn bản frontend và bản static backend được đồng bộ để cách phục vụ ứng dụng không làm thay đổi hành vi.
14. Là bệnh nhân, tôi muốn liên kết đến trang báo giá và quyền hưởng vẫn sử dụng được để không phải học lại cách truy cập sau khi tổ chức mã nguồn.
15. Là Admin, tôi muốn trang duyệt và thu hồi quyền hưởng giữ URL và quyền truy cập để tiếp tục xử lý hồ sơ như trước.
16. Là bệnh nhân, tôi muốn báo giá và giá chốt không thay đổi do việc chuyển thư mục để các chính sách Tiêu chuẩn, Bảo hiểm và VIP vẫn đúng.
17. Là bệnh nhân, tôi muốn các nhánh đổi giá, mất quyền và mất ca vẫn phục hồi đúng để không xác nhận nhầm giá hoặc ngày giờ khám.
18. Là người bảo trì, tôi muốn ứng dụng phát hiện đầy đủ component, entity và repository sau khi đổi package để không thiếu bean hoặc ánh xạ dữ liệu lúc khởi động.
19. Là người bảo trì dữ liệu, tôi muốn các SQL nâng cấp được gom theo tính năng nhưng giữ nguyên nội dung và thứ tự để không thay đổi ý nghĩa nâng cấp.
20. Là người bảo trì dữ liệu, tôi muốn các lệnh kiểm tra nâng cấp tìm đúng vị trí mới để vẫn chứng minh dữ liệu lịch sử được bảo toàn.
21. Là người kiểm thử, tôi muốn các test Java của Strategy nằm cùng nhóm để xác định phạm vi hồi quy.
22. Là người kiểm thử, tôi muốn các test Node và trình duyệt được gom theo Strategy để chạy và đọc bằng chứng thuận tiện.
23. Là người kiểm thử, tôi muốn giữ các điểm kiểm thử API và trình duyệt hiện có để đánh giá hành vi bên ngoài thay vì số lần gọi phương thức nội bộ.
24. Là người kiểm thử, tôi muốn fixture trình duyệt khởi động được sau khi đổi tên package để các luồng Chrome không phụ thuộc vị trí cũ.
25. Là người thực hiện đồ án, tôi muốn báo cáo, thuyết minh và hình kiểm thử nằm cùng khu vực để chuẩn bị phần trình bày đầy đủ.
26. Là người thực hiện đồ án, tôi muốn bốn UML draw.io và PNG được gom cùng tài liệu Strategy để không lẫn với sơ đồ của thành viên khác.
27. Là người thực hiện đồ án, tôi muốn UML tiếp tục chỉ mô tả báo giá và đặt lịch dùng Strategy để không mở rộng sang sơ đồ Admin hoặc toàn hệ thống.
28. Là người chỉnh sửa UML, tôi muốn các lệnh sinh và xuất ảnh tìm đúng vị trí mới để tiếp tục chỉnh sửa và xuất sơ đồ cục bộ.
29. Là người đọc tài liệu, tôi muốn mọi liên kết trong mục lục và báo cáo trỏ đến tài nguyên tồn tại để không gặp đường dẫn hỏng.
30. Là người chạy dự án, tôi muốn hướng dẫn và lệnh kiểm thử được cập nhật để không dùng nhầm lệnh theo cấu trúc cũ.
31. Là người review, tôi muốn phân biệt việc chuyển vị trí với thay đổi nghiệp vụ để đánh giá phạm vi refactor chính xác.
32. Là người review, tôi muốn thấy bằng chứng build sạch và hồi quy sau khi chuyển để không chấp nhận kết quả từ tài nguyên biên dịch cũ.
33. Là chủ dự án, tôi muốn thay đổi được giữ trên branch thaihoa để tự quyết định thời điểm mở pull request.
34. Là chủ dự án, tôi muốn spec tổ chức mã nguồn tách khỏi spec nghiệp vụ đã hoàn tất để không vô tình mở lại các ticket cũ.

## Implementation Decisions

- Phạm vi người dùng đã xác nhận là gom cả mã nguồn và tài liệu Strategy, không chỉ báo cáo và sơ đồ.
- Giữ cấu trúc dự án Spring Boot và frontend hiện có; tạo nhóm Strategy theo từng phần thay vì chuyển toàn bộ dự án sang một hệ thống build hoặc module triển khai mới.
- Backend nhóm các thành phần riêng gồm pricing, controller, service, entity, repository và DTO. Nhóm pricing chứa giao diện Strategy, Context, ba Concrete Strategy và các kiểu kết quả/lựa chọn tính phí.
- Báo giá và quy trình yêu cầu, duyệt, từ chối, thu hồi, gia hạn quyền hưởng là các thành phần liên quan trong nhóm; điều kiện hưởng vẫn tách khỏi công thức tính tiền.
- Lớp đặt lịch, entity lịch hẹn và DTO dùng chung giữ vị trí và trách nhiệm hiện tại. Chúng tham chiếu đến các thành phần Strategy qua import đã cập nhật; không sao chép lớp dùng chung vào mô-đun mới.
- Cập nhật package, import, tham chiếu đầy đủ và tên lớp khởi động fixture. Component scan, entity scan và repository discovery của Spring phải tiếp tục hoạt động trong phạm vi ứng dụng hiện tại.
- JavaScript riêng của báo giá và quyền hưởng được gom nhóm; các trang tích hợp, lớp gọi API chung, xác thực và tài nguyên dùng chung giữ vai trò hiện có. Các trang tham chiếu đến tài nguyên ở vị trí mới.
- Đồng bộ các thay đổi frontend với bản static do backend phục vụ. Giữ URL trang, endpoint API, payload nghiệp vụ, phân quyền, ownership và mã lỗi hiện có.
- SQL nâng cấp được gom dưới nhóm Strategy của cây nâng cấp dữ liệu. Giữ nguyên byte nội dung và thứ tự năm bước; không bổ sung hoặc chạy một migration nghiệp vụ mới chỉ vì đổi thư mục.
- Gom các test Java, Node và Chrome riêng của tính năng. Cập nhật lệnh chạy, cơ chế tìm test, vị trí fixture và nơi ghi ảnh bằng chứng khi cần.
- Gom công cụ sinh/xuất UML, fixture và kiểm tra trình duyệt/MySQL theo nhóm Strategy. Công cụ phải xác định đúng thư mục gốc dự án sau khi chuyển và chạy được từ hướng dẫn đã cập nhật.
- Có mục lục Strategy chung, liên kết từ tài liệu tổng của dự án. Mục lục phân biệt mã nguồn riêng, các điểm tích hợp dùng chung, tài liệu, UML, kiểm thử, SQL nâng cấp và công cụ.
- Giữ đúng bốn file draw.io chỉnh sửa được và bốn PNG đen trắng: Class và Sequence cho báo giá, Class và Sequence cho đặt lịch dùng Strategy. Chuyển vị trí không làm thay đổi nội dung sơ đồ; xuất ảnh cục bộ.
- Giữ công thức, quy tắc tiền, quyền hưởng, snapshot giá, khóa giao dịch, cơ chế AFTER_COMMIT và hàng đợi email đã được chốt trong spec nghiệp vụ.
- Làm trong repository MinhQuy06/CK_MTKPM, branch thaihoa, checkout người dùng đã chỉ định. Giữ quy trình commit/push được người dùng yêu cầu; người dùng tự mở PR. Không merge hoặc deploy.
- Tracker dự án là Markdown cục bộ. Đặc tả tổ chức này là một hạng mục riêng; không sửa trạng thái hoặc đóng lại spec cha trên GitHub, không mở lại mười ticket nghiệp vụ đã hoàn tất.

## Testing Decisions

- Điểm kiểm thử chính tiếp tục là API HTTP đã được người dùng đồng ý; thêm các luồng trình duyệt thực tế. Việc tổ chức thư mục không cần một điểm kiểm thử nghiệp vụ mới.
- Dùng lại các test Spring Boot/MockMvc/JWT với H2, các contract MySQL, test Node xử lý trạng thái bất đồng bộ và Playwright/Chrome của tính năng. Không thay bằng kiểm tra số lần gọi phương thức nội bộ hoặc test phản chiếu nguyên cấu trúc thư mục.
- Build sạch phải biên dịch toàn bộ main/test code, khởi động context và chạy các API báo giá, quyền hưởng, xác nhận đặt lịch. Kết quả không được dựa vào class hoặc static resource cũ còn trong đầu ra build.
- API hồi quy kiểm tra ba chính sách giá, quyền và ownership, snapshot, tranh ca, cập nhật phí hoặc thu hồi đồng thời, xác nhận lại giá và phát thông báo theo giao dịch.
- MySQL thật tiếp tục xác minh hành vi khóa và nâng cấp dữ liệu; không coi kết quả H2 là bằng chứng thay thế cho MySQL. Dùng database tổng hợp kiểm thử, không dùng dữ liệu vận hành.
- Node kiểm tra lựa chọn báo giá hiện tại, response trễ, lỗi và mã phục hồi; cơ chế tìm test phải tiếp tục chạy đủ các test sau khi chuyển.
- Chrome kiểm tra truy cập trang và tải tài nguyên qua vị trí mới, yêu cầu quyền đến Admin duyệt, báo giá/đặt lịch/đọc snapshot ba loại, và phục hồi khi giá, quyền hoặc ca thay đổi. Mỗi kịch bản dùng fixture tách biệt.
- So sánh nội dung năm SQL nâng cấp trước và sau chuyển; chạy lại kiểm tra nâng cấp để xác nhận giá lịch sử và metadata chưa biết được bảo toàn.
- Kiểm tra liên kết trong mục lục/báo cáo, tính đồng bộ frontend/static và việc chạy các lệnh công cụ theo hướng dẫn mới.
- Đối chiếu bốn draw.io và bốn PNG trước/sau chuyển; kiểm tra công cụ sinh/xuất vẫn giữ số lượng, tính chỉnh sửa được, đen trắng và phạm vi sơ đồ.
- Bằng chứng hiện có của lần tổ chức tại baseline: 118 test H2/MySQL, 11 test Node, 16 kịch bản Chrome và năm bước SQL nâng cấp đã qua. Đây là số liệu quan sát được, không phải điều kiện buộc các bộ test tương lai phải giữ nguyên số lượng.

## Out of Scope

- Thay đổi công thức Standard, Insurance hoặc VIP, điều kiện hưởng, trạng thái hồ sơ hoặc nghiệp vụ xác nhận đặt lịch.
- Tạo một luồng đặt lịch thứ hai, sao chép lớp dùng chung, hoặc chuyển toàn bộ dự án sang kiến trúc hay hệ thống build khác.
- Đổi URL, hợp đồng API, quyền truy cập hoặc giao diện người dùng ngoài cập nhật tham chiếu tài nguyên cần thiết cho việc chuyển vị trí.
- Thay nội dung/schema dữ liệu hoặc tạo migration nghiệp vụ mới; chạy nâng cấp trên database vận hành.
- Thêm UML quản lý quyền hưởng, UML toàn hệ thống, pattern khác, sơ đồ màu hoặc thay đổi giới hạn bốn UML đã thống nhất.
- Thanh toán, tích hợp cơ quan bảo hiểm ngoài, tải ảnh hồ sơ, outbox hoặc thiết kế lại cơ chế thông báo.
- Thực hiện lại mười ticket nghiệp vụ đã hoàn tất, tạo ticket triển khai trùng, sửa/đóng spec cha GitHub hoặc thay cấu hình tracker.
- Tự mở PR, merge hoặc triển khai production.

## Further Notes

- Spec này tổng hợp yêu cầu tổ chức thư mục mới nhất; người dùng đã trả lời rõ rằng cần gom cả mã nguồn và tài liệu. Không có phỏng vấn nghiệp vụ mới.
- Baseline được kiểm chứng là commit d513e65 trên thaihoa; thay đổi tổ chức đã được triển khai, kiểm thử và push trước khi người dùng gọi to-spec. Spec ghi nhận quyết định và tiêu chí duy trì/nghiệm thu, không yêu cầu làm lại phần đã hoàn tất.
- Nhãn ready-for-agent được áp dụng theo skill vì đặc tả đã đủ rõ; nhãn này không tự chuyển công việc đã hoàn tất thành chưa triển khai.
- “Một mục Strategy” được hiện thực bằng một mục lục chung và các nhóm cùng tên trong từng phần của dự án, phù hợp cấu trúc build. Không diễn giải thành gom mọi ngôn ngữ và tài nguyên vào một thư mục vật lý duy nhất bất chấp cách ứng dụng được phục vụ.
- Các trang và lớp dùng chung được giải thích trong mục lục là điểm tích hợp; việc chúng không chuyển vào nhóm riêng không có nghĩa phần Strategy bị thiếu.
- Spec nghiệp vụ vẫn là căn cứ cho hành vi tính phí và quyền hưởng. Đặc tả này chỉ thêm quy ước tổ chức và hồi quy, không thay thế các quyết định nghiệp vụ đó.
- Điểm nhỏ đã ghi nhận từ review nghiệp vụ trước là thiếu test cố ý rollback sau khi đăng ký event email. Việc tổ chức thư mục không tuyên bố đã xử lý hoặc mở rộng điểm đó.
