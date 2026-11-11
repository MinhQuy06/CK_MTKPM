'use strict';

// Trang Admin "Xác minh quyền hưởng": liệt kê yêu cầu, duyệt / từ chối yêu cầu đang chờ, thu hồi quyền đã duyệt.

const benefitAdmin = Auth.checkAuth(['ADMIN']);

/** Tạo phần tử với class và (tuỳ chọn) nội dung chữ. */
function adminBenefitNode(tag, cls, text) {
  const element = document.createElement(tag);
  element.className = cls || '';
  if (text !== undefined) element.textContent = text;
  return element;
}

function showAdminBenefitMessage(text) {
  document.getElementById('admin-benefit-message').textContent = text;
}

/** Ô nhập lý do (dùng cho quyết định và thu hồi). */
function reasonInput(cls, label) {
  const input = adminBenefitNode('input', cls);
  input.maxLength = 1000;
  input.placeholder = label;
  input.setAttribute('aria-label', label);
  return input;
}

/** Form duyệt / từ chối cho yêu cầu đang chờ: khoảng ngày hiệu lực (mặc định theo đề nghị) và lý do. */
function decisionForm(item) {
  const form = adminBenefitNode('div', 'row g-2 mt-2');
  const from = adminBenefitNode('input', 'form-control decision-from');
  const until = adminBenefitNode('input', 'form-control decision-until');
  from.type = until.type = 'date';
  from.value = item.requestedFrom;
  until.value = item.requestedUntil;
  from.setAttribute('aria-label', 'Ngày bắt đầu quyền hưởng');
  until.setAttribute('aria-label', 'Ngày kết thúc quyền hưởng');
  const reason = adminBenefitNode('input', 'form-control decision-reason');
  reason.maxLength = 1000;
  reason.placeholder = 'Lý do quyết định';
  reason.setAttribute('aria-label', 'Lý do quyết định');
  form.append(from, until, reason);

  ['APPROVE', 'REJECT'].forEach(action => {
    const approve = action === 'APPROVE';
    const button = adminBenefitNode('button',
      'btn ' + (approve ? 'btn-success benefit-approve' : 'btn-outline-danger benefit-reject'),
      approve ? 'Duyệt quyền hưởng' : 'Từ chối');
    button.type = 'button';
    button.addEventListener('click', async () => {
      button.disabled = true;
      try {
        await Api.benefits.decide(item.id, {
          action,
          reason: reason.value,
          approvedFrom: from.value || null,
          approvedUntil: until.value || null,
        });
        showAdminBenefitMessage('Đã lưu quyết định.');
        await loadAdminBenefits();
      } catch (error) {
        showAdminBenefitMessage(error.message);
      } finally {
        button.disabled = false;
      }
    });
    form.append(button);
  });
  return form;
}

/** Ô lý do và nút thu hồi cho quyền đã duyệt. */
function revokeControls(item) {
  const reason = reasonInput('form-control revoke-reason mb-2', 'Lý do thu hồi');
  const button = adminBenefitNode('button', 'btn btn-outline-danger benefit-revoke', 'Thu hồi quyền hưởng');
  button.type = 'button';
  button.addEventListener('click', async () => {
    button.disabled = true;
    try {
      await Api.benefits.revoke(item.id, { reason: reason.value });
      showAdminBenefitMessage('Đã thu hồi và lưu lịch sử.');
      await loadAdminBenefits();
    } catch (error) {
      showAdminBenefitMessage(error.message);
    } finally {
      button.disabled = false;
    }
  });
  return [reason, button];
}

/** Một thẻ yêu cầu: thông tin, lịch sử quyết định, và thao tác tuỳ trạng thái. */
function adminBenefitRow(item) {
  const row = adminBenefitNode('article', 'card p-3 mb-3');
  row.dataset.requestId = item.id;
  row.append(adminBenefitNode('h2', 'h5', item.patientName + ' — ' + item.kind + ' — ' + item.status));
  row.append(adminBenefitNode('p', '', BenefitKinds.of(item.kind).shortReferenceLabel + ': ' + item.referenceCode
    + ' · Đề nghị: ' + item.requestedFrom + ' → ' + item.requestedUntil));
  (item.decisions || []).forEach(d => row.append(adminBenefitNode('p', 'small mb-1',
    d.action + ': ' + d.reason + ' · Admin #' + d.decidedBy + ' · ' + d.decidedAt)));

  if (item.status === 'PENDING') row.append(decisionForm(item));
  if (item.approvedFrom) {
    row.append(adminBenefitNode('p', 'small', 'Hiệu lực đã duyệt: ' + item.approvedFrom + ' → ' + item.approvedUntil));
  }
  if (item.status === 'APPROVED') row.append(...revokeControls(item));
  return row;
}

async function loadAdminBenefits() {
  const list = document.getElementById('admin-benefit-list');
  try {
    const response = await Api.benefits.adminList();
    list.replaceChildren();
    if (!response.data.length) {
      list.textContent = 'Chưa có yêu cầu.';
      return;
    }
    response.data.forEach(item => list.append(adminBenefitRow(item)));
  } catch (error) {
    list.textContent = error.message;
  }
}

document.getElementById('admin-benefit-reload').addEventListener('click', loadAdminBenefits);
if (benefitAdmin) loadAdminBenefits();
