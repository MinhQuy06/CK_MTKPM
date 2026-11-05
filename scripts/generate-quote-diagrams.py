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


d = Diagram('Consultation quote — Class', 1600, 1100)
d.text('title', 'STANDARD CONSULTATION QUOTE — CLASS DIAGRAM', 35, 15, 1460, 40, 23)
d.cls('controller', '«REST Controller»\nConsultationQuoteController',
      '+ quote(request: ConsultationQuoteRequest)\n  : ResponseEntity<ApiResponseDto<ConsultationQuoteDto>>\n\nGET /api/v1/appointments/quote\nJWT + PATIENT; @Valid; Cache-Control: no-store', 40, 110, 420, 200)
d.cls('service', '«Application Service / Client»\nConsultationQuoteService',
      '- schedules: ScheduleRepository\n- appointments: AppointmentRepository\n- pricing: PricingContext\n\n+ quote(scheduleId: Long, strategyKey: String)\n  : ConsultationQuoteDto\n@Transactional(readOnly = true)', 40, 405, 420, 245)
d.cls('context', '«Context»\nPricingContext',
      '- strategies: Map<String, ConsultationPricingStrategy>\n\n+ PricingContext(List<ConsultationPricingStrategy>)\n+ calculate(key: String, fee: BigDecimal)\n  : PricingAmounts\n- wholeVnd(amount: BigDecimal): BigDecimal\n\nSelect by key; validate rate/amount; HALF_UP VND.', 590, 405, 430, 260)
d.cls('strategy', '«interface / Strategy»\nConsultationPricingStrategy',
      '+ key(): String\n+ name(): String\n+ discountRate(): BigDecimal\n+ calculate(basePrice: BigDecimal): BigDecimal\n+ simulated(): boolean', 1110, 115, 420, 230)
d.cls('standard', '«Concrete Strategy»\nStandardPricingStrategy',
      '- discountRate: BigDecimal\n\n+ StandardPricingStrategy(discountRate: BigDecimal)\n+ key(): String = STANDARD\n+ name(): String = Tiêu chuẩn\n+ discountRate(): BigDecimal\n+ calculate(basePrice: BigDecimal): BigDecimal\n+ simulated(): boolean = false\n\nFormula: basePrice × (1 − discountRate)\nDefault rate = 0; valid range [0, 1].', 1110, 490, 420, 330)
d.cls('repositories', '«Repository interfaces»\nScheduleRepository / AppointmentRepository',
      '+ findById(id: Long): Optional<Schedule>\n+ existsByScheduleId(id: Long): boolean\n\nRead Schedule → Doctor → User; no slot lock/write.', 40, 800, 430, 200)
d.cls('result', '«Response DTO»\nConsultationQuoteDto',
      'scheduleId, strategyKey, strategyName\nbasePrice, discountRate, finalPrice\ncurrency = VND; simulated = false\n\nPricingAmounts is the immutable Context result.\nExisting entities and booking persistence unchanged.', 590, 800, 430, 200)
