# 06: Báo giá VIP theo quyền đã được duyệt

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/13

**What to build:** Bệnh nhân có quyền VIP hợp lệ chọn loại này cho ca khám và xem giá do Vip Strategy tính. Có cả Bảo hiểm/VIP thì bệnh nhân chọn một loại được phép, không cộng dồn.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 01: Báo giá Tiêu chuẩn qua Strategy, 03: Admin duyệt hoặc từ chối yêu cầu quyền hưởng

- [ ] Concrete Strategy VIP dùng giao diện và Context hiện có; thêm thuật toán không sửa công thức điều phối.
- [ ] Mặc định giảm 10%, bệnh nhân trả 90%: 300.000đ → 270.000đ. Tỷ lệ cấu hình được, BigDecimal/HALF_UP và giới hạn tiền thống nhất.
- [ ] API/giao diện hiển thị VIP trong lựa chọn hợp lệ của chính bệnh nhân cho ca; kiểm tra đã duyệt, chưa thu hồi, hiệu lực hiện tại và ngày khám theo múi giờ Việt Nam.
- [ ] Request loại VIP không hợp lệ bị server từ chối, không tự áp giảm hoặc chuyển loại. Chờ/từ chối/chưa đến hạn/hết hạn đều không đủ điều kiện.
- [ ] VIP hợp lệ dùng được ở môi trường vận hành, không khóa theo demo/test; mỗi báo giá chỉ một loại, không tự chọn ưu đãi tốt nhất thay người dùng.
- [ ] Kiểm thử API công thức, quyền, ngày biên và danh sách lựa chọn; kiểm thử giao diện đổi loại/ca và loại bỏ response cũ. Báo giá không tạo lịch, giữ ca hay phát email.
- [ ] Cập nhật cùng cặp Class/Sequence báo giá và PNG đen trắng để phản ánh VIP thực tế; giữ tổng chỉ hai sơ đồ cho chức năng báo giá.
