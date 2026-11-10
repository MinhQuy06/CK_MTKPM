
"""Generate the booking-only Strategy Class and Sequence diagrams."""
from pathlib import Path
import xml.etree.ElementTree as ET
from drawio import Diagram,OUT
root=Path(__file__).resolve().parents[2]
discounted='lockCovering' in (root/'backend/src/main/java/com/datlichkhambenh/backend/strategy/service/BenefitService.java').read_text()
d=Diagram('Strategy booking — Class',2150,1510)
d.text('title','APPOINTMENT BOOKING — STRATEGY CLASS DIAGRAM',40,15,2050,45,23)
d.cls('controller','«REST Controller → Facade»\nBookingController → BookingFacade',
 '+ confirmBooking: POST /appointments/confirm\n    Facade builds ConfirmedQuote (price + slot time)\n+ createBooking: legacy POST /appointments\n    Facade builds LegacyQuote or LegacyStandard\nJWT + PATIENT; patient ID from principal.',40,120,540,230)
d.cls('service','«Transaction boundary» BookingServiceImpl\n+ «Client» BookingOffer',
 'BookingServiceImpl.createBooking(patientId, scheduleId,\n    symptoms, expectation): AppointmentDto\n  offers.lockAndConfirm(...) → LockedOffer; save snapshot,\n  close slot, publish AppointmentBookedEvent\n\nBookingOffer.lockAndConfirm(patientId, scheduleId, expectation)\n  lock slot → doctor → benefit; check bookable slot,\n  supported key, covering benefit; compare Expectation;\n  refuse only with BookingRejection codes',40,470,540,365)
d.cls('context','«Context»\nPricingContext',
 '- strategies: Map<String, ConsultationPricingStrategy>\n+ calculate(key, fee): PricingAmounts\n+ supports(key): boolean\n\nRegistered algorithms; common monetary validation\nand HALF_UP VND normalization.',760,470,470,245)
d.cls('interface','«interface / Strategy»\nConsultationPricingStrategy',
 '+ key(): String\n+ name(): String\n+ discountRate(): BigDecimal\n+ calculate(basePrice: BigDecimal): BigDecimal\n+ simulated(): boolean',1380,120,700,230)
for i,(key,name,rate) in enumerate([('std','StandardPricingStrategy','0'),('ins','InsurancePricingStrategy','0.8'),('vip','VipPricingStrategy','0.1')]):
 x,y=[(1380,470),(1740,470),(1740,850)][i]
 d.cls(key,'«Concrete Strategy»\n'+name,
       '+ calculate(basePrice): BigDecimal\n+ key / name / discountRate / simulated\n\nbasePrice × (1 − discountRate)\nDefault discount = '+rate+'\nRate configurable in [0,1].',x,y,340,255)
 d.edge(key,'interface','implements','dashed=1;endArrow=block;endFill=0;exitX='+('1' if i==2 else '0.5')+';exitY='+('0.1' if i==2 else '0')+';entryX='+str([.24,.76,1][i])+';entryY='+('0.5' if i==2 else '1')+';')
d.cls('repo','«Repository interfaces»\nScheduleRepository / DoctorRepository / AppointmentRepository',
 '+ Schedule.findByIdForUpdate(id): Optional<Schedule>\n+ Doctor.findByIdForUpdate(id): Optional<Doctor>\n+ Appointment.existsByScheduleId / save\n\nSlot and fee remain locked until commit;\nunique schedule_id also prevents duplicates.',40,1040,540,225)
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
       '+ lockCovering(patientId, kind, day)\n  : Optional<BenefitRequest>\n\nLocks covering benefit until booking commits;\nrevocation takes the same row lock.',760,760,470,225)
 d.edge('service','benefit','checks and locks grant','exitX=1;exitY=0.65;entryX=0;entryY=0.4;')
d.text('scope','Only the Strategy-enabled booking feature is modeled. No separate Admin/benefit workflow, payment or whole-system UML.\nPrice changes → explicit reconfirmation; no partial appointment, consumed slot or confirmation on failure.',40,1430,2040,65,15)
d.save('consultation-booking-class.drawio')


labels=['Patient UI','BookingController\n→ BookingFacade','BookingServiceImpl\n«transactional proxy»','BookingOffer\n«Client»','ScheduleRepository','DoctorRepository']
if discounted:labels.append('BenefitService')
labels+=['PricingContext','selectedStrategy\n: ConsultationPricingStrategy','AppointmentRepository','Notification subsystem\n«existing»']
xs=[140+i*275 for i in range(len(labels))];width=xs[-1]+150
s=Diagram('Strategy booking — Sequence',width+50,2440)
s.text('title','APPOINTMENT BOOKING — STRATEGY SEQUENCE DIAGRAM',30,10,width,45,23)
for i,(label,x) in enumerate(zip(labels,xs)):
 s.box('p'+str(i),label,x-120,80,240,75,'whiteSpace=wrap;fontSize=15;')
 s.line(x,155,x,2290,dashed=True,arrow=False)
