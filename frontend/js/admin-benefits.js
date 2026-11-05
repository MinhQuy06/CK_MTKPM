'use strict';
const benefitAdmin=Auth.checkAuth(['ADMIN']);
function adminBenefitNode(tag,cls,text) {
 const element=document.createElement(tag);element.className=cls||'';if(text!==undefined)element.textContent=text;return element;
}
async function loadAdminBenefits() {
 const list=document.getElementById('admin-benefit-list');
 try {
  const response=await Api.benefits.adminList();list.replaceChildren();
  if(!response.data.length){list.textContent='Chưa có yêu cầu.';return;}
  response.data.forEach(item=>{
   const row=adminBenefitNode('article','card p-3 mb-3');row.dataset.requestId=item.id;
   row.append(adminBenefitNode('h2','h5',item.patientName+' — '+item.kind+' — '+item.status));
   row.append(adminBenefitNode('p','', 'Mã hồ sơ: '+item.referenceCode+' · Đề nghị: '+item.requestedFrom+' → '+item.requestedUntil));
   (item.decisions||[]).forEach(d=>row.append(adminBenefitNode('p','small mb-1',d.action+': '+d.reason+' · Admin #'+d.decidedBy+' · '+d.decidedAt)));
   if(item.status==='PENDING') {
    const form=adminBenefitNode('div','row g-2 mt-2');
    const from=adminBenefitNode('input','form-control decision-from'),until=adminBenefitNode('input','form-control decision-until');
    from.type=until.type='date';from.value=item.requestedFrom;until.value=item.requestedUntil;
    from.setAttribute('aria-label','Ngày bắt đầu quyền hưởng');until.setAttribute('aria-label','Ngày kết thúc quyền hưởng');
    const reason=adminBenefitNode('input','form-control decision-reason');reason.maxLength=1000;reason.placeholder='Lý do quyết định';reason.setAttribute('aria-label','Lý do quyết định');
    form.append(from,until,reason);
    ['APPROVE','REJECT'].forEach(action=>{
     const button=adminBenefitNode('button','btn '+(action==='APPROVE'?'btn-success benefit-approve':'btn-outline-danger benefit-reject'),action==='APPROVE'?'Duyệt quyền hưởng':'Từ chối');
     button.type='button';button.addEventListener('click',async()=>{
      button.disabled=true;
      try {await Api.benefits.decide(item.id,{action,reason:reason.value,approvedFrom:from.value||null,approvedUntil:until.value||null});document.getElementById('admin-benefit-message').textContent='Đã lưu quyết định.';await loadAdminBenefits();}
      catch(error){document.getElementById('admin-benefit-message').textContent=error.message;}
      finally{button.disabled=false;}
     });form.append(button);
    });row.append(form);
   }
   if(item.approvedFrom)row.append(adminBenefitNode('p','small','Hiệu lực đã duyệt: '+item.approvedFrom+' → '+item.approvedUntil));
   if(item.status==='APPROVED') {
    const reason=adminBenefitNode('input','form-control revoke-reason mb-2');reason.placeholder='Lý do thu hồi';reason.setAttribute('aria-label','Lý do thu hồi');reason.maxLength=1000;
    const button=adminBenefitNode('button','btn btn-outline-danger benefit-revoke','Thu hồi quyền hưởng');button.type='button';
    button.addEventListener('click',async()=>{button.disabled=true;try{await Api.benefits.revoke(item.id,{reason:reason.value});document.getElementById('admin-benefit-message').textContent='Đã thu hồi và lưu lịch sử.';await loadAdminBenefits();}catch(error){document.getElementById('admin-benefit-message').textContent=error.message;}finally{button.disabled=false;}});
    row.append(reason,button);
   }
   list.append(row);
  });
 }catch(error){list.textContent=error.message;}
}
document.getElementById('admin-benefit-reload').addEventListener('click',loadAdminBenefits);
if(benefitAdmin)loadAdminBenefits();
