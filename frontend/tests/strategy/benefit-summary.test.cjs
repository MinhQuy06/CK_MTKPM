const { test } = require('node:test');
const assert = require('node:assert/strict');
const Summary = require('../../strategy/benefit-summary.js');

const request = (over) => ({ id: 1, kind: 'INSURANCE', status: 'APPROVED', effectiveStatus: 'APPROVED',
  approvedFrom: '2026-11-01', approvedUntil: '2026-12-31', ...over });

test('active approved benefits are listed with their validity', () => {
  const summary = Summary.summarize([request(), request({ id: 2, kind: 'VIP', approvedFrom: '2026-10-01', approvedUntil: '2027-01-31' })]);
  assert.deepEqual(summary.active, [
    { kind: 'INSURANCE', label: 'Bảo hiểm', from: '2026-11-01', until: '2026-12-31' },
    { kind: 'VIP', label: 'VIP', from: '2026-10-01', until: '2027-01-31' }
  ]);
});

test('expired, revoked and not-yet-valid grants fall back to Standard; pending requests are counted', () => {
  const summary = Summary.summarize([
    request({ effectiveStatus: 'EXPIRED' }),
    request({ id: 2, status: 'REVOKED', effectiveStatus: 'REVOKED' }),
    request({ id: 3, effectiveStatus: 'NOT_YET_VALID' }),
    request({ id: 4, kind: 'VIP', status: 'PENDING', effectiveStatus: 'PENDING', approvedFrom: null, approvedUntil: null })
  ]);
  assert.deepEqual(summary.active, []);
  assert.equal(summary.pendingCount, 1);
});

test('no requests at all means Standard pricing with nothing pending', () => {
  assert.deepEqual(Summary.summarize([]), { active: [], pendingCount: 0 });
});
