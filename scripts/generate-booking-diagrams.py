
"""Generate the booking-only Strategy Class and Sequence diagrams."""
from pathlib import Path
import xml.etree.ElementTree as ET
from drawio import Diagram,OUT
root=Path(__file__).resolve().parents[1]
discounted='lockedEligible' in (root/'backend/src/main/java/com/datlichkhambenh/backend/service/BenefitService.java').read_text()
d=Diagram('Strategy booking — Class',2150,1510)
d.text('title','APPOINTMENT BOOKING — STRATEGY CLASS DIAGRAM',40,15,2050,45,23)
d.cls('controller','«REST Controller»\nBookingController',
 '+ createBooking(actor, request): legacy POST /appointments\n+ confirmBooking(actor, request): POST /appointments/confirm\n\nJWT + PATIENT; strict route requires quote fields.\nAuthenticated patient ID comes from principal.',40,120,540,230)
d.cls('service','«Client / Transaction boundary»\nBookingServiceImpl',
 '- schedules: ScheduleRepository\n- appointments: AppointmentRepository\n- pricing: PricingContext\n\n+ createBooking(patientId, request): AppointmentDto\n- confirmedPrice(request, doctor): PricingAmounts\n\nLock slot; validate; calculate and compare quote;\nsave snapshot and unavailable slot; notify after commit.\n'+('Verify and lock eligible benefit for discounts.' if discounted else 'This stage accepts STANDARD booking only.'),40,470,540,365)
d.cls('context','«Context»\nPricingContext',
 '- strategies: Map<String, ConsultationPricingStrategy>\n+ calculate(key, fee): PricingAmounts\n+ supports(key): boolean\n\nRegistered algorithms; common monetary validation\nand HALF_UP VND normalization.',760,470,470,245)
d.cls('interface','«interface / Strategy»\nConsultationPricingStrategy',
 '+ key(): String\n+ name(): String\n+ discountRate(): BigDecimal\n+ calculate(basePrice: BigDecimal): BigDecimal\n+ simulated(): boolean',1380,120,700,230)
for i,(key,name,rate) in enumerate([('std','StandardPricingStrategy','0'),('ins','InsurancePricingStrategy','0.8'),('vip','VipPricingStrategy','0.1')]):
 x,y=[(1380,470),(1740,470),(1740,850)][i]
 d.cls(key,'«Concrete Strategy»\n'+name,
       '+ calculate(basePrice): BigDecimal\n+ key / name / discountRate / simulated\n\nbasePrice × (1 − discountRate)\nDefault discount = '+rate+'\nRate configurable in [0,1].',x,y,340,255)
 d.edge(key,'interface','implements','dashed=1;endArrow=block;endFill=0;exitX='+('1' if i==2 else '0.5')+';exitY='+('0.1' if i==2 else '0')+';entryX='+str([.24,.76,1][i])+';entryY='+('0.5' if i==2 else '1')+';')
d.cls('repo','«Repository interfaces»\nScheduleRepository / AppointmentRepository',
 '+ findByIdForUpdate(id): Optional<Schedule>\n+ existsByScheduleId(id): boolean\n+ save(entity)\n\nPESSIMISTIC_WRITE on Schedule; unique schedule_id\nprovides an additional database constraint.',40,1040,540,225)
d.cls('snapshot','«Persisted entity / response snapshot»\nAppointment / AppointmentDto',
 'price: agreed final amount\npricingKey: selected Strategy\nbasePrice: normalized original fee\ndiscountRate: agreed rate\n'+('benefitRequestId: verified grant used\n' if discounted else '')+'\nHistorical null metadata is preserved, not invented.',760,1040,470,245)
d.cls('notification','«Existing notification subsystem»\nAsyncEmailNotificationService',
 '+ sendBookingConfirmation(appointment)\n\nCaptures appointment.price in event.\nExisting listener queues email AFTER_COMMIT.\nRollback does not dispatch confirmation.',1380,1190,700,220)
