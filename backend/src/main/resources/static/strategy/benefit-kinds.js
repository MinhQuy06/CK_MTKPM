// Display text for each benefit kind, shared by the patient, admin and dashboard Strategy pages.
// The reference code is issued outside this system (BHYT card, clinic VIP card); the UI only explains where to find it.
(function (root) {
  'use strict';

  const kinds = {
    INSURANCE: {
      label: 'Bảo hiểm',
      referenceLabel: 'Số thẻ BHYT',
      shortReferenceLabel: 'Số thẻ BHYT',
      placeholder: 'Ví dụ: DN4010123456789',
      hint: 'Mã in trên thẻ BHYT hoặc trong ứng dụng VssID; có thể dùng số CCCD nếu thẻ đã tích hợp.'
    },
    VIP: {
      label: 'VIP',
      referenceLabel: 'Mã thẻ thành viên VIP',
      shortReferenceLabel: 'Mã thẻ VIP',
      placeholder: 'Ví dụ: VIP-2026-0001',
      hint: 'Mã thẻ do quầy lễ tân phòng khám cấp khi bạn đăng ký thành viên VIP.'
    }
  };
  const fallback = kind => ({ label: kind, referenceLabel: 'Mã hồ sơ', shortReferenceLabel: 'Mã hồ sơ', placeholder: '', hint: '' });

  const api = { of: kind => kinds[kind] || fallback(kind) };
  if (typeof module === 'object' && module.exports) module.exports = api;
  else root.BenefitKinds = api;
})(typeof window !== 'undefined' ? window : globalThis);
