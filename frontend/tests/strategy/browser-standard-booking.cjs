const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage();const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';const errors=[];page.on('pageerror',e=>errors.push(e.message));
 const login=await page.request.post(origin+'/api/v1/auth/login',{data:{email:'quote-patient@test.vn',password:'BrowserTest123!'}});assert.equal(login.status(),200);const auth=(await login.json()).data;
 await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},auth);
 await page.goto(origin+'/patient/dashboard.html');
 await page.locator('button[onclick^="openBookingModal"]').first().click();await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
 await page.waitForFunction(()=>consultationQuote.getState().status==='ready');
 await page.locator('#symptoms').fill('Triệu chứng kiểm thử snapshot');
 const created=page.waitForResponse(r=>r.url().endsWith('/appointments/confirm')&&r.request().method()==='POST');
 await page.locator('#btn-confirm-booking').click();const response=await created;assert.equal(response.status(),201);
 const payload=response.request().postDataJSON();assert.equal(payload.strategyKey,'STANDARD');assert.equal(payload.expectedPrice,300000);
 const appointment=(await response.json()).data;assert.equal(appointment.pricingKey,'STANDARD');assert.equal(appointment.price,300000);
 await page.waitForFunction(()=>document.getElementById('success-price').textContent.includes('300'));
 assert.match(await page.locator('#success-price').innerText(),/300[.\s]000/);
 const detail=await page.request.get(origin+'/api/v1/appointments/'+appointment.id,{headers:{Authorization:'Bearer '+auth.accessToken}});
 const saved=(await detail.json()).data;assert.equal(saved.basePrice,300000);assert.equal(saved.price,300000);assert.equal(saved.symptoms,'Triệu chứng kiểm thử snapshot');
 assert.deepEqual(errors,[]);console.log('PASS: browser quote → strict confirm → stored Standard snapshot → displayed agreed price and API readback.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
