const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage(),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>(await(await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}})).json()).data;
 const patient=await login('quote-patient@test.vn'),doctor=await login('quote-doctor-1@test.vn');
 await page.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},patient);
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();await page.locator('#slots-container .slot-btn:not([disabled])').first().click();await page.waitForFunction(()=>consultationQuote.getState().status==='ready');
 const q=await page.evaluate(()=>consultationQuote.getState().quote);await page.locator('#symptoms').fill('Giữ khi bác sĩ xóa ca');
 const deleted=await page.request.delete(origin+'/api/v1/doctors/slots/'+q.scheduleId,{headers:{Authorization:'Bearer '+doctor.accessToken}});assert.equal(deleted.status(),200);
 let attempts=0;page.on('request',r=>{if(r.url().endsWith('/appointments/confirm'))attempts++;});
 await page.locator('#btn-confirm-booking').click();await page.waitForFunction(()=>document.getElementById('selected-schedule-id').value==='');
 assert.equal(attempts,1);assert.equal(await page.locator('#btn-confirm-booking').isDisabled(),true);assert.equal(await page.locator('#symptoms').inputValue(),'Giữ khi bác sĩ xóa ca');
 await page.locator('#slots-container .slot-btn:not([disabled])').first().waitFor();
 assert.equal(await page.locator('#slots-container .slot-btn[onclick="selectSlot(this, '+q.scheduleId+')"]').count(),0);
 console.log('PASS: doctor deletes quoted slot → clear stale quote/selection, refresh remaining slots, retain symptoms, no repeated booking.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
