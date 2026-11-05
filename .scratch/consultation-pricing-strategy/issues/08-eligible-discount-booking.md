# 08: Đặt lịch Bảo hiểm và VIP có kiểm tra lại quyền hưởng

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/15

**What to build:** Bệnh nhân đặt lịch theo báo giá Bảo hiểm/VIP; server kiểm tra lại quyền trong giao dịch, tính đúng Strategy và lưu quyền cùng snapshot giá đã chốt.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 04: Thu hồi và gia hạn quyền hưởng, giữ lịch sử, 05: Báo giá Bảo hiểm theo quyền đã được duyệt, 06: Báo giá VIP theo quyền đã được duyệt, 07: Đặt lịch Tiêu chuẩn và lưu snapshot giá

- [ ] Giao diện gửi đúng loại đã chọn và thông tin báo giá; server xét bệnh nhân từ principal, lấy dữ liệu quyền đã duyệt hiện tại và tính lại trước khi lưu.
- [ ] Quyền phải có hiệu lực hiện tại/ngày khám, chưa thu hồi; thiếu quyền hoặc hết hạn bị từ chối dù giá cuối có thể chưa đổi. Không tự chuyển Tiêu chuẩn.
- [ ] Mặc định 300.000đ lưu Bảo hiểm 60.000đ hoặc VIP 270.000đ, một lịch một loại; tuân thủ cấu hình, BigDecimal, HALF_UP và giới hạn tiền.
- [ ] Lưu tham chiếu quyền dùng lúc đặt cùng snapshot; chi tiết/lịch sử/email lấy dữ liệu đã lưu. Thu hồi/hết hạn/đổi tỷ lệ sau xác nhận không đổi giá lịch đó.
- [ ] Giữ khóa ca, giao dịch và thông báo AFTER_COMMIT. Giải pháp khóa/version phải ngăn thu hồi quyền chen giữa lần xác minh cuối và chốt giá/quyền theo cách làm sai điều kiện.
- [ ] Số tiền giả mạo, loại không hỗ trợ, thiếu thông tin xác nhận của luồng mới, quyền không hợp lệ hoặc giá thay đổi không tạo dữ liệu dở dang và không phát xác nhận.
- [ ] Kiểm thử API cho cả hai loại, hồi quy Tiêu chuẩn, quyền thu hồi/hết hạn, snapshot và race thu hồi/đặt lịch; xác minh hành vi khóa MySQL trong môi trường test.
- [ ] Cập nhật cặp Class/Sequence đặt lịch và PNG đen trắng để thể hiện kiểm tra quyền và chọn Strategy đúng code; giữ tổng bốn UML cho báo giá và đặt lịch.
