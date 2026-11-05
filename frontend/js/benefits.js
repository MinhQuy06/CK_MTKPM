'use strict';
const benefitUser = Auth.checkAuth(['PATIENT']);
const benefitLabels = { PENDING:'Chờ duyệt', APPROVED:'Đã duyệt', REJECTED:'Từ chối', REVOKED:'Đã thu hồi' };
async function loadBenefitRequests() {
  const container=document.getElementById('benefit-list');
  try {
    const response=await Api.benefits.mine();
    container.replaceChildren();
    if (!response.data.length) { container.textContent='Bạn chưa có yêu cầu nào.'; return; }
    response.data.forEach(item => {
      const article=document.createElement('article');article.className='border rounded p-3 mb-2';article.dataset.requestId=item.id;
      const title=document.createElement('h3');title.className='h6';
      title.textContent=(item.kind==='INSURANCE'?'Bảo hiểm':'VIP')+' — '+(benefitLabels[item.status]||item.status);
      const detail=document.createElement('p');detail.className='mb-0';
      detail.textContent='Mã hồ sơ: '+item.referenceCode+' · Đề nghị: '+item.requestedFrom+' → '+item.requestedUntil;
      article.append(title,detail);
      if(item.approvedFrom) { const valid=document.createElement('p');valid.textContent='Hiệu lực đã duyệt: '+item.approvedFrom+' → '+item.approvedUntil;article.append(valid); }
      (item.decisions||[]).forEach(d=>{const history=document.createElement('p');history.className='small mb-1';history.textContent=d.action+': '+d.reason+' · '+d.decidedAt;article.append(history);});
      container.append(article);
    });
  } catch(error) { container.textContent=error.message; }
}
document.getElementById('benefit-reload').addEventListener('click',loadBenefitRequests);
document.getElementById('benefit-form').addEventListener('submit',async event=>{
  event.preventDefault();
  const button=document.getElementById('benefit-submit'),message=document.getElementById('benefit-message');
  button.disabled=true;message.textContent='';
  try {
    await Api.benefits.submit({
      kind:document.getElementById('benefit-kind').value,
      referenceCode:document.getElementById('benefit-reference').value,
      requestedFrom:document.getElementById('benefit-from').value,
      requestedUntil:document.getElementById('benefit-until').value
    });
    message.textContent='Đã gửi yêu cầu. Vui lòng chờ phòng khám xác minh.';
    await loadBenefitRequests();
  } catch(error) { message.textContent=error.message; }
  finally { button.disabled=false; }
});
if(benefitUser) loadBenefitRequests();
