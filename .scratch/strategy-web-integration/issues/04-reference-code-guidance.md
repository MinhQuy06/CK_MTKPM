# 04 — Làm rõ "mã hồ sơ" trên form quyền hưởng

Status: ready-for-agent

Bệnh nhân không biết phải nhập mã gì: hệ thống không cấp mã; đây là mã bên ngoài (số thẻ BHYT / mã thẻ VIP).

- Nhãn, ví dụ mẫu và hướng dẫn đổi theo loại quyền:
  - Bảo hiểm: "Số thẻ BHYT", ví dụ `DN4010123456789`, hướng dẫn: mã in trên thẻ BHYT hoặc ứng dụng VssID; có thể dùng số CCCD.
  - VIP: "Mã thẻ thành viên VIP", hướng dẫn: do quầy lễ tân phòng khám cấp khi đăng ký thành viên.
- Không chặn định dạng ở form; backend giữ nguyên kiểm tra.
- Danh sách "Yêu cầu của tôi" (và trang Admin) hiển thị nhãn theo loại quyền thay cho "Mã hồ sơ".
- Chỉ sửa UI; đồng bộ `frontend/` và static backend.
