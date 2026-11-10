const {chromium}=require('playwright');
const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({channel:'chrome',headless:true});
 try {
  const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
  const response=await page.request.post(origin+'/api/v1/auth/login',{data:{email:'quote-patient@test.vn',password:'BrowserTest123!'}});
  assert.equal(response.status(),200);
  const auth=(await response.json()).data;
  await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},auth);
  await page.goto(origin+'/patient/benefits.html');
  // The reference field explains which external code to enter for each benefit kind.
  await page.locator('#benefit-kind').selectOption('INSURANCE');
  assert.match(await page.locator('label[for="benefit-reference"]').innerText(),/Số thẻ BHYT/);
  assert.match(await page.locator('#benefit-reference').getAttribute('placeholder'),/DN4010123456789/);
  assert.match(await page.locator('#benefit-reference-hint').innerText(),/in trên thẻ/);
  await page.locator('#benefit-kind').selectOption('VIP');
  assert.match(await page.locator('label[for="benefit-reference"]').innerText(),/Mã thẻ thành viên VIP/);
  assert.match(await page.locator('#benefit-reference-hint').innerText(),/lễ tân/);
  await page.locator('#benefit-reference').fill('HS-BROWSER-9');
  await page.locator('#benefit-from').fill('2026-01-01');
  await page.locator('#benefit-until').fill('2027-12-31');
  const created=page.waitForResponse(r=>r.url().endsWith('/patients/me/benefit-requests')&&r.request().method()==='POST');
  await page.locator('#benefit-submit').click();assert.equal((await created).status(),201);
  await page.waitForFunction(()=>document.getElementById('benefit-list').textContent.includes('HS-BROWSER-9'));
  assert.match(await page.locator('#benefit-list').innerText(),/Chờ duyệt/);
  assert.match(await page.locator('#benefit-list').innerText(),/Mã thẻ VIP: HS-BROWSER-9/);
  assert.deepEqual(errors,[]);
  console.log('PASS: patient login → benefit form → pending request persisted/read through real API; no page errors.');
 } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
