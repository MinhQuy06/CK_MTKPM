const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage(),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const login=async email=>(await(await page.request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}})).json()).data;
 const patient=await login('quote-patient@test.vn'),doctor=await login('quote-doctor-1@test.vn');
 await page.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},patient);
 let edited=false;
 await page.route('**/api/v1/appointments/quote?**',async route=>{
  if(!edited){
   edited=true;const id=new URL(route.request().url()).searchParams.get('scheduleId');
   const workDate=await page.locator('#booking-date').inputValue();
   const changed=await page.request.put(origin+'/api/v1/doctors/slots/'+id,{headers:{Authorization:'Bearer '+doctor.accessToken},data:{workDate,startTime:'11:00:00',endTime:'11:30:00'}});assert.equal(changed.status(),200);
  }
  await route.continue();
 });
 await page.goto(origin+'/patient/dashboard.html');await page.locator('button[onclick^="openBookingModal"]').first().click();
 await page.locator('#symptoms').fill('Giữ triệu chứng khi ca đổi trước báo giá');
 await page.locator('#slots-container .slot-btn:not([disabled])').first().click();
 await page.waitForFunction(()=>document.getElementById('selected-schedule-id').value==='');
 assert.equal(await page.locator('#btn-confirm-booking').isDisabled(),true);
 assert.equal(await page.locator('#symptoms').inputValue(),'Giữ triệu chứng khi ca đổi trước báo giá');
 assert.match(await page.locator('#booking-recovery-notice').innerText(),/ca khám/);
 console.log('PASS: slot changed between selection and quote → explicit reselection, no misleading old time or automatic booking.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
