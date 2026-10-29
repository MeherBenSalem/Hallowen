"""Entity textures, painted face by face onto the models' UV layouts.

Each model is described as boxes (texture offset, size, mirror) exactly as in its Java LayerDefinition; a painter
fills every face of every box. The glow textures (rendered additively, black is clear) share the layout.
"""
from __future__ import annotations

import math
from dataclasses import dataclass

from pixel import Rng, canvas, mix, put, rgb


@dataclass
class Box:
    name: str
    u: int
    v: int
    w: int
    h: int
    d: int

    def faces(self):
        u, v, w, h, d = self.u, self.v, self.w, self.h, self.d
        return {
            'top': (u + d, v, w, d),
            'bottom': (u + d + w, v, w, d),
            'right': (u, v + d, d, h),
            'front': (u + d, v + d, w, h),
            'left': (u + d + w, v + d, d, h),
            'back': (u + d + w + d, v + d, w, h),
        }


def paint(size, boxes, painter):
    img = canvas(*size)
    for box in boxes:
        for face, (x0, y0, w, h) in box.faces().items():
            for y in range(h):
                for x in range(w):
                    colour = painter(box, face, x, y, w, h)
                    if colour is not None:
                        put(img, x0 + x, y0 + y, colour)
    return img


def alpha(colour, a):
    c = rgb(colour)
    return (c[0], c[1], c[2], int(a))


# ---------------------------------------------------------------------------------------------------------------------
# Spirit (64x64): hooded head, shroud in three folds, ragged tails and long arms. Pale, translucent, fading downward.

SPIRIT_BOXES = [
    Box('head', 0, 0, 8, 8, 8),
    Box('body', 0, 16, 10, 7, 6),
    Box('skirt', 0, 30, 9, 6, 5),
    Box('hem', 0, 42, 8, 5, 4),
    Box('tail_left', 32, 30, 2, 4, 2),
    Box('tail_mid', 40, 30, 2, 6, 2),
    Box('tail_right', 48, 30, 2, 3, 2),
    Box('right_arm', 32, 0, 3, 11, 3),
    Box('left_arm', 44, 0, 3, 11, 3),
]
SPIRIT_FADE = {'head': 235, 'body': 225, 'right_arm': 200, 'left_arm': 200, 'skirt': 200, 'hem': 170, 'tail_left': 130,
               'tail_mid': 130, 'tail_right': 130}
SPIRIT_FACE = """
........
........
.kk..kk.
.kK..Kk.
........
...mm...
...mm...
........
"""


