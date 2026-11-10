'use strict';

// Trang bệnh nhân "Quyền hưởng BH/VIP": gửi yêu cầu mới hoặc gia hạn, xem trạng thái và lịch sử quyết định.

const benefitUser = Auth.checkAuth(['PATIENT']);

const benefitLabels = {
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Từ chối',
  REVOKED: 'Đã thu hồi',
  EXPIRED: 'Đã hết hạn',
  NOT_YET_VALID: 'Chưa đến ngày hiệu lực',
  INVALID: 'Thiếu dữ liệu hiệu lực',
};

function benefitParagraph(text, cls) {
  const p = document.createElement('p');
  if (cls) p.className = cls;
  p.textContent = text;
  return p;
}

/** Điền sẵn form để gia hạn từ một yêu cầu đã từng được duyệt. */
function startRenewal(item) {
  document.getElementById('benefit-renewal').value = item.id;
  document.getElementById('benefit-kind').value = item.kind;
  showReferenceGuidance();
  document.getElementById('benefit-reference').value = item.referenceCode;
  document.getElementById('benefit-from').value = '';
  document.getElementById('benefit-until').value = '';
  document.getElementById('benefit-message').textContent =
    'Đang đề nghị gia hạn yêu cầu #' + item.id + '. Chọn thời hạn mới rồi gửi để được duyệt.';
  document.getElementById('benefit-form').scrollIntoView({ behavior: 'smooth' });
}

/** Một thẻ yêu cầu: loại + trạng thái, mã hồ sơ, hiệu lực, lịch sử, và nút gia hạn nếu được. */
function benefitRow(item) {
  const kind = BenefitKinds.of(item.kind);
  const article = document.createElement('article');
  article.className = 'border rounded p-3 mb-2';
  article.dataset.requestId = item.id;

  const title = document.createElement('h3');
  title.className = 'h6';
  title.textContent = kind.label + ' — '
    + (benefitLabels[item.effectiveStatus] || benefitLabels[item.status] || item.status);
  article.append(title, benefitParagraph(kind.shortReferenceLabel + ': ' + item.referenceCode
    + ' · Đề nghị: ' + item.requestedFrom + ' → ' + item.requestedUntil, 'mb-0'));

  if (item.approvedFrom) {
    article.append(benefitParagraph('Hiệu lực đã duyệt: ' + item.approvedFrom + ' → ' + item.approvedUntil));
  }
  (item.decisions || []).forEach(d =>
    article.append(benefitParagraph(d.action + ': ' + d.reason + ' · ' + d.decidedAt, 'small mb-1')));
  if (item.renewalOfId) {
    article.append(benefitParagraph('Gia hạn từ yêu cầu #' + item.renewalOfId));
  }
  if (['APPROVED', 'REVOKED'].includes(item.status)) {
    const renew = document.createElement('button');
    renew.type = 'button';
    renew.className = 'btn btn-outline-primary benefit-renew';
    renew.textContent = 'Gửi yêu cầu gia hạn';
    renew.addEventListener('click', () => startRenewal(item));
    article.append(renew);
  }
  return article;
}

async function loadBenefitRequests() {
  const container = document.getElementById('benefit-list');
  try {
    const response = await Api.benefits.mine();
    container.replaceChildren();
    if (!response.data.length) {
      container.textContent = 'Bạn chưa có yêu cầu nào.';
      return;
    }
    response.data.forEach(item => container.append(benefitRow(item)));
  } catch (error) {
    container.textContent = error.message;
  }
}

/** Cho bệnh nhân biết cần nhập mã gì (thẻ BHYT hay mã thành viên VIP) theo loại quyền đang chọn. */
function showReferenceGuidance() {
  const kind = BenefitKinds.of(document.getElementById('benefit-kind').value);
  document.querySelector('label[for="benefit-reference"]').textContent = kind.referenceLabel;
  document.getElementById('benefit-reference').placeholder = kind.placeholder;
  document.getElementById('benefit-reference-hint').textContent = kind.hint;
}

document.getElementById('benefit-kind').addEventListener('change', showReferenceGuidance);
showReferenceGuidance();
document.getElementById('benefit-reload').addEventListener('click', loadBenefitRequests);

document.getElementById('benefit-form').addEventListener('submit', async event => {
  event.preventDefault();
  const button = document.getElementById('benefit-submit');
  const message = document.getElementById('benefit-message');
  const renewalOf = document.getElementById('benefit-renewal').value;
  button.disabled = true;
  message.textContent = '';
  try {
    await Api.benefits.submit({
      ...(renewalOf ? { renewalOfId: Number(renewalOf) } : {}),
      kind: document.getElementById('benefit-kind').value,
      referenceCode: document.getElementById('benefit-reference').value,
      requestedFrom: document.getElementById('benefit-from').value,
      requestedUntil: document.getElementById('benefit-until').value,
    });
    document.getElementById('benefit-renewal').value = '';
    message.textContent = 'Đã gửi yêu cầu. Vui lòng chờ phòng khám xác minh.';
    await loadBenefitRequests();
  } catch (error) {
    message.textContent = error.message;
  } finally {
    button.disabled = false;
  }
});

if (benefitUser) loadBenefitRequests();
