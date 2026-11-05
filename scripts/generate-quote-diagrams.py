#!/usr/bin/env python3
"""Generate editable, uncompressed draw.io UML for the Standard quote feature."""
from pathlib import Path
import xml.etree.ElementTree as ET

OUT = Path(__file__).resolve().parents[1] / 'docs/diagrams'
STYLE = 'fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontFamily=Arial;shadow=0;'


class Diagram:
    def __init__(self, title, width, height):
        self.file = ET.Element('mxfile', host='app.diagrams.net', type='device')
        page = ET.SubElement(self.file, 'diagram', id=title, name=title)
        model = ET.SubElement(page, 'mxGraphModel', grid='0', page='1', pageScale='1',
                              pageWidth=str(width), pageHeight=str(height), background='#FFFFFF')
        self.root = ET.SubElement(model, 'root')
        ET.SubElement(self.root, 'mxCell', id='0')
        ET.SubElement(self.root, 'mxCell', id='1', parent='0')
        self.counter = 0

    def box(self, key, value, x, y, w, h, style='', parent='1'):
        cell = ET.SubElement(self.root, 'mxCell', id=key, value=value, style=STYLE + style,
                             vertex='1', parent=parent)
        ET.SubElement(cell, 'mxGeometry', x=str(x), y=str(y), width=str(w), height=str(h), **{'as': 'geometry'})
        return cell

    def text(self, key, value, x, y, w, h, size=15):
        return self.box(key, value, x, y, w, h,
                        f'shape=text;strokeColor=none;fillColor=none;whiteSpace=wrap;align=left;verticalAlign=top;fontSize={size};spacing=5;')

    def cls(self, key, title, body, x, y, w, h):
        self.box(key, title, x, y, w, h,
                 'swimlane;startSize=52;horizontal=1;whiteSpace=wrap;fontSize=17;fontStyle=1;')
        self.box(key + '-body', body, 0, 52, w, h - 52,
                 'shape=text;strokeColor=none;fillColor=none;whiteSpace=wrap;align=left;verticalAlign=top;fontSize=14;spacing=10;', key)

    def edge(self, source, target, label='', style=''):
        self.counter += 1
        edge = ET.SubElement(self.root, 'mxCell', id=f'e{self.counter}', value=label,
                             style=STYLE + 'edgeStyle=orthogonalEdgeStyle;rounded=0;fontSize=14;labelBackgroundColor=#FFFFFF;endArrow=open;' + style,
                             edge='1', parent='1', source=source, target=target)
        ET.SubElement(edge, 'mxGeometry', relative='1', **{'as': 'geometry'})

    def line(self, x1, y1, x2, y2, label='', dashed=False, arrow=True):
        self.counter += 1
        cell = ET.SubElement(self.root, 'mxCell', id=f'l{self.counter}', value=label, edge='1', parent='1',
            style=STYLE + f'fontSize=14;labelBackgroundColor=#FFFFFF;endArrow={"open" if arrow else "none"};dashed={int(dashed)};verticalAlign=bottom;')
        geo = ET.SubElement(cell, 'mxGeometry', relative='1', **{'as': 'geometry'})
        ET.SubElement(geo, 'mxPoint', x=str(x1), y=str(y1), **{'as': 'sourcePoint'})
        ET.SubElement(geo, 'mxPoint', x=str(x2), y=str(y2), **{'as': 'targetPoint'})

    def save(self, name):
        OUT.mkdir(parents=True, exist_ok=True)
        ET.indent(self.file)
        ET.ElementTree(self.file).write(OUT / name, encoding='utf-8', xml_declaration=True)


d = Diagram('Consultation quote — Class', 2130, 1300)
d.text('title','CONSULTATION QUOTE — STRATEGY CLASS DIAGRAM',40,15,2000,45,23)
d.cls('controller','«REST Controller»\nConsultationQuoteController',
 '+ quote(actor, request): ResponseEntity<ApiResponseDto<ConsultationQuoteDto>>\n+ options(actor, request): ResponseEntity<ApiResponseDto<List<PricingChoice>>>\nJWT + PATIENT; @Valid; Cache-Control: no-store',40,110,510,230)
