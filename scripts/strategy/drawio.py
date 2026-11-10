#!/usr/bin/env python3
"""Strategy diagrams: the shared draw.io kit, writing into docs/strategy/diagrams."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import Diagram as _KitDiagram, REPO, STYLE  # noqa: E402,F401

OUT = REPO / 'docs/strategy/diagrams'


class Diagram(_KitDiagram):
    def __init__(self, title, width, height):
        super().__init__(title, width, height, out=OUT)
