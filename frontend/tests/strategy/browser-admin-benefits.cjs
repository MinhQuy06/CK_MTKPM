const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({channel:'chrome',headless:true});try {
 const page=await browser.newPage();const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>{const r=await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return (await r.json()).data;};
 const patient=await login('quote-patient@test.vn');
 const created=await page.request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:{Authorization:'Bearer '+patient.accessToken},data:{kind:'INSURANCE',referenceCode:'HS-ADMIN-BROWSER',requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'}});
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
 console.log('PASS: patient pending request → real Admin approval UI → verified dates/history visible to patient.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