d.cls('service','«Client / Application service»\nConsultationQuoteService',
 '- schedules: ScheduleRepository\n- appointments: AppointmentRepository\n- pricing: PricingContext\n- benefits: BenefitService\n\n+ quote(patientId, scheduleId, strategyKey): ConsultationQuoteDto\n+ options(patientId, scheduleId): List<PricingChoice>\n- bookableSchedule(id): Schedule\n@Transactional(readOnly=true)',40,430,510,295)
d.cls('context','«Context»\nPricingContext',
 '- strategies: Map<String, ConsultationPricingStrategy>\n\n+ supports(key): boolean\n+ choices(): List<PricingChoice>\n+ calculate(key, fee: BigDecimal): PricingAmounts\n- wholeVnd(amount: BigDecimal): BigDecimal\n\nImmutable registry; HALF_UP and storage limits.',700,430,460,270)
d.cls('strategy','«interface / Strategy»\nConsultationPricingStrategy',
 '+ key(): String\n+ name(): String\n+ discountRate(): BigDecimal\n+ calculate(basePrice: BigDecimal): BigDecimal\n+ simulated(): boolean',1300,110,750,240)
kinds=[('STANDARD','StandardPricingStrategy','0'),('INSURANCE','InsurancePricingStrategy','0.8')]
if (OUT.parents[1]/'backend/src/main/java/com/datlichkhambenh/backend/service/pricing/VipPricingStrategy.java').exists():
    kinds.append(('VIP','VipPricingStrategy','0.1'))
for index,(key,cls,rate) in enumerate(kinds):
    x,y=[(1300,460),(1700,460),(1700,870)][index]
    d.cls(key,'«Concrete Strategy»\n'+cls,
          '+ key(): String = '+key+'\n+ name(): String\n+ discountRate(): BigDecimal\n+ calculate(basePrice): BigDecimal\n+ simulated(): boolean = false\n\nFormula: base × (1 − rate)\nDefault discount rate = '+rate+'\nConfigured rate must be in [0,1].',x,y,350,300)
    d.edge(key,'strategy','implements','dashed=1;endArrow=block;endFill=0;exitX='+('1' if index==2 else '0.5')+';exitY='+('0.1' if index==2 else '0')+';entryX='+str([.23,.77,1][index])+';entryY='+('0.5' if index==2 else '1')+';')
d.cls('repositories','«Repository interfaces»\nScheduleRepository / AppointmentRepository',
 '+ findById(id): Optional<Schedule>\n+ existsByScheduleId(id): boolean\n\nReads Schedule → Doctor → User.\nNo write lock or reservation during quote.',40,870,510,200)
d.cls('eligibility','«Supporting service»\nBenefitService',
 '+ eligible(patientId, kind, appointmentDay)\n  : Optional<BenefitRequest>\n\nAPPROVED, not revoked; dates must include\ntoday AND appointment day (Vietnam time).\nSTANDARD does not require a grant.',700,870,460,220)
d.cls('dto','«Immutable response»\nConsultationQuoteDto',
 'scheduleId, strategyKey, strategyName\nbasePrice, discountRate, finalPrice\ncurrency = VND; simulated = false\n\nPricingAmounts: Context result\nPricingChoice: available key/name',1300,870,350,230)
