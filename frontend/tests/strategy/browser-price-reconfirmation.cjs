const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage();const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>{const r=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return(await r.json()).data;};
 const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');
 await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},patient);
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();
 await page.locator('#slots-container .slot-btn:not([disabled])').first().click();await page.waitForFunction(()=>consultationQuote.getState().status==='ready');
 const doctorId=await page.locator('#selected-doctor-id').inputValue();
 await page.locator('#symptoms').fill('Triệu chứng phải giữ khi giá đổi');
 let attempts=0;await page.route('**/api/v1/appointments/confirm',async route=>{
  attempts++;if(attempts<=2){const changed=await page.request.put(origin+'/api/v1/admin/doctors/'+doctorId,{headers:{Authorization:'Bearer '+admin.accessToken},data:{consultationFee:attempts===1?350000:360000}});assert.equal(changed.status(),200);}
  await route.continue();
 });
 await page.locator('#btn-confirm-booking').click();
 await page.waitForFunction(()=>document.getElementById('price-change-notice') && !document.getElementById('price-change-notice').hidden&&consultationQuote.getState().status==='ready');
 assert.match(await page.locator('#price-change-notice').innerText(),/300[.\s]000/);assert.match(await page.locator('#price-change-notice').innerText(),/350[.\s]000/);
 assert.equal(await page.locator('#symptoms').inputValue(),'Triệu chứng phải giữ khi giá đổi');assert.equal(attempts,1);
 const before=await page.request.get(origin+'/api/v1/appointments/my-appointments',{headers:{Authorization:'Bearer '+patient.accessToken}});assert.equal((await before.json()).data.length,0);
 await page.locator('#btn-confirm-booking').click();
 await page.waitForFunction(()=>document.getElementById('price-change-notice').textContent.includes('360')&&consultationQuote.getState().status==='ready');
 assert.equal(attempts,2);assert.equal(await page.locator('#symptoms').inputValue(),'Triệu chứng phải giữ khi giá đổi');
 const done=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm')&&r.status()===201);
 await page.locator('#btn-confirm-booking').click();const result=(await(await done).json()).data;
 assert.equal(result.price,360000);assert.equal(attempts,3);
 // Cached response fixture represents a quote obtained under an older deployment policy.
 // The confirmation and the refreshed quote still hit the real server and current policy.
 await page.unroute('**/api/v1/appointments/confirm');
 const dates={requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'};
 const submitted=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:{Authorization:'Bearer '+patient.accessToken},data:{kind:'INSURANCE',referenceCode:'RATE-CHANGE',...dates}});
 assert.equal(submitted.status(),201);const grant=(await submitted.json()).data.id;
 assert.equal((await page.request.post(origin+'/api/v1/admin/benefit-requests/'+grant+'/decision',{headers:{Authorization:'Bearer '+admin.accessToken},data:{action:'APPROVE',reason:'Đối chiếu',approvedFrom:dates.requestedFrom,approvedUntil:dates.requestedUntil}})).status(),200);
 let stale=true;await page.route('**/api/v1/appointments/quote?**',async route=>{
  if(stale&&new URL(route.request().url()).searchParams.get('strategyKey')==='INSURANCE'){
   stale=false;const response=await route.fetch();const body=await response.json();body.data.discountRate=0.7;body.data.discountRateExact='0.7';body.data.finalPrice=108000;
   await route.fulfill({response,json:body});
  }else await route.continue();
 });
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();
 await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
 await page.waitForFunction(()=>!document.getElementById('pricing-kind').disabled);
 await page.locator('#pricing-kind').selectOption('INSURANCE');
 await page.waitForFunction(()=>consultationQuote.getState().status==='ready'&&consultationQuote.getState().quote.discountRate===0.7);
 await page.locator('#symptoms').fill('Giữ triệu chứng khi tỷ lệ đổi');
 await page.locator('#btn-confirm-booking').click();
 await page.waitForFunction(()=>!document.getElementById('price-change-notice').hidden&&consultationQuote.getState().quote?.discountRate===0.8);
 assert.match(await page.locator('#price-change-notice').innerText(),/70%.*80%/);assert.equal(await page.locator('#symptoms').inputValue(),'Giữ triệu chứng khi tỷ lệ đổi');
 const policyDone=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm')&&r.status()===201);
 await page.locator('#btn-confirm-booking').click();assert.equal((await(await policyDone).json()).data.price,72000);
 console.log('PASS: two real fee changes require two explicit reconfirmations; no auto-booking; symptoms preserved; final snapshot 360000 VND.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
