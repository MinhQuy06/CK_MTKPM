const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const Quote = require('../../strategy/consultation-quote.js');

const quote = (id) => ({ scheduleId: id, strategyKey: 'STANDARD', strategyName: 'Tiêu chuẩn',
  basePrice: 300000, discountRate: 0, finalPrice: 300000, currency: 'VND', simulated: false });
const deferred = () => { let resolve, reject; const promise = new Promise((ok, fail) => {
  resolve = ok; reject = fail;
}); return { promise, resolve, reject }; };

test('selection loads current quote and defaults to Standard', async () => {
  const controller = Quote.create({ fetchQuote: async (id, key) => {
    assert.equal(key, 'STANDARD'); return quote(id);
  }, onChange() {} });
  await controller.select(1);
  assert.equal(controller.getState().status, 'ready');
  assert.equal(controller.getState().quote.finalPrice, 300000);
});

test('late response from a previous slot cannot replace current quote', async () => {
  const old = deferred(), current = deferred();
  const controller = Quote.create({ fetchQuote: id => id === 1 ? old.promise : current.promise, onChange() {} });
  const first = controller.select(1), second = controller.select(2);
  current.resolve(quote(2)); await second;
  old.resolve(quote(1)); await first;
  assert.equal(controller.getState().quote.scheduleId, 2);
});

test('changing pricing kind discards earlier kind response', async () => {
  const old = deferred(), current = deferred();
  const controller = Quote.create({ fetchQuote: (id, key) => key === 'STANDARD' ? old.promise : current.promise, onChange() {} });
  const first = controller.select(1), second = controller.select(1, 'CUSTOM');
  current.resolve({ ...quote(1), strategyKey: 'CUSTOM' }); await second;
  old.resolve(quote(1)); await first;
  assert.equal(controller.getState().quote.strategyKey, 'CUSTOM');
});

test('closing or changing date clears amounts and ignores in-flight responses', async () => {
  const pending = deferred();
  const controller = Quote.create({ fetchQuote: () => pending.promise, onChange() {} });
  const request = controller.select(1);
  assert.equal(controller.getState().status, 'loading');
  controller.reset(); pending.resolve(quote(1)); await request;
  assert.equal(controller.getState().status, 'idle');
  assert.equal(controller.getState().quote, null);
});

test('current request failure removes the old price and provides an error', async () => {
  const controller = Quote.create({ fetchQuote: async id => {
    if (id === 1) return quote(1);
    throw new Error('Ca khám không còn khả dụng');
  }, onChange() {} });
  await controller.select(1); await controller.select(2);
  assert.equal(controller.getState().status, 'error');
  assert.equal(controller.getState().quote, null);
  assert.match(controller.getState().error, /không còn khả dụng/);
});

test('stale error cannot replace a successful current quote', async () => {
  const old = deferred();
  const controller = Quote.create({ fetchQuote: id => id === 1 ? old.promise : Promise.resolve(quote(2)), onChange() {} });
  const first = controller.select(1); await controller.select(2);
  old.reject(new Error('old error')); await first;
  assert.equal(controller.getState().status, 'ready');
  assert.equal(controller.getState().quote.scheduleId, 2);
});

test('a mismatched payload is never shown as the selected quote', async () => {
  const controller = Quote.create({ fetchQuote: async () => quote(99), onChange() {} });
  await controller.select(1);
  assert.equal(controller.getState().status, 'error');
  assert.equal(controller.getState().quote, null);
});

function loadApi(location, override) {
  const calls = [];
  const context = { window: { location, CLINIC_API_BASE: override }, URLSearchParams,
    localStorage: { getItem: () => 'fixture-token' }, fetch: async (url, options) => {
      calls.push({ url, options }); return { ok: true, status: 200, json: async () => ({ data: quote(7) }) };
    } };
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../js/api.js'), 'utf8') + '\nglobalThis.client = Api;', context);
  return { api: context.client, calls };
}

test('quote uses same origin on an alternate port and sends Bearer token', async () => {
  const { api, calls } = loadApi({ protocol: 'http:', port: '18081' });
  await api.appointments.quote(7);
  assert.equal(calls[0].url, '/api/v1/appointments/quote?scheduleId=7&strategyKey=STANDARD');
  assert.equal(calls[0].options.headers.Authorization, 'Bearer fixture-token');
});

test('explicit API base is preserved and query parameters are encoded', async () => {
  const { api, calls } = loadApi({ protocol: 'http:', port: '5500' }, 'https://clinic.example/api/v1');
  await api.appointments.quote(7, 'A&B');
  assert.equal(calls[0].url, 'https://clinic.example/api/v1/appointments/quote?scheduleId=7&strategyKey=A%26B');
});

test('booking errors preserve machine code and HTTP status for recovery UI', async () => {
  const context = { window:{location:{protocol:'http:',port:'18081'}}, URLSearchParams,
    localStorage:{getItem:()=> 'fixture-token'},fetch:async()=>({
      ok:false,status:409,json:async()=>({success:false,code:'PRICE_CHANGED',message:'Giá đã đổi'})
    })};
  vm.runInNewContext(fs.readFileSync(path.join(__dirname,'../../js/api.js'),'utf8')+'\nglobalThis.client=Api;',context);
  await assert.rejects(context.client.appointments.create({scheduleId:7}), error=>error.code==='PRICE_CHANGED'&&error.status===409);
});


test('current quote rejection keeps the recovery code; a later selection clears it', async () => {
  const controller = Quote.create({ fetchQuote: async id => {
    if (id === 1) throw Object.assign(new Error('Quyền đã thu hồi'), {code:'BENEFIT_UNAVAILABLE'});
    return quote(id);
  }, onChange() {} });
  await controller.select(1); assert.equal(controller.getState().errorCode, 'BENEFIT_UNAVAILABLE');
  await controller.select(2); assert.equal(controller.getState().errorCode, undefined);
});
