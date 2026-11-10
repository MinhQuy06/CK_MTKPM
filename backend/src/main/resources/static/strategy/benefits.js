'use strict';
const benefitUser = Auth.checkAuth(['PATIENT']);
const benefitLabels = { PENDING:'Chờ duyệt', APPROVED:'Đã duyệt', REJECTED:'Từ chối', REVOKED:'Đã thu hồi', EXPIRED:'Đã hết hạn', NOT_YET_VALID:'Chưa đến ngày hiệu lực', INVALID:'Thiếu dữ liệu hiệu lực' };
async function loadBenefitRequests() {
  const container=document.getElementById('benefit-list');
  try {
    const response=await Api.benefits.mine();
    container.replaceChildren();
    if (!response.data.length) { container.textContent='Bạn chưa có yêu cầu nào.'; return; }
    response.data.forEach(item => {
      const article=document.createElement('article');article.className='border rounded p-3 mb-2';article.dataset.requestId=item.id;
      const title=document.createElement('h3');title.className='h6';
      title.textContent=BenefitKinds.of(item.kind).label+' — '+(benefitLabels[item.effectiveStatus]||benefitLabels[item.status]||item.status);
      const detail=document.createElement('p');detail.className='mb-0';
      detail.textContent=BenefitKinds.of(item.kind).shortReferenceLabel+': '+item.referenceCode+' · Đề nghị: '+item.requestedFrom+' → '+item.requestedUntil;
      article.append(title,detail);
      if(item.approvedFrom) { const valid=document.createElement('p');valid.textContent='Hiệu lực đã duyệt: '+item.approvedFrom+' → '+item.approvedUntil;article.append(valid); }
      (item.decisions||[]).forEach(d=>{const history=document.createElement('p');history.className='small mb-1';history.textContent=d.action+': '+d.reason+' · '+d.decidedAt;article.append(history);});
      if(item.renewalOfId) {const original=document.createElement('p');original.textContent='Gia hạn từ yêu cầu #'+item.renewalOfId;article.append(original);}
      if(['APPROVED','REVOKED'].includes(item.status)) {
        const renew=document.createElement('button');renew.type='button';renew.className='btn btn-outline-primary benefit-renew';renew.textContent='Gửi yêu cầu gia hạn';
        renew.addEventListener('click',()=>{
          document.getElementById('benefit-renewal').value=item.id;
          document.getElementById('benefit-kind').value=item.kind;showReferenceGuidance();
          document.getElementById('benefit-reference').value=item.referenceCode;
          document.getElementById('benefit-from').value='';document.getElementById('benefit-until').value='';
          document.getElementById('benefit-message').textContent='Đang đề nghị gia hạn yêu cầu #'+item.id+'. Chọn thời hạn mới rồi gửi để được duyệt.';
          document.getElementById('benefit-form').scrollIntoView({behavior:'smooth'});
        });article.append(renew);
      }
      container.append(article);
    });
  } catch(error) { container.textContent=error.message; }
}
// Tells the patient which external code to enter for the selected benefit kind.
function showReferenceGuidance() {
  const kind=BenefitKinds.of(document.getElementById('benefit-kind').value);
  document.querySelector('label[for="benefit-reference"]').textContent=kind.referenceLabel;
  document.getElementById('benefit-reference').placeholder=kind.placeholder;
  document.getElementById('benefit-reference-hint').textContent=kind.hint;
}
document.getElementById('benefit-kind').addEventListener('change',showReferenceGuidance);
showReferenceGuidance();
document.getElementById('benefit-reload').addEventListener('click',loadBenefitRequests);
document.getElementById('benefit-form').addEventListener('submit',async event=>{
  event.preventDefault();
  const button=document.getElementById('benefit-submit'),message=document.getElementById('benefit-message');
  button.disabled=true;message.textContent='';
  try {
    await Api.benefits.submit({
      ...(document.getElementById('benefit-renewal').value ? {renewalOfId:Number(document.getElementById('benefit-renewal').value)} : {}),
      kind:document.getElementById('benefit-kind').value,
      referenceCode:document.getElementById('benefit-reference').value,
      requestedFrom:document.getElementById('benefit-from').value,
      requestedUntil:document.getElementById('benefit-until').value
    });
    document.getElementById('benefit-renewal').value='';
    message.textContent='Đã gửi yêu cầu. Vui lòng chờ phòng khám xác minh.';
    await loadBenefitRequests();
  } catch(error) { message.textContent=error.message; }
  finally { button.disabled=false; }
});
if(benefitUser) loadBenefitRequests();
