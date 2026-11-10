const {chromium}=require('playwright');
const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({channel:'chrome',headless:true});
 try {
  const origin=process.env.QUOTE_TEST_ORIGIN||'http://localhost:18081';
  const request=(await browser.newContext()).request;
  const login=async email=>{const r=await request.post(origin+'/api/v1/auth/login',{data:{email,password:'BrowserTest123!'}});assert.equal(r.status(),200);return (await r.json()).data;};
  const asUser=async auth=>{const page=await (await browser.newContext()).newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
   await page.addInitScript(auth=>{localStorage.setItem('accessToken',auth.accessToken);localStorage.setItem('refreshToken',auth.refreshToken);localStorage.setItem('user',JSON.stringify(auth));},auth);
   return {page,errors};};
  const patient=await login('quote-patient@test.vn'),admin=await login('quote-admin@test.vn');

  // Every patient page links to the benefit page from its sidebar.
  const p=await asUser(patient);
  for(const name of ['dashboard','appointments','medical-records','profile','doctors','benefits']){
   await p.page.goto(origin+'/patient/'+name+'.html');
   const link=p.page.locator('[data-strategy-nav]');
   assert.equal(await link.count(),1,'patient/'+name);
   assert.match(await link.getAttribute('href'),/benefits\.html$/);
  }
  assert.equal(await p.page.locator('[data-strategy-nav].active').count(),1,'benefit page marks its own nav item active');

  // Without an active benefit the dashboard says Standard pricing applies and points to verification.
  await p.page.goto(origin+'/patient/dashboard.html');
  const card=p.page.locator('#benefit-summary');
  await p.page.waitForFunction(()=>!document.getElementById('benefit-summary').hidden);
  assert.match(await card.innerText(),/Tiêu chuẩn/);
  assert.match(await card.locator('a[href="benefits.html"]').innerText(),/Gửi yêu cầu xác minh/);

  // A pending request is mentioned; once approved, the dashboard lists the active benefit.
  const auth=t=>({Authorization:'Bearer '+t});
  const created=await request.post(origin+'/api/v1/patients/me/benefit-requests',{headers:auth(patient.accessToken),data:{kind:'VIP',referenceCode:'NAV-VIP',requestedFrom:'2026-01-01',requestedUntil:'2027-12-31'}});
  assert.equal(created.status(),201);const id=(await created.json()).data.id;
  await p.page.reload();await p.page.waitForFunction(()=>/chờ duyệt/.test(document.getElementById('benefit-summary').innerText));
  const decided=await request.post(origin+'/api/v1/admin/benefit-requests/'+id+'/decision',{headers:auth(admin.accessToken),data:{action:'APPROVE',reason:'Đã đối chiếu',approvedFrom:'2026-01-01',approvedUntil:'2027-12-31'}});
  assert.equal(decided.status(),200);
  await p.page.reload();await p.page.waitForFunction(()=>/VIP/.test(document.getElementById('benefit-summary').innerText));
  const text=await card.innerText();
  assert.match(text,/31\/12\/2027/);assert.doesNotMatch(text,/Tiêu chuẩn/);
  assert.match(await card.locator('a[href="benefits.html"]').innerText(),/Quản lý quyền hưởng/);
  assert.deepEqual(p.errors,[]);

  // Every admin page links to the verification page; the old button inside user management is gone.
  const a=await asUser(admin);
  for(const name of ['dashboard','users','doctors','specialties','analytics','services','benefits']){
   await a.page.goto(origin+'/admin/'+name+'.html');
   assert.equal(await a.page.locator('[data-strategy-nav]').count(),1,'admin/'+name);
  }
  await a.page.goto(origin+'/admin/users.html');
  assert.equal(await a.page.locator('main a.btn[href="benefits.html"], body > div > div > a.btn[href="benefits.html"]').count(),0);
  console.log('PASS: Strategy reachable from every patient/admin sidebar; dashboard shows Standard → pending → active VIP.');
 } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
