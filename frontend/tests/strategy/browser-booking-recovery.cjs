const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage(),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>{const r=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return(await r.json()).data;};
 const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');
 const auth=token=>({Authorization:'Bearer '+token});
 await page.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},patient);
 const submitted=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:auth(patient.accessToken),data:{kind:'INSURANCE',referenceCode:'RECOVERY',requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'}});
 assert.equal(submitted.status(),201);const id=(await submitted.json()).data.id;
 assert.equal((await page.request.post(origin+'/api/v1/admin/benefit-requests/'+id+'/decision',{headers:auth(admin.accessToken),data:{action:'APPROVE',reason:'Xác minh hồ sơ',approvedFrom:'2026-01-01',approvedUntil:'2027-12-31'}})).status(),200);
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();
 await page.locator('#slots-container .slot-btn:not([disabled])').first().click();await page.waitForFunction(()=>!document.getElementById('pricing-kind').disabled);
 await page.locator('#pricing-kind').selectOption('INSURANCE');await page.waitForFunction(()=>consultationQuote.getState().quote?.strategyKey==='INSURANCE');
 await page.locator('#symptoms').fill('Giữ nguyên triệu chứng qua mọi phục hồi');
 assert.equal((await page.request.post(origin+'/api/v1/admin/benefit-requests/'+id+'/revoke',{headers:auth(admin.accessToken),data:{reason:'Thu hồi trước xác nhận'}})).status(),200);
 let posts=0;page.on('request',r=>{if(r.url().endsWith('/appointments/confirm')&&r.method()==='POST')posts++;});
 await page.locator('#btn-confirm-booking').click();
 await page.waitForFunction(()=>document.getElementById('booking-recovery-notice')?.textContent.includes('quyền'));
 await page.waitForFunction(()=>!document.getElementById('pricing-kind').disabled&&document.getElementById('pricing-kind').value==='');
 assert.equal(posts,1);assert.equal(await page.locator('#pricing-kind option[value="INSURANCE"]').count(),0);
 assert.equal(await page.locator('#btn-confirm-booking').isDisabled(),true);
 assert.equal(await page.locator('#symptoms').inputValue(),'Giữ nguyên triệu chứng qua mọi phục hồi');
 // Explicit choice, never an automatic fallback to Standard.
 await page.locator('#pricing-kind').selectOption('STANDARD');await page.waitForFunction(()=>consultationQuote.getState().status==='ready');
 // Another committed booking takes the quoted slot; the UI's original confirmation must fail.
 const q=await page.evaluate(()=>consultationQuote.getState().quote);
 const occupied=await page.request.post(origin+'/api/v1/appointments/confirm',{headers:auth(patient.accessToken),data:{scheduleId:q.scheduleId,symptoms:'Concurrent booking',strategyKey:q.strategyKey,expectedWorkDate:q.workDate,expectedStartTime:q.startTime,expectedEndTime:q.endTime,expectedBasePrice:q.basePrice,expectedDiscountRate:q.discountRateExact??q.discountRate,expectedPrice:q.finalPrice}});assert.equal(occupied.status(),201);
 await page.locator('#btn-confirm-booking').click();
 await page.waitForFunction(()=>document.getElementById('booking-recovery-notice').textContent.includes('ca khám'));
 await page.waitForFunction(()=>document.getElementById('selected-schedule-id').value===''&&document.querySelector('#slots-container .slot-btn:not([disabled])'));
 assert.equal(posts,2);assert.equal(await page.locator('#btn-confirm-booking').isDisabled(),true);
 assert.equal(await page.locator('#symptoms').inputValue(),'Giữ nguyên triệu chứng qua mọi phục hồi');
 assert.equal(await page.locator('#slots-container .slot-btn[onclick="selectSlot(this, '+q.scheduleId+')"]').count(),0);
 await page.locator('#slots-container .slot-btn:not([disabled])').first().click();await page.waitForFunction(()=>consultationQuote.getState().status==='ready');
 const done=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm')&&r.status()===201);
 await page.locator('#btn-confirm-booking').click();const saved=(await(await done).json()).data;
 assert.notEqual(saved.scheduleId,q.scheduleId);assert.equal(saved.price,300000);assert.equal(saved.symptoms,'Giữ nguyên triệu chứng qua mọi phục hồi');assert.equal(posts,3);
 console.log('PASS: real revocation → explicit valid pricing selection → occupied slot → new explicit slot → saved snapshot; symptoms preserved; no blind retry.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