d.edge('controller','service','uses','exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
d.edge('service','context','uses','exitX=1;exitY=0.25;entryX=0;entryY=0.25;')
d.edge('context','strategy','1..* registered','exitX=0.7;exitY=0;entryX=0;entryY=0.6;')
d.edge('service','repositories','reads','exitX=0.3;exitY=1;entryX=0.3;entryY=0;')
d.edge('service','eligibility','checks eligibility','exitX=1;exitY=0.9;entryX=0;entryY=0.3;')
d.edge('service','dto','«create»','dashed=1;exitX=0.8;exitY=1;entryX=0.5;entryY=0;')
d.text('scope','Scope: quote and its pricing options only. BenefitService is shown solely as a collaborator for the Strategy eligibility check.\nNo separate Admin workflow UML. No appointment write, reservation, notification or payment in this interaction.',40,1180,2000,70,15)
d.save('consultation-quote-class.drawio')

s=Diagram('Consultation quote — Sequence',2250,2240)
s.text('title','CONSULTATION QUOTE — STRATEGY SEQUENCE DIAGRAM',30,10,2160,45,23)
labels=['Patient dashboard','ConsultationQuoteController','ConsultationQuoteService','ScheduleRepository','AppointmentRepository','BenefitService','PricingContext','selectedStrategy\n: ConsultationPricingStrategy']
xs=[140,420,700,980,1260,1540,1820,2100]
for i,(label,x) in enumerate(zip(labels,xs)):
    s.box('p'+str(i),label,x-120,80,240,70,'whiteSpace=wrap;fontSize=15;')
    s.line(x,150,x,2140,dashed=True,arrow=False)
s.box('auth','JWT authenticates PATIENT; query @Valid. 401 / 403 / 400 stop before the application service.',30,175,2160,55,'whiteSpace=wrap;fontSize=15;')
s.line(xs[0],280,xs[1],280,'GET /appointments/quote? scheduleId & strategyKey')
s.line(xs[1],340,xs[2],340,'quote(patientId from principal, scheduleId, key)')
s.line(xs[2],405,xs[3],405,'findById(scheduleId) — no write lock')
s.line(xs[3],465,xs[2],465,'Optional<Schedule>',dashed=True)
s.box('missing','',30,500,2160,150,'fillColor=none;')
s.text('missing-label','break [schedule absent]',40,505,1400,35)
s.line(xs[2],575,xs[1],575,'ResourceNotFoundException',dashed=True)
s.line(xs[1],630,xs[0],630,'404 ApiResponseDto.error; stop',dashed=True)
s.line(xs[2],710,xs[4],710,'existsByScheduleId(scheduleId)')
s.line(xs[4],770,xs[2],770,'boolean',dashed=True)
s.box('bookable','Available flag; no existing appointment; future slot in Vietnam time; approved and active doctor.',510,800,1150,55,'whiteSpace=wrap;fontSize=15;')
s.box('unavailable','',30,880,2160,150,'fillColor=none;')
s.text('unavailable-label','break [slot or doctor unavailable]',40,885,1500,35)
s.line(xs[2],955,xs[1],955,'BookingConflictException',dashed=True)
s.line(xs[1],1015,xs[0],1015,'409 ApiResponseDto.error; stop',dashed=True)
s.line(xs[2],1090,xs[6],1090,'supports(key); absent key defaults to STANDARD')
s.line(xs[6],1145,xs[2],1145,'supported? unknown / blank key → 400 and stop',dashed=True)
s.box('benefit-opt','',500,1190,1690,220,'fillColor=none;')
s.text('benefit-opt-label','opt [key != STANDARD]',510,1195,1400,35)
s.line(xs[2],1260,xs[5],1260,'eligible(patientId, key, schedule.workDate)')
s.line(xs[5],1320,xs[2],1320,'Optional<BenefitRequest>',dashed=True)
s.box('eligibility-note','Empty grant → 403 and stop. Approved grant dates must cover today + appointment day; no self-declared privileges.',550,1350,1580,45,'whiteSpace=wrap;fontSize=14;')
s.line(xs[2],1480,xs[6],1480,'calculate(key, doctor.consultationFee)')
s.box('normalization','Context selects registered Strategy; checks rate [0,1]; normalizes base fee HALF_UP. Invalid money / overflow → 400 and stop.',1450,1510,730,65,'whiteSpace=wrap;fontSize=14;')
s.line(xs[6],1640,xs[7],1640,'calculate(basePrice)')
s.line(xs[7],1710,xs[6],1710,'basePrice × (1 − discountRate)',dashed=True)
s.box('result-note','Context validates final money and rounds HALF_UP; creates PricingAmounts.',1480,1740,700,50,'whiteSpace=wrap;fontSize=14;')
s.line(xs[6],1850,xs[2],1850,'PricingAmounts',dashed=True)
s.line(xs[2],1920,xs[1],1920,'ConsultationQuoteDto',dashed=True)
s.line(xs[1],2000,xs[0],2000,'200 ApiResponseDto.success(quote); Cache-Control: no-store',dashed=True)
s.box('ui-note','UI loads pricing-options for the selected patient/slot, then requests the selected quote. Late slot/kind responses are ignored.',30,2050,2160,55,'whiteSpace=wrap;fontSize=15;')
s.text('footer','No appointment saved, slot reserved, notification sent or payment performed. A quote does not guarantee availability.\nSelected Strategy is '+ ' / '.join(c[1] for c in kinds)+'. Admin verification workflow is outside this UML.',30,2160,2160,65,15)
s.save('consultation-quote-sequence.drawio')
print('Generated 2 editable quote UML files matching registered pricing Strategies.')
