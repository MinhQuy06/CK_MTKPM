# 10: Phục hồi khi mất quyền hưởng hoặc mất ca

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/17

**What to build:** Nếu quyền hưởng hết hiệu lực/bị thu hồi hoặc ca bị người khác đặt, bệnh nhân biết đúng nguyên nhân, xem lựa chọn còn hợp lệ và chủ động chọn lại, giữ triệu chứng.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 08: Đặt lịch Bảo hiểm và VIP có kiểm tra lại quyền hưởng

- [ ] Giao diện phân biệt mất quyền hưởng với mất ca và thay đổi giá; không hiển thị mọi lỗi như cùng một nguyên nhân.
- [ ] Mất quyền: cập nhật loại phí đủ điều kiện, không âm thầm áp giảm hay đổi về Tiêu chuẩn; yêu cầu người dùng chọn lại và xem báo giá mới.
- [ ] Mất ca: xóa lựa chọn/báo giá của ca đó, tải lại ca còn đặt được; không lặp gửi request đặt ca cũ.
- [ ] Giữ triệu chứng và thông tin còn hợp lệ; chặn xác nhận với báo giá cũ và loại bỏ response về muộn sau phục hồi.
- [ ] Kiểm thử API/giao diện các nhánh từ chối/thu hồi/hết hạn, mất ca và không phát sinh lịch/email khi xác nhận thất bại.
- [ ] Chạy trình duyệt thật toàn luồng yêu cầu quyền → Admin duyệt → báo giá → đặt → đọc snapshot; kiểm tra Standard, Bảo hiểm và VIP, thêm tình huống phục hồi.
- [ ] Đối chiếu cuối cùng đúng 4 UML: Class + Sequence báo giá và Class + Sequence đặt lịch. File draw.io mở/chỉnh sửa được, xuất PNG cục bộ trắng/đen, quan hệ/vai trò khớp code và chữ không bị cắt.
- [ ] Hoàn thiện bằng chứng kiểm thử và hướng dẫn demo theo hành vi đã chạy; không thêm UML Admin, pattern khác, thanh toán hoặc kết nối bảo hiểm ngoài.
