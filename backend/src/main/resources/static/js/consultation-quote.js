// Keeps asynchronous quote responses tied to the current slot and pricing kind.
(function (root) {
  'use strict';

  function create({ fetchQuote, onChange }) {
    let version = 0;
    let state = { status: 'idle', quote: null, error: '', scheduleId: null, strategyKey: 'STANDARD' };
    const update = next => { state = next; onChange(state); };

    function reset() {
      version++;
      update({ status: 'idle', quote: null, error: '', scheduleId: null, strategyKey: 'STANDARD' });
    }

    async function select(scheduleId, strategyKey = 'STANDARD') {
      const request = ++version;
      update({ status: 'loading', quote: null, error: '', scheduleId, strategyKey });
      try {
        const quote = await fetchQuote(scheduleId, strategyKey);
        if (request !== version) return;
        if (!quote || String(quote.scheduleId) !== String(scheduleId) || quote.strategyKey !== strategyKey) {
          throw new Error('Báo giá không khớp ca khám đã chọn. Vui lòng thử lại.');
        }
        update({ status: 'ready', quote, error: '', scheduleId, strategyKey });
      } catch (error) {
        if (request !== version) return;
        update({ status: 'error', quote: null, error: error.message || 'Không thể tải báo giá. Vui lòng thử lại.',
          scheduleId, strategyKey });
      }
    }

    return { select, reset, getState: () => state };
  }

  const api = { create };
  if (typeof module === 'object' && module.exports) module.exports = api;
  else root.ConsultationQuote = api;
})(globalThis);
