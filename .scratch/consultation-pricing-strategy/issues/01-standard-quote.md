# 01: Báo giá Tiêu chuẩn qua Strategy

Status: completed

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/8

**What to build:** Bệnh nhân chọn ca và xem báo giá Tiêu chuẩn do server tính, không tạo lịch hoặc giữ chỗ. Rà lại phần code đang làm dở theo spec mới; không coi test hoặc UML cũ là bằng chứng đã hoàn tất.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** None (can start immediately)

- [x] Giao diện chọn ca gọi API có JWT/PATIENT và hiển thị tên loại phí, giá gốc, tỷ lệ giảm, giá cuối và VND. Tiêu chuẩn là mặc định.
- [x] Giao diện Strategy, Concrete Strategy Tiêu chuẩn và Context chọn qua giao diện; có thể đăng ký thuật toán mới mà không sửa công thức điều phối. Không dùng lựa chọn Strategy mutable dùng chung giữa request.
- [x] Giá gốc lấy từ phí bác sĩ. Mặc định 300.000đ → 300.000đ; tỷ lệ cấu hình được trong [0,1]; BigDecimal, HALF_UP phí gốc rồi giá cuối đến đồng; kiểm tra giới hạn DECIMAL(12,2) cả sau làm tròn.
- [x] Phí 0 hợp lệ; phí thiếu/âm/vượt trần và khóa loại không hỗ trợ bị từ chối. Ca quá khứ/đã đặt/bác sĩ không hoạt động hoặc chưa được duyệt không được báo giá.
- [x] Xem báo giá không ghi appointment, giữ/khóa ca, thay availability hoặc phát thông báo. Kiểm thử API xác nhận các tác động không phát sinh và phân quyền.
- [x] Đổi ca/ngày/bác sĩ hoặc đóng modal phải xóa báo giá cũ; response về muộn không ghi đè lựa chọn hiện tại. Có trạng thái tải, lỗi và thử lại.
- [x] Client gọi đúng backend khi chạy port trống khác 8080; giữ API base cấu hình cho frontend riêng. Kiểm thử giao diện và API trên port thay thế.
- [x] Bàn giao cặp Class + Sequence báo giá bằng draw.io chỉnh sửa được và PNG đen trắng, khớp code ở bước này; thuyết minh Strategy/Concrete Strategy/Context và phép tính.

Verification: 51 backend tests, 9 Node tests, real Chrome/H2 smoke passed; UTC regression RED→GREEN; two draw.io files exported locally and PNGs inspected.
