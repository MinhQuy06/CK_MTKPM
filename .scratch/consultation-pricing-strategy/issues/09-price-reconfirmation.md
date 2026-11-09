# 09: Xác nhận lại khi giá thay đổi, giữ triệu chứng

Status: completed

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/16

**What to build:** Khi phí bác sĩ hoặc tỷ lệ thay đổi giữa báo giá và xác nhận, bệnh nhân được xem giá mới, giữ triệu chứng và chủ động xác nhận lại trước khi tạo lịch.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 08: Đặt lịch Bảo hiểm và VIP có kiểm tra lại quyền hưởng

- [x] API báo lỗi thay đổi giá có mã phân biệt với mất ca/mất quyền; lần xác nhận cũ không lưu appointment, đổi availability hoặc phát email.
- [x] Giao diện lấy báo giá mới cho đúng ca/loại, thể hiện số tiền mới và giữ triệu chứng cùng các lựa chọn còn hợp lệ.
- [x] Không tự gửi lại đặt lịch hoặc tự chấp nhận giá mới; chỉ tạo lịch sau thao tác xác nhận rõ ràng của người dùng.
- [x] Nếu giá tiếp tục thay đổi, yêu cầu xác nhận lại tiếp; response trễ không thay giá/lựa chọn mới, không tạo vòng lặp đặt lịch tự động.
- [x] Kiểm thử API và trình duyệt thật cho phí/tỷ lệ đổi, không tạo dữ liệu dở dang, giữ triệu chứng và đọc lại đúng snapshot sau xác nhận.
- [x] Cập nhật nhánh thay đổi giá trong Sequence đặt lịch và PNG; hoàn thiện thuyết minh Strategy, SOLID, lợi ích/hạn chế và ví dụ ba mức giá theo phần đã chạy.
- [x] Không thêm sơ đồ riêng cho quản lý quyền hưởng; giữ tổng 4 draw.io + 4 PNG đen trắng và đối chiếu chữ ký/hành vi code.

Evidence: 111 H2/MySQL tests (0 failures/errors/skips), 10 Node tests; real Chrome repeats two fee changes then explicit final confirmation. Cached old-policy quote fixture is rejected by the real server, refreshed to current 80% discount and explicitly confirmed. UML exported offline and visually inspected.
