#!/usr/bin/env python3
"""Shared builder for editable, uncompressed, black-and-white draw.io diagrams.

Every design-pattern folder uses this kit so all diagrams look alike:

    import sys; from pathlib import Path
    sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
    from drawio_kit import Diagram, REPO

    d = Diagram('Builder class', 1600, 1000, out=REPO / 'docs/builder/diagrams')
    d.cls('builder', 'NotificationMessageBuilder', '+ type(t)\\n+ build()', 40, 40, 360, 200)
    d.save('builder-class.drawio')

Export PNGs afterwards with `scripts/common/export-diagrams.sh docs/<pattern>/diagrams`.
"""
from html import escape
from pathlib import Path
import xml.etree.ElementTree as ET

REPO = Path(__file__).resolve().parents[2]
STYLE = 'fillColor=#FFFFFF;strokeColor=#000000;fontColor=#000000;fontFamily=Arial;shadow=0;'


class Diagram:
    def __init__(self, title, width, height, out):
        self.out = Path(out)
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

    def edge(self, source, target, label='', style='', points=()):
        self.counter += 1
        edge = ET.SubElement(self.root, 'mxCell', id=f'e{self.counter}', value=label,
                             style=STYLE + 'edgeStyle=orthogonalEdgeStyle;rounded=0;fontSize=14;labelBackgroundColor=#FFFFFF;endArrow=open;' + style,
                             edge='1', parent='1', source=source, target=target)
        geo = ET.SubElement(edge, 'mxGeometry', relative='1', **{'as': 'geometry'})
        if points:
            waypoints = ET.SubElement(geo, 'Array', **{'as': 'points'})
            for x, y in points:
                ET.SubElement(waypoints, 'mxPoint', x=str(x), y=str(y))
        return edge

    def line(self, x1, y1, x2, y2, label='', dashed=False, arrow=True):
        self.counter += 1
        cell = ET.SubElement(self.root, 'mxCell', id=f'l{self.counter}', value=label, edge='1', parent='1',
            style=STYLE + f'fontSize=14;labelBackgroundColor=#FFFFFF;endArrow={"open" if arrow else "none"};dashed={int(dashed)};verticalAlign=bottom;')
        geo = ET.SubElement(cell, 'mxGeometry', relative='1', **{'as': 'geometry'})
        ET.SubElement(geo, 'mxPoint', x=str(x1), y=str(y1), **{'as': 'sourcePoint'})
        ET.SubElement(geo, 'mxPoint', x=str(x2), y=str(y2), **{'as': 'targetPoint'})

    def save(self, name):
        self.out.mkdir(parents=True, exist_ok=True)
        ET.indent(self.file)
        ET.ElementTree(self.file).write(self.out / name, encoding='utf-8', xml_declaration=True)

    # ------------------------------------------------------------ helpers shared by the pattern diagrams
    def title(self, text, width):
        self.text('title', text, 40, 15, width, 40, 21)

    def uml(self, key, title, members, x, y, w):
        """UML class box sized to its member lines; returns its bottom y."""
        h = 52 + 20 * max(1, len(members)) + 24
        self.cls(key, title, '\n'.join(members), x, y, w, h)
        return y + h

    def table(self, key, title, rows, x, y, w=440, new=False):
        """ERD table (physical level): rows of (tag, column, type); returns its bottom y."""
        cells = ''.join(f'<tr><td style="padding:0 10px 0 0">{escape(tag)}</td><td style="padding:0 14px 0 0">'
                        f'{escape(col)}</td><td>{escape(kind)}</td></tr>' for tag, col, kind in rows)
        h = 44 + 19 * len(rows) + 28
        self.box(key, title, x, y, w, h, 'swimlane;startSize=44;fontSize=16;fontStyle=1;' + ('strokeWidth=3;' if new else ''))
        self.box(key + '-body', f'<table style="border-collapse:collapse">{cells}</table>', 0, 44, w, h - 44,
                 'shape=text;html=1;strokeColor=none;fillColor=none;whiteSpace=wrap;align=left;verticalAlign=top;'
                 'fontSize=14;fontFamily=Courier New;spacing=10;', key)
        return y + h

    def rel(self, src, dst, label, many, one, style='', points=()):
        """Crow's-foot relationship: many/one are draw.io ER arrow names (ERmandOne, ERzeroToMany, …)."""
        return self.edge(src, dst, label, f'startArrow={many};endArrow={one};startFill=0;endFill=0;' + style, points)

    def note(self, key, text, x, y, w, h):
        return self.box(key, text, x, y, w, h, NOTE)

    # ------------------------------------------------------------ sơ đồ use case (cùng kiểu với Strategy)
    def system(self, label, x, y, w, h):
        """Khung hệ thống; use case nằm bên trong, actor nằm bên ngoài."""
        self.box('system', label, x, y, w, h, 'verticalAlign=top;fontSize=17;fontStyle=1;spacingTop=8;')

    def actor(self, key, label, x, y):
        self.box(key, label, x, y, 40, 80,
                 'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;fontSize=16;fontStyle=1;'
                 'whiteSpace=wrap;')

    def case(self, key, label, x, y, w=320, h=80):
        self.box(key, label, x, y, w, h, 'ellipse;whiteSpace=wrap;fontSize=15;')

    def assoc(self, a, b, style=''):
        """Actor ↔ use case: đường thẳng không mũi tên."""
        return self.edge(a, b, '', 'edgeStyle=none;endArrow=none;' + style)

    def include(self, base, included, style=''):
        return self.edge(base, included, '«include»', 'edgeStyle=none;dashed=1;' + style)

    def extend(self, extension, base, condition='', style=''):
        label = '«extend»' + ('\n[' + condition + ']' if condition else '')
        return self.edge(extension, base, label, 'edgeStyle=none;dashed=1;' + style)


