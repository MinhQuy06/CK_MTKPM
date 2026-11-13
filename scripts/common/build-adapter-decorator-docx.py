"""Gom lý thuyết Adapter, lý thuyết Decorator và kịch bản thuyết trình thành docs/adapter-decorator.docx (cần python-docx).

Chạy: python3 scripts/common/build-adapter-decorator-docx.py . docs/adapter-decorator.docx
"""
import re
import sys
from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

ROOT = Path(sys.argv[1])
OUT = Path(sys.argv[2])
PARTS = [
    ('PHẦN I. MẪU ADAPTER', ROOT / 'docs/adapter/ly-thuyet-adapter.md'),
    ('PHẦN II. MẪU DECORATOR', ROOT / 'docs/decorator/ly-thuyet-decorator.md'),
    ('PHẦN III. KỊCH BẢN THUYẾT TRÌNH', ROOT / 'docs/thuyet-trinh-adapter-decorator.md'),
]
FONT = 'Arial'
MONO = 'Courier New'
CONTENT_WIDTH_CM = 17.0

doc = Document()
sec = doc.sections[0]
sec.page_width, sec.page_height = Cm(21), Cm(29.7)
sec.orientation = WD_ORIENT.PORTRAIT
for side in ('left_margin', 'right_margin'):
    setattr(sec, side, Cm(2))
sec.top_margin = sec.bottom_margin = Cm(2)


def set_font(style, size, bold=False, color=None):
    style.font.name = FONT
    style.font.size = Pt(size)
    style.font.bold = bold
    rpr = style.element.get_or_add_rPr()
    fonts = rpr.find(qn('w:rFonts'))
    if fonts is None:
        fonts = OxmlElement('w:rFonts')
        rpr.append(fonts)
    for attr in ('w:ascii', 'w:hAnsi', 'w:cs', 'w:eastAsia'):
        fonts.set(qn(attr), FONT)
    if color:
        style.font.color.rgb = RGBColor.from_string(color)


set_font(doc.styles['Normal'], 11)
doc.styles['Normal'].paragraph_format.space_after = Pt(4)
doc.styles['Normal'].paragraph_format.line_spacing = 1.15
for level, size in ((0, 24), (1, 18), (2, 15), (3, 13), (4, 12)):
    st = doc.styles['Title' if level == 0 else f'Heading {level}']
    set_font(st, size, bold=True, color='1F3864' if level <= 1 else '2E2E2E')
    st.paragraph_format.space_before = Pt(14 if level <= 2 else 10)
    st.paragraph_format.space_after = Pt(6)
    st.paragraph_format.keep_with_next = True

INLINE = re.compile(r'(\*\*[^*]+\*\*|`[^`]+`|\[[^\]]+\]\([^)]+\)|\*[^*\s][^*]*\*)')


def add_inline(par, text, bold=False, size=None):
    for piece in INLINE.split(text):
        if not piece:
            continue
        if piece.startswith('**') and piece.endswith('**'):
            add_inline(par, piece[2:-2], bold=True, size=size)
            continue
        if piece.startswith('`') and piece.endswith('`'):
            run = par.add_run(piece[1:-1])
            run.font.name = MONO
            run._element.rPr.rFonts.set(qn('w:hAnsi'), MONO)
            run.font.size = Pt((size or 11) - 1)
            run.font.color.rgb = RGBColor(0xA3, 0x15, 0x15)
            run.bold = bold
            continue
        link = re.fullmatch(r'\[([^\]]+)\]\(([^)]+)\)', piece)
        if link:
            run = par.add_run(link.group(1))
            run.bold = bold
            if size:
                run.font.size = Pt(size)
            continue
        if piece.startswith('*') and piece.endswith('*') and len(piece) > 2:
            run = par.add_run(piece[1:-1])
            run.italic = True
            run.bold = bold
            if size:
                run.font.size = Pt(size)
            continue
        run = par.add_run(piece)
        run.bold = bold
        if size:
            run.font.size = Pt(size)


def shade(cell, fill):
    tcpr = cell._element.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), fill)
    tcpr.append(shd)


def borders(table):
    tblpr = table._element.tblPr
    b = OxmlElement('w:tblBorders')
    for edge in ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'):
        e = OxmlElement(f'w:{edge}')
        e.set(qn('w:val'), 'single')
        e.set(qn('w:sz'), '4')
        e.set(qn('w:color'), '808080')
        b.append(e)
    tblpr.append(b)