d.edge('controller', 'service', 'uses 1', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
d.edge('service', 'context', 'uses 1', 'exitX=1;exitY=0.45;entryX=0;entryY=0.45;')
d.edge('context', 'strategy', 'registered 1..*', 'exitX=0.75;exitY=0;entryX=0;entryY=0.7;')
d.edge('standard', 'strategy', 'implements', 'dashed=1;endArrow=block;endFill=0;exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
d.edge('service', 'repositories', 'reads', 'exitX=0.3;exitY=1;entryX=0.3;entryY=0;')
d.edge('service', 'result', '«create»', 'dashed=1;exitX=0.8;exitY=1;entryX=0.2;entryY=0;')
d.text('scope', 'Scope: server quote only. No Appointment creation, reservation, notification or payment.\nSolid open arrow: association; dashed hollow triangle: interface realization; dashed open arrow: dependency.', 40, 1020, 1460, 65, 14)
d.save('consultation-quote-class.drawio')

s = Diagram('Consultation quote — Sequence', 1830, 1560)
s.text('title', 'STANDARD CONSULTATION QUOTE — SEQUENCE DIAGRAM', 30, 10, 1750, 40, 23)
participants = [('ui', 'Patient dashboard\n«Browser»'), ('controller', 'ConsultationQuote\nController'),
                ('service', 'ConsultationQuote\nService'), ('schedules', 'ScheduleRepository'),
                ('appointments', 'AppointmentRepository'), ('context', 'PricingContext\n«Context»'),
                ('strategy', 'StandardPricingStrategy\n«Concrete Strategy»')]
xs = [135, 390, 645, 900, 1155, 1410, 1665]
for (key, title), x in zip(participants, xs):
    s.box(key, title, x - 112, 80, 224, 70, 'whiteSpace=wrap;fontSize=16;')
    s.line(x, 150, x, 1480, dashed=True, arrow=False)
s.text('security', 'Precondition: JWT filter authenticates the patient; controller requires PATIENT.\n401: missing/invalid JWT; 403: other role; 400: @Valid query binding fails. These stop before the service.', 35, 170, 1740, 70, 15)
s.line(xs[0], 280, xs[1], 280, 'GET /quote?scheduleId&strategyKey')
s.line(xs[1], 335, xs[2], 335, 'quote(scheduleId, strategyKey)')
s.line(xs[2], 390, xs[3], 390, 'findById(scheduleId) — no write lock')
s.line(xs[3], 445, xs[2], 445, 'Optional<Schedule> (Doctor → User)', dashed=True)
s.line(xs[2], 510, xs[4], 510, 'existsByScheduleId(scheduleId)')
s.line(xs[4], 565, xs[2], 565, 'boolean', dashed=True)
s.box('checks', '«service checks»  Available flag; no appointment; slot is in the future; doctor approved and user active.', 505, 610, 820, 55, 'whiteSpace=wrap;fontSize=15;')
s.box('alt1', '', 35, 700, 1740, 170, 'fillColor=none;')
s.text('alt1label', 'alt [schedule missing / unavailable / past / doctor unavailable]', 40, 705, 1400, 35, 15)
s.line(xs[2], 775, xs[1], 775, 'throw ResourceNotFoundException (404) / BookingConflictException (409)', dashed=True)
s.line(xs[1], 835, xs[0], 835, 'ApiResponseDto.error — show error, clear quote', dashed=True)
s.text('continuation', '[else: schedule is bookable] — continue below', 40, 885, 1500, 35, 15)
s.line(xs[2], 955, xs[5], 955, 'calculate(strategyKey, doctor.consultationFee)')
s.box('choose', 'Default absent key → STANDARD; look up registry; validate rate; normalize base fee HALF_UP.\nUnknown key / invalid fee / overflow → 400 via exception handler; no quote returned.', 1200, 985, 560, 85, 'whiteSpace=wrap;fontSize=14;')
s.line(xs[5], 1120, xs[6], 1120, 'calculate(basePrice)')
s.line(xs[6], 1180, xs[5], 1180, 'basePrice × (1 − discountRate)', dashed=True)
s.text('normalize', 'Context validates and rounds final price HALF_UP; builds PricingAmounts.', 1150, 1200, 610, 45, 14)
s.line(xs[5], 1285, xs[2], 1285, 'PricingAmounts', dashed=True)
s.line(xs[2], 1350, xs[1], 1350, 'ConsultationQuoteDto', dashed=True)
s.line(xs[1], 1415, xs[0], 1415, '200 ApiResponseDto.success(quote); Cache-Control: no-store', dashed=True)
s.text('footer', 'UI displays only the current slot/kind response; late results are ignored.\nNo appointment saved, slot reserved, notification sent or payment performed. A quote does not guarantee availability.', 35, 1495, 1740, 55, 15)
s.save('consultation-quote-sequence.drawio')
print('Generated 2 editable monochrome draw.io UML files.')
