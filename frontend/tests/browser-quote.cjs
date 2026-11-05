const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

(async () => {
  const origin = process.env.QUOTE_TEST_ORIGIN || 'http://localhost:18081';
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const page = await browser.newPage({ viewport: { width: 1360, height: 1000 } });
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  let bookingPosts = 0;
  page.on('request', request => {
    if (request.method() === 'POST' && new URL(request.url()).pathname === '/api/v1/appointments') bookingPosts++;
  });
  try {
    // Login and all quote/slot reads use the real backend and real JWT.
    const login = await page.request.post(origin + '/api/v1/auth/login', { data: {
      email: 'quote-patient@test.vn', password: 'BrowserTest123!'
    } });
    assert.equal(login.status(), 200);
    const auth = (await login.json()).data;
    await page.addInitScript(auth => {
      localStorage.setItem('accessToken', auth.accessToken);
      localStorage.setItem('refreshToken', auth.refreshToken);
      localStorage.setItem('user', JSON.stringify(auth));
    }, auth);
    await page.goto(origin + '/patient/dashboard.html');
    await page.waitForFunction(() => document.querySelectorAll('button[onclick^="openBookingModal"]').length >= 2);
    await page.locator('button[onclick^="openBookingModal"]').first().click();
    await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
    await page.waitForFunction(() => !document.getElementById('quote-details').hidden);
    assert.equal(await page.locator('#quote-kind').innerText(), 'Tiêu chuẩn');
    assert.equal(await page.locator('#quote-currency').innerText(), 'VND');
    assert.match(await page.locator('#quote-base').innerText(), /300[.\s]000/);
    assert.match(await page.locator('#quote-final').innerText(), /300[.\s]000/);
    assert.equal(await page.locator('#quote-discount').innerText(), '0%');
    assert.equal(await page.locator('#btn-confirm-booking').isEnabled(), true);
    const evidence = process.env.QUOTE_EVIDENCE_DIR || path.resolve(__dirname, '../../docs/strategy/evidence');
    fs.mkdirSync(evidence, { recursive: true });
    await page.screenshot({ path: path.join(evidence, 'standard-quote-browser.png'), fullPage: true });

    // Reorder real backend responses, rather than substituting a fake quote payload.
    let releaseOld, oldArrived;
    const gate = new Promise(resolve => { releaseOld = resolve; });
    const arrived = new Promise(resolve => { oldArrived = resolve; });
    let first = true;
    await page.route('**/api/v1/appointments/quote?*', async route => {
      const response = await route.fetch();
      if (first) { first = false; oldArrived(); await gate; }
      await route.fulfill({ response });
    });
    const slots = page.locator('#slots-container .slot-btn:not([disabled])');
    await slots.nth(0).click(); await arrived;
    await slots.nth(1).click();
    await page.waitForFunction(() => consultationQuote.getState().status === 'ready');
    const selectedId = await page.locator('#selected-schedule-id').inputValue();
    releaseOld();
    await page.waitForResponse(response => response.url().includes('/appointments/quote?')
      && !response.url().includes('scheduleId=' + selectedId + '&'));
    assert.equal(await page.evaluate(() => String(consultationQuote.getState().quote.scheduleId)), selectedId);
    await page.unroute('**/api/v1/appointments/quote?*');

    // An invalid date resets displayed price and disables confirmation.
    await page.locator('#booking-date').fill('');
    await page.locator('#booking-date').dispatchEvent('change');
    assert.equal(await page.locator('#quote-details').isHidden(), true);
    assert.equal(await page.locator('#btn-confirm-booking').isDisabled(), true);
    await page.locator('#booking-modal [data-bs-dismiss="modal"]').first().click();
    await page.waitForFunction(() => !document.getElementById('booking-modal').classList.contains('show'));
    await page.locator('button[onclick^="openBookingModal"]').nth(1).click();
    await page.locator('#booking-date').fill(new Date(Date.now() + 86400000).toISOString().slice(0, 10));
    await page.locator('#booking-date').dispatchEvent('change');
    await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
    await page.waitForFunction(() => !document.getElementById('quote-details').hidden);
    assert.match(await page.locator('#quote-final').innerText(), /450[.\s]000/);
    assert.equal(bookingPosts, 0);
    assert.deepEqual(errors, []);
    console.log('PASS: real JWT → doctor/slot → Standard quote; stale response ignored; date reset; second doctor; no booking POST; no page errors.');
  } finally {
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