def add_table(rows):
    cols = max(len(r) for r in rows)
    table = doc.add_table(rows=len(rows), cols=cols)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    borders(table)
    # Cột rộng theo độ dài nội dung dài nhất (giới hạn 4..60 ký tự), tổng = bề rộng trang
    weights = []
    for j in range(cols):
        longest = max(len(re.sub(r'[`*]', '', r[j])) if j < len(r) else 0 for r in rows)
        weights.append(min(max(longest, 4), 60))
    total_dxa = int(CONTENT_WIDTH_CM / 2.54 * 1440)
    widths = [int(total_dxa * w / sum(weights)) for w in weights]
    tblpr = table._element.tblPr
    tblw = tblpr.find(qn('w:tblW'))
    if tblw is None:
        tblw = OxmlElement('w:tblW')
        tblpr.append(tblw)
    tblw.set(qn('w:w'), str(sum(widths)))
    tblw.set(qn('w:type'), 'dxa')
    layout = OxmlElement('w:tblLayout')
    layout.set(qn('w:type'), 'fixed')
    tblpr.append(layout)
    for j, gc in enumerate(table._element.tblGrid.findall(qn('w:gridCol'))):
        gc.set(qn('w:w'), str(widths[j]))
    for i, row in enumerate(rows):
        for j in range(cols):
            cell = table.cell(i, j)
            cell.width = int(widths[j] * 635)  # dxa → EMU
            text = row[j] if j < len(row) else ''
            par = cell.paragraphs[0]
            par.paragraph_format.space_after = Pt(0)
            add_inline(par, text, bold=(i == 0), size=9.5)
            if i == 0:
                shade(cell, 'D9E2F3')
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def add_code(lines):
    table = doc.add_table(rows=1, cols=1)
    borders(table)
    table.autofit = False
    cell = table.cell(0, 0)
    shade(cell, 'F2F2F2')
    cell.width = Cm(CONTENT_WIDTH_CM)
    full = str(int(CONTENT_WIDTH_CM / 2.54 * 1440))
    table._element.tblGrid.findall(qn('w:gridCol'))[0].set(qn('w:w'), full)
    tblw = table._element.tblPr.find(qn('w:tblW'))
    if tblw is None:
        tblw = OxmlElement('w:tblW')
        table._element.tblPr.append(tblw)
    tblw.set(qn('w:w'), full)
    tblw.set(qn('w:type'), 'dxa')
    par = cell.paragraphs[0]
    for k, line in enumerate(lines):
        run = par.add_run(line)
        run.font.name = MONO
        run._element.rPr.rFonts.set(qn('w:hAnsi'), MONO)
        run.font.size = Pt(8.5)
        if k < len(lines) - 1:
            run.add_break()
    doc.add_paragraph().paragraph_format.space_after = Pt(2)


def orient(landscape):
    sec = doc.add_section(WD_SECTION.NEW_PAGE)
    sec.orientation = WD_ORIENT.LANDSCAPE if landscape else WD_ORIENT.PORTRAIT
    sec.page_width, sec.page_height = (Cm(29.7), Cm(21)) if landscape else (Cm(21), Cm(29.7))
    sec.left_margin = sec.right_margin = Cm(1.5 if landscape else 2)
    sec.top_margin = sec.bottom_margin = Cm(1.5 if landscape else 2)


def add_diagram(path, caption):
    last = doc.paragraphs[-1]
    moved = None
    if last.style.name.startswith('Heading'):
        moved = (last.text, int(last.style.name.split()[-1]))
        last._element.getparent().remove(last._element)
    orient(True)
    if moved:
        doc.add_heading(moved[0], level=moved[1])
    if not path.exists():
        raise SystemExit(f'missing image {path}')
    import struct
    with open(path, 'rb') as f:
        head = f.read(24)
    w, h = struct.unpack('>II', head[16:24])  # kích thước trong header PNG
    max_w, max_h = 26.7, 15.0
    if w / h > max_w / max_h:
        doc.add_picture(str(path), width=Cm(max_w))
    else:
        doc.add_picture(str(path), height=Cm(max_h))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap = doc.add_paragraph()
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = cap.add_run(caption + ' (trang ngang để xem rõ; giải thích ở trang sau)')
    run.italic = True
    run.font.size = Pt(9.5)
    orient(False)


def add_image(path, caption):
    if not path.exists():
        raise SystemExit(f'missing image {path}')
    doc.add_picture(str(path), width=Cm(CONTENT_WIDTH_CM))
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap = doc.add_paragraph()
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = cap.add_run(caption)
    run.italic = True
    run.font.size = Pt(9.5)


def add_quote(text):
    par = doc.add_paragraph()
    par.paragraph_format.left_indent = Cm(0.8)
    ppr = par._element.get_or_add_pPr()
    bdr = OxmlElement('w:pBdr')
    left = OxmlElement('w:left')
    left.set(qn('w:val'), 'single')
    left.set(qn('w:sz'), '18')
    left.set(qn('w:space'), '8')
    left.set(qn('w:color'), '4472C4')
    bdr.append(left)
    ppr.append(bdr)
    add_inline(par, text)
    for run in par.runs:
        if not run.font.name == MONO:
            run.italic = True


