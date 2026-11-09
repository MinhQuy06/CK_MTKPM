const {chromium}=require('playwright');const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({channel:'chrome',headless:true});try{
 const page=await browser.newPage({timezoneId:'Pacific/Honolulu'}),origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
 const today=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Ho_Chi_Minh',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
 const tomorrow=new Date(today+'T00:00:00Z');tomorrow.setUTCDate(tomorrow.getUTCDate()+1);
 const response=await page.request.post(origin+'/api/v1/auth/login',{data:{email:'quote-patient@test.vn',password:'BrowserTest123!'}});assert.equal(response.status(),200);const auth=(await response.json()).data;
 await page.addInitScript(a=>{localStorage.setItem('accessToken',a.accessToken);localStorage.setItem('refreshToken',a.refreshToken);localStorage.setItem('user',JSON.stringify(a));},auth);
 await page.clock.install({time:new Date(today+'T00:30:00+07:00')});
 await page.goto(origin+'/patient/dashboard.html');assert.equal(await page.locator('#booking-date').inputValue(),tomorrow.toISOString().slice(0,10));
 await page.locator('button[onclick^="openBookingModal"]').first().click();await page.locator('#slots-container .slot-btn:not([disabled])').first().waitFor();
 console.log('PASS: default date follows Vietnam tomorrow at 00:30 VN even in Honolulu browser timezone.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exitCode=1;});
