const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage();page.on('pageerror',e=>console.error('PAGE_ERROR',e.message));page.on('requestfailed',r=>console.error('REQUEST_FAILED',r.url(),r.failure()?.errorText));const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>{const r=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return(await r.json()).data;};
 const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');
 await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},patient);
 for(const kind of ['INSURANCE','VIP']){
  const input={kind,referenceCode:'BOOK-'+kind,requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'};
  const submitted=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:{Authorization:'Bearer '+patient.accessToken},data:input});assert.equal(submitted.status(),201);const grant=(await submitted.json()).data.id;
  const approved=await page.request.post(origin+'/api/v1/admin/benefit-requests/'+grant+'/decision',{headers:{Authorization:'Bearer '+admin.accessToken},data:{action:'APPROVE',reason:'Đối chiếu hồ sơ',approvedFrom:input.requestedFrom,approvedUntil:input.requestedUntil}});assert.equal(approved.status(),200);
  await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();
  await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
  await page.waitForFunction(kind=>document.querySelector('#pricing-kind option[value="'+kind+'"]'),kind);
  await page.locator('#pricing-kind').selectOption(kind);
  await page.waitForFunction(kind=>consultationQuote.getState().status==='ready'&&consultationQuote.getState().quote.strategyKey===kind,kind);
  await page.locator('#symptoms').fill('Đặt theo quyền '+kind);
  const created=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm')&&r.request().method()==='POST');
  await page.locator('#btn-confirm-booking').click();const response=await created;assert.equal(response.status(),201);
  const data=(await response.json()).data,expected=kind==='INSURANCE'?60000:270000;
  assert.equal(data.price,expected);assert.equal(data.pricingKey,kind);assert.equal(data.benefitRequestId,grant);
  const revoked=await page.request.post(origin+'/api/v1/admin/benefit-requests/'+grant+'/revoke',{headers:{Authorization:'Bearer '+admin.accessToken},data:{reason:'Thu hồi sau khi đã đặt'}});assert.equal(revoked.status(),200);
  const read=await page.request.get(origin+'/api/v1/appointments/'+data.id,{headers:{Authorization:'Bearer '+patient.accessToken}});
  const saved=(await read.json()).data;assert.equal(saved.price,expected);assert.equal(saved.benefitRequestId,grant);
 }
 console.log('PASS: verified Insurance and VIP → actual UI quote/confirm → correct saved grant/price; later revocation preserves both historical snapshots.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