def toc():
    par = doc.add_paragraph()
    run = par.add_run()
    for tag, text in (('begin', None), (None, 'TOC \\o "1-2" \\h \\z \\u'), ('separate', None), (None, None), ('end', None)):
        if tag:
            fld = OxmlElement('w:fldChar')
            fld.set(qn('w:fldCharType'), tag)
            run._r.append(fld)
        elif text:
            instr = OxmlElement('w:instrText')
            instr.set(qn('xml:space'), 'preserve')
            instr.text = text
            run._r.append(instr)
        else:
            t = OxmlElement('w:t')
            t.text = 'Mở file trong Word, bấm chuột phải vào đây → Update Field để hiện mục lục.'
            run._r.append(t)


def convert(md_path, part_title):
    base = md_path.parent
    lines = md_path.read_text(encoding='utf-8').splitlines()
    doc.add_heading(part_title, level=1)
    i = 0
    first_h1_skipped = False
    while i < len(lines):
        line = lines[i]
        s = line.strip()
        if not s or s == '---':
            i += 1
            continue
        if s.startswith('[') and '](' in s and ' · ' in s:  # thanh liên kết đầu file
            i += 1
            continue
        if s.startswith('```'):
            code = []
            i += 1
            while i < len(lines) and not lines[i].strip().startswith('```'):
                code.append(lines[i])
                i += 1
            add_code(code)
            i += 1
            continue
        m = re.match(r'^(#{1,4})\s+(.*)', s)
        if m:
            level = len(m.group(1))
            if level == 1 and not first_h1_skipped:
                first_h1_skipped = True
                p = doc.add_paragraph()
                add_inline(p, m.group(2), bold=True, size=12)
            else:
                doc.add_heading(re.sub(r'[`*]', '', m.group(2)), level=min(level, 4))
            i += 1
            continue
        img = re.match(r'^!\[([^\]]*)\]\(([^)]+)\)', s)
        if img:
            add_diagram((base / img.group(2)).resolve(), img.group(1))
            i += 1
            continue
        if s.startswith('|'):
            rows = []
            while i < len(lines) and lines[i].strip().startswith('|'):
                cells = [c.strip() for c in lines[i].strip().strip('|').split('|')]
                if not all(re.fullmatch(r':?-{2,}:?', c) for c in cells if c):
                    rows.append(cells)
                i += 1
            add_table(rows)
            continue
        if s.startswith('>'):
            buf = []
            while i < len(lines) and lines[i].strip().startswith('>'):
                t = lines[i].strip()[1:].strip()
                if t:
                    buf.append(t)
                elif buf:
                    add_quote(' '.join(buf))
                    buf = []
                i += 1
            if buf:
                add_quote(' '.join(buf))
            continue
        b = re.match(r'^(\s*)([-*]|\d+\.)\s+(.*)', line)
        if b:
            indent = len(b.group(1)) // 2
            numbered = b.group(2)[0].isdigit()
            style = 'List Number' if numbered and indent == 0 else ('List Bullet 2' if indent else 'List Bullet')
            par = doc.add_paragraph(style=style)
            par.paragraph_format.space_after = Pt(2)
            add_inline(par, b.group(3))
            i += 1
            continue
        par = doc.add_paragraph()
        add_inline(par, s)
        i += 1


# Trang bìa
title = doc.add_paragraph(style='Title')
title.alignment = WD_ALIGN_PARAGRAPH.CENTER
title.add_run('ADAPTER VÀ DECORATOR')
for text, size, bold in (
        ('Lý thuyết · Điều kiện áp dụng · Cách làm trong bài · Nếu không dùng thì sao', 13, False),
        ('Giải thích chi tiết 10 sơ đồ · Kịch bản thuyết trình', 13, False),
        ('', 11, False),
        ('Hệ thống đặt lịch khám bệnh · Môn Mẫu thiết kế cho phần mềm', 12, True),
        ('Phần mẫu cấu trúc (slide 16–24)', 11, False)):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(text)
    r.font.size = Pt(size)
    r.bold = bold
add_image(ROOT / 'docs/diagrams/design-patterns-map.png', 'Bản đồ 7 mẫu thiết kế: khung «Decorator» và «Adapter» ở nửa dưới')
doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)
doc.add_heading('Mục lục', level=1)
toc()

for k, (part_title, path) in enumerate(PARTS):
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)
    convert(path, part_title)

# Số trang ở chân trang
footer = sec.footer.paragraphs[0]
footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
run = footer.add_run()
for tag, text in (('begin', None), (None, 'PAGE'), ('end', None)):
    if tag:
        fld = OxmlElement('w:fldChar')
        fld.set(qn('w:fldCharType'), tag)
        run._r.append(fld)
    else:
        instr = OxmlElement('w:instrText')
        instr.text = text
        run._r.append(instr)

doc.save(OUT)
print('saved', OUT)