# Class-diagram relationship styles (UML)
IMPLEMENTS = 'dashed=1;endArrow=block;endFill=0;endSize=14;'
EXTENDS = 'endArrow=block;endFill=0;endSize=14;'
DEPENDS = 'dashed=1;endArrow=open;'
AGGREGATES = 'startArrow=diamondThin;startFill=0;startSize=18;endArrow=open;'
COMPOSES = 'startArrow=diamondThin;startFill=1;startSize=18;endArrow=open;'
# DFD (Yourdon/DeMarco) shapes
EXTERNAL = 'whiteSpace=wrap;fontSize=17;fontStyle=1;'
PROCESS = 'ellipse;whiteSpace=wrap;fontSize=15;fontStyle=1;'
STORE = 'shape=partialRectangle;right=0;whiteSpace=wrap;align=left;spacingLeft=14;fontSize=15;fontStyle=1;'
NOTE = 'shape=note;size=18;whiteSpace=wrap;align=left;verticalAlign=top;fontSize=14;spacing=10;'


class Sequence(Diagram):
    """UML sequence diagram: participants across the top, messages added top to bottom."""

    def __init__(self, title, participants, out, col=280):
        self.xs = [150 + i * col for i in range(len(participants))]
        self.width = self.xs[-1] + 160
        super().__init__(title, self.width + 40, 1000, out)
        self.title(title.upper(), self.width - 40)
        for i, (label, x) in enumerate(zip(participants, self.xs)):
            self.box(f'p{i}', label, x - 125, 80, 250, 75, 'whiteSpace=wrap;fontSize=15;')
        self.y = 215
        self.frames = []
        self.footnote = ''

    def call(self, a, b, label, gap=62):
        self.line(self.xs[a], self.y, self.xs[b], self.y, label)
        self.y += gap

    def reply(self, a, b, label, gap=62):
        self.line(self.xs[a], self.y, self.xs[b], self.y, label, dashed=True)
        self.y += gap

    def step(self, a, label, w=420, h=50):
        """Something a participant does internally, written next to its lifeline (left of the last one)."""
        x = self.xs[a] + 12 if self.xs[a] + 12 + w <= self.width else self.xs[a] - 12 - w
        self.box(f'step{self.counter}', label, x, self.y - 22, w, h,
                 'whiteSpace=wrap;fontSize=14;align=left;spacingLeft=8;')
        self.counter += 1
        self.y += h + 22

    def remark(self, label, h=50):
        self.counter += 1
        self.box(f'remark{self.counter}', label, 40, self.y - 20, self.width - 60, h, 'whiteSpace=wrap;fontSize=14;')
        self.y += h + 20

    def begin(self, label):
        self.frames.append((label, self.y - 10))
        self.y += 40

    def end(self):
        label, top = self.frames.pop()
        self.counter += 1
        inset = 20 + 12 * len(self.frames)
        self.box(f'frame{self.counter}', '', inset, top, self.width - 2 * inset, self.y - top - 25, 'fillColor=none;')
        self.text(f'frame{self.counter}-label', label, inset + 8, top + 2, 900, 30, 15)
        self.y += 10

    def footer(self, text):
        self.footnote = text

    def save(self, name):
        """Lifelines end just below the last message; the page height follows the content."""
        bottom = self.y + 10
        for i, x in enumerate(self.xs):
            self.line(x, 155, x, bottom, dashed=True, arrow=False)
        if self.footnote:
            self.text('footer', self.footnote, 30, bottom + 25, self.width - 40, 60, 14)
        self.file.find('diagram/mxGraphModel').set('pageHeight', str(bottom + 110))
        super().save(name)