d.edge('controller','service','uses','exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
d.edge('service','context','uses','exitX=1;exitY=0.25;entryX=0;entryY=0.4;')
d.edge('context','interface','1..* registered','exitX=0.7;exitY=0;entryX=0;entryY=0.7;')
d.edge('service','repo','lock / read / save','exitX=0.3;exitY=1;entryX=0.3;entryY=0;')
d.edge('service','snapshot','«create»','dashed=1;exitX=0.9;exitY=1;entryX=0.1;entryY=0;')
notification_edge=d.edge('service','notification','existing event flow','dashed=1;exitX=1;exitY=0.9;entryX=0;entryY=0.2;edgeStyle=segmentEdgeStyle;')
points=ET.SubElement(notification_edge.find('mxGeometry'),'Array',{'as':'points'})
for x,y in [(650,800),(650,1000),(1290,1000),(1290,1234)]: ET.SubElement(points,'mxPoint',x=str(x),y=str(y))
if discounted:
 d.cls('benefit','«Supporting eligibility service»\nBenefitService',
       '+ lockedEligible(patientId, kind, day)\n  : BenefitRequest\n\nLocks verified grant until booking commits;\nrevocation takes the same row lock.',760,760,470,225)
 d.edge('service','benefit','checks and locks grant','exitX=1;exitY=0.65;entryX=0;entryY=0.4;')
d.text('scope','Only the Strategy-enabled booking feature is modeled. No separate Admin/benefit workflow, payment or whole-system UML.\nPrice changes → explicit reconfirmation; no partial appointment, consumed slot or confirmation on failure.',40,1430,2040,65,15)
d.save('consultation-booking-class.drawio')

labels=['Patient UI','BookingController','BookingServiceImpl\n«transactional proxy»','ScheduleRepository']
if discounted:labels.append('BenefitService')
labels+=['PricingContext','selectedStrategy\n: ConsultationPricingStrategy','AppointmentRepository','Notification subsystem\n«existing»']
xs=[140+i*275 for i in range(len(labels))]
width=xs[-1]+150
s=Diagram('Strategy booking — Sequence',width+50,2270)
s.text('title','APPOINTMENT BOOKING — STRATEGY SEQUENCE DIAGRAM',30,10,width,45,23)
for i,(label,x) in enumerate(zip(labels,xs)):
 s.box('p'+str(i),label,x-120,80,240,75,'whiteSpace=wrap;fontSize=15;')
 s.line(x,155,x,2160,dashed=True,arrow=False)
ui,controller,service,slot=xs[:4]
index=4
if discounted:benefit=xs[index];index+=1
context,strategy,appointments,notification=xs[index:]
s.box('preconditions','JWT + PATIENT. Strict confirm route sets server-only quoteRequired; old POST keeps documented Standard compatibility.',30,180,width,55,'whiteSpace=wrap;fontSize=15;')
s.line(ui,285,controller,285,'POST /appointments/confirm {slot, kind, expected base/rate/price, symptoms}')
s.line(controller,350,service,350,'createBooking(authenticated patientId, request)')
s.line(service,420,slot,420,'findByIdForUpdate(scheduleId)')
s.line(slot,480,service,480,'locked Schedule',dashed=True)
s.box('guards','Validate available/future slot, no appointment, active approved doctor and patient. Missing slot → 404; unavailable → 409 SLOT_UNAVAILABLE.\nInvalid key / missing strict quote fields → 400; stop and roll back before any write.',40,520,width-30,85,'whiteSpace=wrap;fontSize=15;')
if discounted:
 s.line(service,670,benefit,670,'lockedEligible(patientId, key, appointmentDay) [discount only]')
 s.line(benefit,735,service,735,'verified BenefitRequest, row locked until commit',dashed=True)
 s.box('eligibility-error','No approved grant valid today + appointment day → 403 BENEFIT_UNAVAILABLE, rollback; STANDARD skips this grant check.',500,770,width-500,55,'whiteSpace=wrap;fontSize=14;')
s.line(service,900,context,900,'calculate(key, doctor.consultationFee)')
s.line(context,970,strategy,970,'calculate(normalized basePrice)')
s.line(strategy,1040,context,1040,'calculated amount',dashed=True)
s.box('normalize','Context validates rate and storage limits; HALF_UP base and final VND amounts.',max(40,context-220),1070,590,55,'whiteSpace=wrap;fontSize=14;')
s.line(context,1180,service,1180,'PricingAmounts',dashed=True)
s.box('changed','',30,1230,width,185,'fillColor=none;')
s.text('changed-label','break [expected base/rate/final amount differs]',40,1235,width-50,35)
s.line(service,1320,controller,1320,'BookingRejectionException: PRICE_CHANGED',dashed=True)
s.line(controller,1390,ui,1390,'409; no appointment/slot update/email. Show new quote only on recovery; wait for user.',dashed=True)
s.line(service,1490,appointments,1490,'save(Appointment with pricing snapshot)')
s.line(appointments,1550,service,1550,'saved Appointment',dashed=True)
s.line(service,1620,slot,1620,'save(isAvailable=false)')
s.line(service,1690,notification,1690,'sendBookingConfirmation(savedAppointment) → register event')
s.box('commit','Transactional proxy commits; releases slot'+(' and benefit' if discounted else '')+' locks. Rollback suppresses existing AFTER_COMMIT listener.',440,1750,width-450,60,'whiteSpace=wrap;fontSize=15;')
s.line(service,1880,controller,1880,'AppointmentDto with stored snapshot',dashed=True)
s.line(controller,1950,ui,1950,'201; display agreed price / booking code',dashed=True)
s.box('async-note','After commit, existing listener asynchronously queues the captured price. Delivery has no guaranteed ordering relative to the HTTP response.',max(30,notification-660),2010,760,80,'whiteSpace=wrap;fontSize=14;')
s.text('footer','Scope: Strategy-driven booking and necessary collaborators only. Existing schedules and eligibility are revalidated; client money never becomes the charge source.\nHistorical appointment prices remain unchanged after doctor-fee/configuration/benefit changes.',30,2180,width,65,15)
s.save('consultation-booking-sequence.drawio')
print('Generated booking Class + Sequence (editable monochrome draw.io).')
