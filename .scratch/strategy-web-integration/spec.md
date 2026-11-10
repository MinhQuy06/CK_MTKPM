# Spec: Tích hợp Strategy vào giao diện web

Status: ready-for-agent

## Problem Statement

Tính năng Strategy tính phí khám (báo giá, quyền hưởng Bảo hiểm/VIP, Admin xác minh) đã có trên web nhưng khó tìm: link bệnh nhân chỉ có ở sidebar dashboard (sai vị trí, không icon), Admin chỉ vào được qua một nút trong trang Quản lý người dùng, hai trang quyền hưởng không theo bố cục chung, và báo giá chỉ xuất hiện khi mở modal đặt lịch.

## Solution

Chỉ sửa UI; không đổi API, công thức, dữ liệu, endpoint.

1. Sidebar mọi trang bệnh nhân có mục "Quyền hưởng BH/VIP" (`bi-shield-check`) ngay sau "Lịch hẹn".
2. Sidebar mọi trang Admin có mục "Xác minh quyền hưởng"; bỏ nút cũ trong `admin/users.html`. Không có huy hiệu đếm.
3. `patient/benefits.html` và `admin/benefits.html` dùng bố cục sidebar chung, giữ nguyên id DOM và JS trong `strategy/`.
4. Dashboard bệnh nhân có ô "Chính sách giá của bạn" dưới banner chào mừng, đọc `Api.benefits.mine()`:
   - có quyền `effectiveStatus === 'APPROVED'`: liệt kê loại + thời hạn, nút "Quản lý quyền hưởng";
   - không có: "Đang áp dụng giá Tiêu chuẩn", nút "Gửi yêu cầu xác minh", và dòng nhỏ nếu có yêu cầu `PENDING`;
   - API lỗi: ô ẩn.
5. Mọi thay đổi áp dụng cho cả `frontend/` và `backend/src/main/resources/static/`.

## Testing

- Logic chọn nội dung ô quyền hưởng là hàm thuần trong `strategy/benefit-summary.js`, có test Node.
- Test trình duyệt `frontend/tests/strategy/browser-strategy-navigation.cjs` kiểm tra link sidebar và ô dashboard.
- Chạy lại `npm --prefix frontend test` và các test trình duyệt Strategy.
