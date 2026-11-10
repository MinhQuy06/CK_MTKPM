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
