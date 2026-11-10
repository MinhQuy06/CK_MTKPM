// Summarises a patient's benefit requests into the pricing policy shown on the dashboard.
(function (root) {
  'use strict';

  const labels = { INSURANCE: 'Bảo hiểm', VIP: 'VIP' };

  function summarize(requests) {
    const active = (requests || [])
      .filter(r => r.effectiveStatus === 'APPROVED')
      .map(r => ({ kind: r.kind, label: labels[r.kind] || r.kind, from: r.approvedFrom, until: r.approvedUntil }));
    const pendingCount = (requests || []).filter(r => r.status === 'PENDING').length;
    return { active, pendingCount };
  }

  const formatIsoDate = iso => iso ? iso.split('-').reverse().join('/') : '…';

  // Fills the dashboard card; hides it when the request list cannot be loaded.
  async function mount(card, loadRequests) {
    let summary;
    try { summary = summarize(await loadRequests()); }
    catch (error) { card.hidden = true; return; }
    const body = card.querySelector('[data-benefit-summary-body]');
    const link = card.querySelector('[data-benefit-summary-link]');
    body.replaceChildren();
    if (summary.active.length) {
      summary.active.forEach(benefit => {
        const line = document.createElement('p'); line.className = 'mb-1';
        const name = document.createElement('span'); name.className = 'badge bg-success me-2'; name.textContent = benefit.label;
        line.append(name, 'Hiệu lực ' + formatIsoDate(benefit.from) + ' → ' + formatIsoDate(benefit.until));
        body.append(line);
      });
      link.textContent = 'Quản lý quyền hưởng';
    } else {
      const line = document.createElement('p'); line.className = 'mb-1';
      line.textContent = 'Đang áp dụng giá Tiêu chuẩn. Có thẻ Bảo hiểm hoặc VIP? Gửi mã hồ sơ để được giảm phí khám.';
      body.append(line);
      link.textContent = 'Gửi yêu cầu xác minh';
    }
    if (!summary.active.length && summary.pendingCount) {
      const pending = document.createElement('p'); pending.className = 'small text-muted mb-0';
      pending.textContent = summary.pendingCount + ' yêu cầu đang chờ duyệt.';
      body.append(pending);
    }
    card.hidden = false;
  }

  const api = { summarize, mount };
  if (typeof module === 'object' && module.exports) module.exports = api;
  else root.BenefitSummary = api;
})(typeof window !== 'undefined' ? window : globalThis);
