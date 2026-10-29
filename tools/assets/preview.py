"""Contact sheets of generated textures, for reviewing art at a glance."""
from __future__ import annotations

from PIL import Image

from pixel import scale

BG = (34, 30, 40, 255)


def sheet(images, factor=8, columns=None, gap=2, bg=BG, under=None):
    columns = columns or len(images)
    rows = (len(images) + columns - 1) // columns
    w = max(i.width for i in images) * factor
    h = max(i.height for i in images) * factor
    out = Image.new('RGBA', (columns * (w + gap * factor) + gap * factor, rows * (h + gap * factor) + gap * factor), bg)
    for n, image in enumerate(images):
        cx, cy = n % columns, n // columns
        pos = (gap * factor + cx * (w + gap * factor), gap * factor + cy * (h + gap * factor))
        if under is not None:
            out.alpha_composite(scale(under, factor), pos)
        out.alpha_composite(scale(image, factor), pos)
    return out
