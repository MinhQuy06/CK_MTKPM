const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage(),adminPage=await browser.newPage(),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';const errors=[];
 for(const p of [page,adminPage])p.on('pageerror',e=>errors.push(e.message));
 const login=async email=>{const response=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(response.status(),200);return(await response.json()).data;};
 const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');
 for(const [p,a] of [[page,patient],[adminPage,admin]]) await p.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},a);
 for(const kind of ['INSURANCE','VIP']){
  await page.goto(origin+'/patient/benefits.html');await page.locator('#benefit-kind').selectOption(kind);await page.locator('#benefit-reference').fill('FULL-'+kind);
  await page.locator('#benefit-from').fill('2026-01-01');await page.locator('#benefit-until').fill('2027-12-31');
  const created=page.waitForResponse(r=>r.url().endsWith('/patients/me/benefit-requests')&&r.request().method()==='POST');await page.locator('#benefit-submit').click();const response=await created;assert.equal(response.status(),201);const id=(await response.json()).data.id;
  await adminPage.goto(origin+'/admin/benefits.html');const row=adminPage.locator('[data-request-id="'+id+'"]');await row.waitFor();await row.locator('.decision-reason').fill('Đã đối chiếu hồ sơ gốc');
  const approved=adminPage.waitForResponse(r=>r.url().endsWith('/'+id+'/decision'));await row.locator('.benefit-approve').click();assert.equal((await approved).status(),200);
 }
 for(const [kind,docIndex,expected] of [['STANDARD',0,300000],['INSURANCE',0,60000],['VIP',1,405000]]){
  await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').nth(docIndex).click();await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
  await page.waitForFunction(()=>!document.getElementById('pricing-kind').disabled);await page.locator('#pricing-kind').selectOption(kind);await page.waitForFunction(k=>consultationQuote.getState().quote?.strategyKey===k,kind);
  await page.locator('#symptoms').fill('Luồng hoàn chỉnh '+kind);
  const booked=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm'));await page.locator('#btn-confirm-booking').click();const response=await booked;assert.equal(response.status(),201);const saved=(await response.json()).data;assert.equal(saved.price,expected);assert.equal(saved.pricingKey,kind);
  const read=await page.request.get(origin+'/api/v1/appointments/'+saved.id,{headers:{Authorization:'Bearer '+patient.accessToken}});assert.equal(read.status(),200);const detail=(await read.json()).data;assert.equal(detail.price,expected);assert.equal(detail.symptoms,'Luồng hoàn chỉnh '+kind);
  await page.locator('#success-modal').waitFor({state:'visible'});assert.match(await page.locator('#success-price').innerText(),new RegExp(String(expected/1000)+'[.\\s]000'));
 }
 assert.deepEqual(errors,[]);console.log('PASS: patient request UI → Admin approval UI → Standard/Insurance/VIP quote+booking UI → snapshot readback, without page errors.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
