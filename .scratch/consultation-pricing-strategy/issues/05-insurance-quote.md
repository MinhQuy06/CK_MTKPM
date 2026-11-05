# 05: Báo giá Bảo hiểm theo quyền đã được duyệt

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/12

**What to build:** Bệnh nhân có quyền Bảo hiểm hợp lệ chọn loại này cho ca khám và xem giá do Insurance Strategy tính. Server kiểm tra quyền đã được duyệt thay vì tin lựa chọn từ trình duyệt.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 01: Báo giá Tiêu chuẩn qua Strategy, 03: Admin duyệt hoặc từ chối yêu cầu quyền hưởng

- [ ] Concrete Strategy Bảo hiểm đăng ký qua giao diện/Context hiện có, không đưa công thức riêng vào cây if/switch trong service đặt lịch.
- [ ] Mặc định giảm 80%, bệnh nhân trả 20%: 300.000đ → 60.000đ. Tỷ lệ cấu hình được, BigDecimal/HALF_UP và giới hạn tiền thống nhất với Tiêu chuẩn.
- [ ] API trả các loại bệnh nhân đủ điều kiện cho ca; giao diện cho chọn Bảo hiểm khi hợp lệ và hiển thị đầy đủ báo giá mới.
- [ ] Quyền phải đã duyệt, chưa thu hồi, có hiệu lực cả hiện tại và ngày khám theo Asia/Ho_Chi_Minh; quyền chờ/từ chối/chưa đến hạn/hết hạn bị loại.
- [ ] Request sửa tay sang Bảo hiểm khi không hợp lệ bị từ chối rõ ràng; không âm thầm chuyển Tiêu chuẩn. Quyền xét theo bệnh nhân xác thực.
- [ ] Không giữ giới hạn chỉ demo/test: quyền hợp lệ dùng được ở môi trường vận hành, theo chính sách nội bộ phòng khám; không tuyên bố khoản bảo hiểm sẽ hoàn trả.
- [ ] Kiểm thử API kết quả, phân quyền, ngày biên, hết hạn trước ngày khám, chưa hiệu lực lúc đặt, quyền không hợp lệ và response giao diện về muộn; báo giá vẫn không giữ chỗ.
- [ ] Cập nhật cùng cặp Class/Sequence báo giá và PNG đen trắng đã có để thể hiện chọn Strategy Bảo hiểm và bước kiểm tra quyền cần thiết; không sinh bộ UML Admin riêng.
