const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage(),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>(await(await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}})).json()).data;
 const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');
 await page.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},patient);
 const submitted=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:{Authorization:'Bearer '+patient.accessToken},data:{kind:'INSURANCE',referenceCode:'PRECISE',requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'}});assert.equal(submitted.status(),201);const id=(await submitted.json()).data.id;
 assert.equal((await page.request.post(origin+'/api/v1/admin/benefit-requests/'+id+'/decision',{headers:{Authorization:'Bearer '+admin.accessToken},data:{action:'APPROVE',reason:'Đối chiếu',approvedFrom:'2026-01-01',approvedUntil:'2027-12-31'}})).status(),200);
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
 await page.waitForFunction(()=>!document.getElementById('pricing-kind').disabled);await page.locator('#pricing-kind').selectOption('INSURANCE');await page.waitForFunction(()=>consultationQuote.getState().quote?.strategyKey==='INSURANCE');
 assert.equal(await page.evaluate(()=>consultationQuote.getState().quote.discountRateExact),'0.123456789123456789');
 const done=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm'));
 await page.locator('#btn-confirm-booking').click();const response=await done;assert.equal(response.request().postDataJSON().expectedDiscountRate,'0.123456789123456789');assert.equal(response.status(),201);assert.equal((await response.json()).data.price,262963);
 console.log('PASS: actual 18-digit configured rate survives API → browser → strict confirmation; agreed 262963 VND.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