def spirit_painter(box, face, x, y, w, h):
    rng = Rng(hash((box.name, face, x, y)) & 0xFFFF)
    base = rgb('#e2f7f4')
    shade = rgb('#a8d6d6')
    deep = rgb('#6fa8ad')
    a = SPIRIT_FADE[box.name]
    t = y / max(1, h - 1)
    if face in ('top',):
        c = mix(base, '#ffffff', 0.25)
    elif face == 'bottom':
        c = shade
    else:
        c = mix(base, shade, t * 0.6)
        # soft vertical folds
        if face in ('front', 'back') and w >= 6 and x in (w // 3, 2 * w // 3) and box.name != 'head':
            c = mix(c, deep, 0.35)
        if rng.random() < 0.07:
            c = mix(c, '#ffffff', 0.4)
    if box.name == 'head':
        if face == 'front':
            row = SPIRIT_FACE.strip('\n').split('\n')[y]
            ch = row[x]
            if ch in 'kK':
                return alpha('#14262b', 250)
            if ch == 'm':
                return alpha('#1f3a40', 240)
            # hood rim: darker border on the face
            if x in (0, w - 1) or y == 0:
                return alpha(mix(c, deep, 0.5), a)
            return alpha(mix(c, '#ffffff', 0.15), a)
        a = a - 10 * t
    if box.name.startswith('tail') or box.name == 'hem':
        a = a * (1 - 0.35 * t)
        # ragged bottoms
        if box.name.startswith('tail') and y == h - 1 and (x + hash(box.name)) % 2 == 0:
            return None
    if box.name.endswith('arm') and y >= h - 2 and face != 'top':
        c = mix(c, '#c8f4f2', 0.3)
    return alpha(c, a)


def spirit_eyes_painter(box, face, x, y, w, h):
    if box.name != 'head' or face != 'front':
        return None
    ch = SPIRIT_FACE.strip('\n').split('\n')[y][x]
    if ch == 'K':
        return '#e8ffff'
    if ch == 'k':
        return '#4fd6e6'
    if ch == 'm':
        return '#1f6f7a'
    return None


def spirit():
    return paint((64, 64), SPIRIT_BOXES, spirit_painter)


def spirit_eyes():
    return paint((64, 64), SPIRIT_BOXES, spirit_eyes_painter)


# ---------------------------------------------------------------------------------------------------------------------
# Hollow Horseman (64x64 humanoid + coat, cape, collar and pauldrons).

HORSEMAN_BOXES = [
    Box('head', 0, 0, 8, 8, 8),
    Box('hat', 32, 0, 8, 8, 8),
    Box('body', 16, 16, 8, 12, 4),
    Box('arm', 40, 16, 4, 12, 4),
    Box('leg', 0, 16, 4, 12, 4),
    Box('cape', 0, 32, 10, 18, 1),
    Box('coat', 24, 32, 9, 8, 5),
    Box('collar', 0, 52, 9, 3, 5),
    Box('pauldron', 32, 48, 5, 3, 5),
]
COAT = ['#0f0c14', '#1a1522', '#262031', '#352c44']
VEST = ['#3a0d16', '#5a1422', '#7a1e2c']
TRIM = ['#8a3a0e', '#c4661a', '#ee8a2c']
LEATHER = ['#160e0a', '#2a1a10', '#40281a']
IRON_DARK = ['#1c1d22', '#2e3038', '#4a4e5a']


def horseman_painter(box, face, x, y, w, h):
    rng = Rng(hash((box.name, face, x, y)) & 0xFFFF)
    noise = rng.random()
    t = y / max(1, h - 1)

    def coat(level=1):
        c = COAT[level]
        if noise < 0.08:
            c = COAT[min(3, level + 1)]
        elif noise > 0.93:
            c = COAT[max(0, level - 1)]
        return c

    if box.name in ('head', 'hat'):
        return '#0a0708' if box.name == 'head' else None
    if box.name == 'body':
        if face == 'front':
            if 2 <= x <= 5 and y <= 8:
                c = VEST[1] if x in (3, 4) else VEST[0]
                if x == 4 and y in (2, 4, 6):
                    return '#e0a030'
                if y == 0:
                    return VEST[2]
                return c
            if x in (1, 6) and y <= 8:
                return TRIM[1] if y > 0 else TRIM[2]
            if y in (9, 10):
                if 3 <= x <= 4:
                    return '#c9a24a' if y == 9 else '#8a6a2a'
                return LEATHER[1] if y == 9 else LEATHER[0]
            return coat(1 if x in (0, 7) else 2)
        if face == 'back':
            if y in (9, 10):
                return LEATHER[1] if y == 9 else LEATHER[0]
            return coat(1)
        if face in ('left', 'right'):
            if y in (9, 10):
                return LEATHER[1] if y == 9 else LEATHER[0]
            return coat(0 if x == 0 else 1)
        if face == 'top':
            return '#2a0a06'
        return coat(0)
    if box.name == 'arm':
        if face == 'bottom':
            return LEATHER[0]
        if y >= 10:
            # gloves
            return LEATHER[2] if (face == 'front' and x == 1) else LEATHER[1] if y == 10 else LEATHER[0]
        if y == 9:
            return TRIM[1] if face != 'back' else TRIM[0]
        if face == 'top':
            return coat(2)
        return coat(2 if face == 'front' and x in (1, 2) else 1)
    if box.name == 'leg':
        if face == 'bottom':
            return LEATHER[0]
        if y >= 6:
            if y == 6:
                return LEATHER[2]
            if y == 9 and face == 'front' and x in (1, 2):
                return '#8a6a2a'
            return LEATHER[1] if face == 'front' and x in (1, 2) else LEATHER[0] if y >= 11 else LEATHER[1]
        return coat(0 if face in ('right', 'left', 'back') else 1)
    if box.name == 'cape':
        # tattered hem: the last rows are cut into points
        if face in ('front', 'back', 'left', 'right') and y >= h - 3:
            cut = (x * 7 + y * 3) % 5
            if y == h - 1 and cut < 3 or y == h - 2 and cut == 0:
                return None
        if face == 'front':
            return VEST[0] if noise > 0.15 else VEST[1]
        if face == 'back':
            c = coat(1)
            if 1 <= x <= w - 2 and (x + y // 3) % 4 == 0 and y > 2:
                c = coat(0)
            return c
        return coat(0)
    if box.name == 'coat':
        if face == 'bottom':
            return None
        if y == h - 1:
            return TRIM[1] if face in ('front', 'left', 'right') else TRIM[0]
        if face == 'front' and x == w // 2:
            return COAT[0]
        return coat(1 if face == 'front' else 0)
    if box.name == 'collar':
        if face == 'top':
            cx, cy = (w - 1) / 2, (h - 1) / 2
            r = math.hypot((x - cx) / (w / 2), (y - cy) / (h / 2))
            return '#ff7a1c' if r < 0.35 else '#7a1a0a' if r < 0.7 else COAT[2]
        if face == 'bottom':
            return COAT[0]
        if face == 'back':
            return TRIM[0] if y == 0 else COAT[1]
        return TRIM[1] if y == 0 else coat(1)
    if box.name == 'pauldron':
        if face == 'top':
            return IRON_DARK[2] if (x + y) % 4 == 0 else IRON_DARK[1]
        if y == h - 1:
            return IRON_DARK[0]
        if x in (1, w - 2) and y == 1:
            return '#c9a24a'
        return IRON_DARK[1]
    return None


def horseman_glow_painter(box, face, x, y, w, h):
    if box.name == 'collar' and face == 'top':
        cx, cy = (w - 1) / 2, (h - 1) / 2
        r = math.hypot((x - cx) / (w / 2), (y - cy) / (h / 2))
        return '#ffb040' if r < 0.35 else '#7a2a08' if r < 0.6 else None
    if box.name == 'body' and face == 'top':
        return '#5a1a06'
    if box.name == 'body' and face == 'front' and x == 4 and y in (2, 4, 6):
        return '#704010'
    if box.name == 'collar' and face in ('front', 'left', 'right') and y == 0:
        return '#3a1a06'
    return None


def horseman():
    return paint((64, 64), HORSEMAN_BOXES, horseman_painter)


def horseman_glow():
    return paint((64, 64), HORSEMAN_BOXES, horseman_glow_painter)


# ---------------------------------------------------------------------------------------------------------------------
# The Nightmare (64x64, vanilla horse layout): a black horse with ember cracks, a burning mane and tail, blazing eyes.

HORSE_BOXES = [
    Box('body', 0, 32, 10, 10, 22),
    Box('neck', 0, 35, 4, 12, 7),
    Box('head', 0, 13, 6, 5, 7),
    Box('mane', 56, 36, 2, 16, 2),
    Box('mouth', 0, 25, 4, 5, 5),
    Box('leg', 48, 21, 4, 11, 4),
    Box('tail', 42, 36, 3, 14, 4),
    Box('ear', 19, 16, 2, 3, 1),
]
HIDE = ['#0b080d', '#151019', '#211a27', '#2f2638']


def _crack(box, face, x, y):
    """Thin ember veins across the hide, from a deterministic pattern."""
    k = (x * 13 + y * 7 + len(box.name) * 31 + len(face) * 17) % 41
    return k == 0 or (k == 1 and (x + y) % 3 == 0)


def nightmare_painter(box, face, x, y, w, h):
    rng = Rng(hash(('n', box.name, face, x, y)) & 0xFFFF)
    noise = rng.random()

    def hide(level=1):
        c = HIDE[level]
        if noise < 0.1:
            c = HIDE[min(3, level + 1)]
        elif noise > 0.92:
            c = HIDE[max(0, level - 1)]
        return c

    if box.name == 'mane':
        t = y / max(1, h - 1)
        return mix('#ffd24a', '#c42a0c', t) if face != 'bottom' else '#c42a0c'
    if box.name == 'tail':
        t = y / max(1, h - 1)
        return mix('#ff9a2e', '#5a0f06', t * 0.8) if (x + y) % 5 else mix('#ffd24a', '#c42a0c', t)
    if box.name == 'leg':
        if y >= h - 2:
            return '#ff7a1c' if y == h - 2 else '#3a1a0a'
        if face == 'bottom':
            return '#2a0e06'
        return hide(1 if face in ('front', 'right') else 0)
    if box.name == 'head':
        if face in ('left', 'right') and y == 1 and x in (2, 3):
            return '#ffcf4a' if x == 2 else '#ff7a1c'
        return hide(1)
    if box.name == 'mouth':
        if face == 'front' and y >= 3:
            return '#5a1a0a' if y == 3 else '#0b080d'
        return hide(1)
    if box.name == 'ear':
        return hide(0)
    if box.name in ('body', 'neck'):
        if _crack(box, face, x, y) and face != 'bottom':
            return '#c4500e'
        if box.name == 'body' and face in ('left', 'right') and 3 <= y <= 7 and x % 4 == 1:
            # ribs showing through the hide
            return '#3a3040'
        return hide(0 if face == 'bottom' else 1)
    return hide(1)


def nightmare_glow_painter(box, face, x, y, w, h):
    if box.name == 'mane':
        return mix('#ffe08a', '#802008', y / max(1, h - 1))
    if box.name == 'tail':
        return mix('#ffb040', '#401004', y / max(1, h - 1))
    if box.name == 'head' and face in ('left', 'right') and y == 1 and x in (2, 3):
        return '#ffcf4a'
    if box.name == 'leg' and y == h - 2:
        return '#ff7a1c'
    if box.name in ('body', 'neck') and face != 'bottom' and _crack(box, face, x, y):
        return '#a03a0a'
    if box.name == 'mouth' and face == 'front' and y == 3:
        return '#601806'
    return None


def nightmare():
    return paint((64, 64), HORSE_BOXES, nightmare_painter)


def nightmare_glow():
    return paint((64, 64), HORSE_BOXES, nightmare_glow_painter)


# ---------------------------------------------------------------------------------------------------------------------
# Wisp glows: white radial falloffs, tinted by vertex colour and drawn additively (black is clear).

def radial(size, power, core=0.0):
    img = canvas(size, size)
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / (size / 2)
            if d >= 1:
                continue
            v = (1 - d) ** power
            if core and d < core:
                v = 1.0
            g = int(255 * min(1.0, v))
            put(img, x, y, (g, g, g, 255))
    return img


def flame_glow(size=32):
    """A teardrop of light, round at the bottom and drawn up into a point."""
    img = canvas(size, size)
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            nx = (x - c) / (size / 2)
            ny = (y - c) / (size / 2)
            # stretch the upper half into a point
            if ny < 0:
                nx = nx / max(0.15, 1 + ny * 0.85)
            d = math.hypot(nx, ny * 0.95)
            if d >= 1:
                continue
            g = int(255 * (1 - d) ** 1.4)
            put(img, x, y, (g, g, g, 255))
    return img


ENTITIES = {
    'spirit/spirit': spirit,
    'spirit/spirit_eyes': spirit_eyes,
    'hollow_horseman/horseman': horseman,
    'hollow_horseman/horseman_glow': horseman_glow,
    'hollow_horseman/nightmare': nightmare,
    'hollow_horseman/nightmare_glow': nightmare_glow,
    'wisp/halo': lambda: radial(32, 2.2),
    'wisp/flame': flame_glow,
    'wisp/core': lambda: radial(16, 1.2, core=0.25),
}
