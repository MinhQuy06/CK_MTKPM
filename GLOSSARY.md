# Đặt lịch khám

Phòng khám cho bệnh nhân xem ca trống của bác sĩ, báo giá theo loại phí và đặt lịch; Admin xác minh quyền hưởng giảm giá.

## Ca và lịch hẹn

**Ca khám (Slot)**:
Một khoảng thời gian cụ thể (ngày, giờ bắt đầu, giờ kết thúc) của một bác sĩ mà bệnh nhân có thể đặt.
_Avoid_: lịch làm việc, schedule (khi nói với người dùng)

**Ca đặt được (Bookable slot)**:
Ca khám còn trống, chưa có lịch hẹn, chưa bắt đầu, và thuộc bác sĩ đã được duyệt và đang hoạt động.
_Avoid_: ca hợp lệ, ca khả dụng

**Lịch hẹn (Appointment)**:
Việc một bệnh nhân giữ một ca khám, kèm giá đã chốt tại thời điểm đặt.
_Avoid_: booking, đơn đặt

## Giá

**Loại phí (Pricing type)**:
Cách tính phí khám được chọn cho một lịch hẹn: Tiêu chuẩn, Bảo hiểm hoặc VIP. Mỗi lịch hẹn dùng đúng một loại, không cộng dồn.
_Avoid_: gói, chính sách giá, strategy (khi nói với người dùng)

**Phí gốc (Base fee)**:
Phí khám niêm yết của bác sĩ trước khi áp dụng loại phí.
_Avoid_: giá bác sĩ, consultation fee

**Giá chào (Booking offer)**:
Kết quả trả lời câu hỏi "ca này, với loại phí này, bệnh nhân này trả bao nhiêu": phí gốc, tỷ lệ giảm và giá cuối cho một ca đặt được.
_Avoid_: báo giá (khi nói về khái niệm chung), quote object

**Giá đã đồng ý (Agreed quote)**:
Giá chào mà bệnh nhân đã xem và bấm xác nhận, gồm số tiền và ngày giờ ca; lịch hẹn chỉ được tạo nếu giá chào lúc xác nhận vẫn trùng với nó.
_Avoid_: expected price, giá kỳ vọng

**Đặt lịch kiểu cũ (Legacy booking)**:
Đặt lịch không kèm giá đã đồng ý, chỉ được phép với loại phí Tiêu chuẩn, giữ cho client cũ.
_Avoid_: đặt nhanh, đặt không báo giá

**Giá chốt (Price snapshot)**:
Loại phí, phí gốc, tỷ lệ giảm và giá cuối được lưu cùng lịch hẹn; không đổi khi phí bác sĩ hay quyền hưởng thay đổi sau đó.
_Avoid_: giá hiện tại

## Quyền hưởng

**Quyền hưởng (Benefit)**:
Sự cho phép bệnh nhân dùng loại phí Bảo hiểm hoặc VIP trong một khoảng ngày, do Admin duyệt sau khi đối chiếu mã hồ sơ.
_Avoid_: ưu đãi, voucher, discount

**Quyền hưởng còn hiệu lực (Covering benefit)**:
Quyền hưởng đã duyệt, chưa thu hồi, có khoảng ngày chứa cả hôm nay lẫn ngày khám (tính theo giờ Việt Nam, kể cả ngày biên).
_Avoid_: quyền hợp lệ, active benefit

## Từ chối đặt lịch

**Lý do từ chối (Booking rejection)**:
Lý do có tên khiến không báo giá hoặc không đặt được lịch: ca không còn đặt được, giá đã đổi, không có quyền hưởng còn hiệu lực, loại phí không hỗ trợ, bác sĩ không nhận lịch.
_Avoid_: lỗi đặt lịch, booking error
