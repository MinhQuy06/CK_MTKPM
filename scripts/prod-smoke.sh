#!/usr/bin/env bash
# Kịch bản nghiệm thu chạy thật trên stack docker-compose.prod.yml (tự tạo dữ liệu riêng, chạy được cả khi có hay không có dữ liệu mẫu):
# admin tạo chuyên khoa + bác sĩ → bác sĩ mở ca → bệnh nhân đăng ký, xin quyền hưởng Bảo hiểm →
# admin duyệt → báo giá → đặt lịch → kiểm tra giá chốt và (nếu có MAILPIT) email đến hộp thư.
#
#   ADMIN_EMAIL=... ADMIN_PASSWORD=... ./scripts/prod-smoke.sh
# Biến tuỳ chọn: BASE (mặc định http://localhost:18081), MAILPIT (ví dụ http://localhost:8025).
set -euo pipefail
BASE="${BASE:-http://localhost:18081}"
: "${ADMIN_EMAIL:?cần ADMIN_EMAIL}" "${ADMIN_PASSWORD:?cần ADMIN_PASSWORD}"
RUN="$(date +%s)"
# Số điện thoại riêng cho mỗi lượt chạy, tránh trùng với dữ liệu mẫu (số điện thoại là duy nhất).
DOC_PHONE="08${RUN: -8}"
PAT_PHONE="09${RUN: -8}"
TOMORROW="$(date -v+1d +%F 2>/dev/null || date -d tomorrow +%F)"
NEXT_YEAR="$(date -v+1y +%F 2>/dev/null || date -d '+1 year' +%F)"
TODAY="$(date +%F)"

jget() { python3 -c "import sys,json; d=json.load(sys.stdin); print(eval('d'+sys.argv[1]))" "$1"; }
api() { # api METHOD PATH TOKEN [JSON]
  local m="$1" p="$2" t="$3" b="${4:-}"
  curl -sf -X "$m" "$BASE$p" -H 'Content-Type: application/json' ${t:+-H "Authorization: Bearer $t"} ${b:+-d "$b"}
}
step() { printf '\n== %s\n' "$*"; }

step "1. Admin đăng nhập"
ADMIN=$(api POST /api/v1/auth/login "" "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" | jget "['data']['accessToken']")

step "2. Admin tạo chuyên khoa và bác sĩ"
SPEC=$(api POST /api/v1/admin/specialties "$ADMIN" "{\"name\":\"Khoa thu $RUN\",\"description\":\"smoke\"}" | jget "['data']['id']")
DOC_EMAIL="bs.$RUN@clinic.test"
api POST /api/v1/admin/doctors "$ADMIN" "{\"email\":\"$DOC_EMAIL\",\"password\":\"Doctor#12345\",\"fullName\":\"BS Smoke $RUN\",\"phone\":\"$DOC_PHONE\",\"specialtyId\":$SPEC,\"degree\":\"ThS\",\"experienceYears\":5,\"consultationFee\":300000,\"approved\":true}" >/dev/null
echo "bác sĩ $DOC_EMAIL, chuyên khoa $SPEC"

step "3. Bác sĩ mở ca khám ngày mai"
DOC=$(api POST /api/v1/auth/login "" "{\"email\":\"$DOC_EMAIL\",\"password\":\"Doctor#12345\"}" | jget "['data']['accessToken']")
SLOT=$(api POST /api/v1/doctors/slots "$DOC" "{\"workDate\":\"$TOMORROW\",\"startTime\":\"09:00:00\",\"endTime\":\"09:30:00\"}" | jget "['data']['id']")
echo "ca $SLOT ngày $TOMORROW 09:00"

step "4. Bệnh nhân đăng ký và đăng nhập"
PAT_EMAIL="bn.${RUN}@clinic.test"
api POST /api/v1/auth/register "" "{\"email\":\"$PAT_EMAIL\",\"password\":\"Patient#12345\",\"role\":\"PATIENT\",\"fullName\":\"BN Smoke $RUN\",\"phone\":\"$PAT_PHONE\",\"gender\":\"MALE\"}" >/dev/null
PAT=$(api POST /api/v1/auth/login "" "{\"email\":\"$PAT_EMAIL\",\"password\":\"Patient#12345\"}" | jget "['data']['accessToken']")

step "5. Giá Tiêu chuẩn khi chưa có quyền hưởng; xin quyền Bảo hiểm"
api GET "/api/v1/appointments/pricing-options?scheduleId=$SLOT" "$PAT" | python3 -c "import sys,json; [print(' ',o) for o in json.load(sys.stdin)['data']]"
REQ=$(api POST /api/v1/patients/me/benefit-requests "$PAT" "{\"kind\":\"INSURANCE\",\"referenceCode\":\"BH-$RUN\",\"requestedFrom\":\"$TODAY\",\"requestedUntil\":\"$NEXT_YEAR\"}" | jget "['data']['id']")

step "6. Admin duyệt quyền hưởng"
api POST "/api/v1/admin/benefit-requests/$REQ/decision" "$ADMIN" "{\"action\":\"APPROVE\",\"reason\":\"Đã đối chiếu mã hồ sơ\",\"approvedFrom\":\"$TODAY\",\"approvedUntil\":\"$NEXT_YEAR\"}" >/dev/null && echo "đã duyệt yêu cầu $REQ"

step "7. Báo giá Bảo hiểm và đặt lịch theo giá đã đồng ý"
Q=$(api GET "/api/v1/appointments/quote?scheduleId=$SLOT&strategyKey=INSURANCE" "$PAT")
echo "$Q" | python3 -c "import sys,json; print(' ', json.load(sys.stdin)['data'])"
BOOK=$(echo "$Q" | python3 -c "
import sys,json
d=json.load(sys.stdin)['data']
print(json.dumps({'scheduleId':d['scheduleId'],'symptoms':'smoke','strategyKey':d['strategyKey'],
 'expectedWorkDate':d['workDate'],'expectedStartTime':d['startTime'],'expectedEndTime':d['endTime'],
 'expectedBasePrice':d['basePrice'],'expectedDiscountRate':d['discountRate'],'expectedPrice':d['finalPrice']}))")
APPT=$(api POST /api/v1/appointments/confirm "$PAT" "$BOOK")
echo "$APPT" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print('  lịch hẹn', d.get('bookingCode'), '| giá chốt:', {k:v for k,v in d.items() if 'rice' in k or 'iscount' in k})"
CODE=$(echo "$APPT" | jget "['data']['bookingCode']")

if [ -n "${MAILPIT:-}" ]; then
  step "8. Email xác nhận đến hộp thư (Mailpit)"
  for _ in $(seq 1 15); do
    if curl -sf "$MAILPIT/api/v1/messages" | grep -q "$CODE"; then echo "  có thư cho $CODE"; FOUND=1; break; fi
    sleep 1
  done
  [ "${FOUND:-}" = 1 ] || { echo "KHÔNG thấy email cho $CODE"; exit 1; }
fi
printf '\nOK: kịch bản nghiệm thu hoàn tất (%s)\n' "$CODE"