ui,controller,service,offer,slot,doctor=xs[:6];index=6
if discounted:benefit=xs[index];index+=1
context,strategy,appointments,notification=xs[index:]
s.box('preconditions','JWT + PATIENT. Strict confirm route requires server-verified quote fields; old POST keeps documented Standard compatibility.',30,180,width,55,'whiteSpace=wrap;fontSize=15;')
s.line(ui,285,controller,285,'POST /appointments/confirm\n(scheduleId, expected day/start/end, key, expected quote, symptoms)')
s.line(controller,330,service,330,'createBooking(patientId, scheduleId, symptoms, ConfirmedQuote)')
s.line(service,385,offer,385,'lockAndConfirm(patientId, scheduleId, expectation)')
s.line(offer,420,slot,420,'findByIdForUpdate(scheduleId)')
s.line(slot,480,offer,480,'locked Schedule',dashed=True)
s.line(offer,550,doctor,550,'findByIdForUpdate(doctorId)')
s.line(doctor,610,offer,610,'locked Doctor with current fee',dashed=True)
s.box('guards','BookingOffer: missing slot → 404 SLOT_UNAVAILABLE; taken / started / changed day-time → 409 SLOT_UNAVAILABLE (UI clears quote, reloads slots, keeps symptoms); doctor not accepting → 409 DOCTOR_UNAVAILABLE.\nUnsupported key → 400 UNSUPPORTED_STRATEGY; missing quote parts → 400 QUOTE_REQUIRED. Every refusal rolls back before any write.',40,650,width-30,85,'whiteSpace=wrap;fontSize=15;')
if discounted:
 s.line(offer,790,benefit,790,'lockCovering(patientId, key, appointmentDay) [discount only]')
 s.line(benefit,850,offer,850,'covering BenefitRequest, row locked until commit',dashed=True)
 s.box('eligibility-error','No approved grant valid today + appointment day → 403 BENEFIT_UNAVAILABLE, rollback. STANDARD skips grant check. UI refreshes permitted types and requires explicit selection; no fallback.',500,875,width-500,75,'whiteSpace=wrap;fontSize=14;')
s.line(offer,1050,context,1050,'calculate(key, doctor.consultationFee)')
s.line(context,1120,strategy,1120,'calculate(normalized basePrice)')
s.line(strategy,1190,context,1190,'calculated amount',dashed=True)
s.box('normalize','Context validates rate and storage limits; HALF_UP base and final VND amounts.',max(40,context-220),1220,590,55,'whiteSpace=wrap;fontSize=14;')
s.line(context,1320,offer,1320,'PricingAmounts',dashed=True)
s.box('changed','',30,1370,width,240,'fillColor=none;')
s.text('changed-label','break [expected base/rate/final amount differs]',40,1375,width-50,35)
s.line(offer,1470,controller,1470,'BookingRejection.PRICE_CHANGED (rolls back)',dashed=True)
s.line(controller,1530,ui,1530,'409; no write or email; user must confirm a fresh quote',dashed=True)
s.text('reconfirm','UI: retain symptoms; GET fresh quote for same slot/key; display old/new base, rate and total.\nWait for explicit confirmation; repeat this branch on every subsequent price change. Never auto-POST.',40,1560,width-50,45,14)
s.line(offer,1630,service,1630,'LockedOffer (slot, doctor, benefit, amounts)',dashed=True)
s.line(service,1680,appointments,1680,'save(Appointment with pricing snapshot and grant ID)')
s.line(appointments,1730,service,1730,'saved Appointment',dashed=True)
s.line(service,1785,slot,1785,'save(isAvailable=false)')
s.line(service,1845,notification,1845,'publish AppointmentBookedEvent → observers → e-mail after commit')
s.box('commit','Transactional proxy commits; releases Schedule → Doctor'+(' → BenefitRequest' if discounted else '')+' locks. Rollback suppresses AFTER_COMMIT listener.',440,1900,width-450,60,'whiteSpace=wrap;fontSize=15;')
s.line(service,2030,controller,2030,'AppointmentDto with stored snapshot',dashed=True)
s.line(controller,2100,ui,2100,'201; display agreed price / booking code',dashed=True)
s.box('async-note','After commit, existing listener asynchronously queues the captured price. No guaranteed ordering with the HTTP response.',max(30,notification-660),2160,760,80,'whiteSpace=wrap;fontSize=14;')
s.text('footer','Only Strategy-driven booking and its necessary collaborators. No separate Admin workflow, payment or whole-system UML.\nClient money is comparison-only; agreed snapshots do not change after fee/configuration/benefit changes.',30,2320,width,65,15)
s.save('consultation-booking-sequence.drawio')
print('Generated booking Class + Sequence (editable monochrome draw.io).')
