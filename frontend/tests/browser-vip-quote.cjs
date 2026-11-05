const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({channel:'chrome',headless:true});try {
 const page=await browser.newPage();const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>{const r=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return (await r.json()).data;};
 const patient=await login('quote-patient@test.vn');
 const created=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:{Authorization:'Bearer '+patient.accessToken},data:{kind:'VIP',referenceCode:'HS-ADMIN-BROWSER',requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'}});
 assert.equal(created.status(),201);const id=(await created.json()).data.id;
 const auth=await login('quote-admin@test.vn');
 await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},auth);
 await page.goto(origin+'/admin/benefits.html');
 const row=page.locator('[data-request-id="'+id+'"]');await row.waitFor();
 await row.locator('.decision-reason').fill('Đối chiếu hồ sơ bản gốc');
 const result=page.waitForResponse(r=>r.url().endsWith('/'+id+'/decision'));
 await row.locator('.benefit-approve').click();assert.equal((await result).status(),200);
 await page.waitForFunction(id=>document.querySelector('[data-request-id="'+id+'"]').textContent.includes('APPROVED'),id);
 const read=await page.request.get(origin+'/api/v1/patients/me/benefit-requests/'+id,{headers:{Authorization:'Bearer '+patient.accessToken}});
 const data=(await read.json()).data;assert.equal(data.status,'APPROVED');assert.equal(data.decisions[0].reason,'Đối chiếu hồ sơ bản gốc');
 const patientPage=await browser.newPage();
 await patientPage.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},patient);
 await patientPage.goto(origin+'/patient/dashboard.html');
 await patientPage.locator('button[onclick^="openBookingModal"]').first().click();
 await patientPage.locator('#slots-container .slot-btn:not([disabled])').first().click();
 await patientPage.waitForFunction(()=>document.querySelector('#pricing-kind option[value="VIP"]'));
 await patientPage.locator('#pricing-kind').selectOption('VIP');
 await patientPage.waitForFunction(()=>consultationQuote.getState().status==='ready'&&consultationQuote.getState().quote.strategyKey==='VIP');
 assert.match(await patientPage.locator('#quote-final').innerText(),/270[.\s]000/);
 assert.equal(await patientPage.locator('#quote-kind').innerText(),'VIP');
 console.log('PASS: verified VIP grant → eligible option in patient UI → real server quote 270000 VND.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
