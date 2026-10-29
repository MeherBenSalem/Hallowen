"""Block textures: the Cursed Pumpkin, the Hallowed Cache and the Horseman's Head (with its animated flame), plus the
face overlays from faces.py. Pumpkin skins are drawn in the vanilla pumpkin's structure (vertical ribs with a lit
crest and shaded grooves) so they sit naturally beside vanilla blocks.
"""
from __future__ import annotations

import math

import faces
from pixel import Rng, canvas, mix, put, rgb, stamp, strip

# Rib layout of a pumpkin side, column by column: 0 groove, 1 flank, 2 crest.
RIBS = [0, 1, 2, 2, 1, 0, 1, 2, 2, 1, 1, 0, 1, 2, 2, 1]


def pumpkin_side(ramp, seed, cracks=None):
    """A ribbed pumpkin side in a four-step ramp (groove, flank, crest, highlight)."""
    rng = Rng(seed)
    img = canvas()
    for x in range(16):
        level = RIBS[x]
        for y in range(16):
            c = ramp[level]
            if y in (0, 15):
                c = ramp[max(0, level - 1)]
            if level == 2 and 2 <= y <= 12 and (x + y) % 7 == 0:
                c = ramp[3]
            if rng.random() < 0.06:
                c = mix(c, ramp[max(0, level - 1)], 0.5)
            put(img, x, y, c)
    if cracks:
        for points, colour in cracks:
            for x, y in points:
                put(img, x, y, colour)
    return img


def pumpkin_top(ramp, stem, seed):
    rng = Rng(seed)
    img = canvas()
    c = 7.5
    for y in range(16):
        for x in range(16):
            a = math.atan2(y - c, x - c)
            r = math.hypot(x - c, y - c)
            rib = (math.cos(a * 8) + 1) / 2
            level = 2 if rib > 0.6 else 1 if rib > 0.2 else 0
            colour = ramp[level]
            if r > 7.2:
                colour = ramp[0]
            if rng.random() < 0.05:
                colour = mix(colour, ramp[0], 0.5)
            put(img, x, y, colour)
    for x, y, col in ((7, 7, stem[1]), (8, 7, stem[0]), (7, 8, stem[0]), (8, 8, stem[0]), (6, 7, stem[0]), (9, 8, stem[1]),
                      (7, 6, stem[1]), (8, 9, stem[0])):
        put(img, x, y, col)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Cursed Pumpkin: a black-violet pumpkin with a sickly green grin that blazes when it wakes.

CURSED_RAMP = ['#120a1a', '#21142e', '#33204a', '#4f3670']
CURSED_FACE = """
................
................
.xx..........xx.
.xxxx......xxxx.
..xxxx....xxxx..
..xxx......xxx..
................
.......xx.......
................
.x............x.
.xx.xx.xx.xx.xx.
.xxxxxxxxxxxxxx.
..xx.xx..xx.xx..
...x..x..x..x...
................
................
"""


def cursed_side():
    vines = [([(2, 13), (3, 12), (3, 11), (4, 10), (4, 9)], '#2f5a1e'), ([(12, 2), (12, 3), (13, 4), (13, 5)], '#2f5a1e')]
    return pumpkin_side(CURSED_RAMP, 13, vines)


def cursed_top():
    return pumpkin_top(CURSED_RAMP, ['#1f3a14', '#3f6b24'], 14)


def cursed_front(awake):
    img = cursed_side()
    m = [[ch == 'x' for ch in row] for row in CURSED_FACE.strip('\n').split('\n')]
    pal = {'lip': '#1d3a12', 'edge': '#4f8a2a', 'mid': '#6fae36', 'core': '#86c442'} if not awake else \
        {'lip': '#4f9e2a', 'edge': '#a6f05a', 'mid': '#d4ff8a', 'core': '#f4ffd8'}
    for y in range(16):
        for x in range(16):
            if not m[y][x]:
                continue
            under = y == 0 or not m[y - 1][x]
            inner = all(0 <= x + dx < 16 and 0 <= y + dy < 16 and m[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            put(img, x, y, pal['lip'] if under else pal['core'] if inner else pal['edge'])
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Hallowed Cache: a small iron-bound coffer of dark wood with a glowing jack o'lantern seal.

WOOD = ['#24160d', '#3b2616', '#523520', '#6b472a']
IRON = ['#25272e', '#4a4e58', '#7c8290']


def planks(seed, vertical=False):
    rng = Rng(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            a, b = (x, y) if vertical else (y, x)
            board = a // 4
            seam = a % 4 == 3
            shade = WOOD[1] if board % 2 else WOOD[2]
            if seam:
                shade = WOOD[0]
            elif (b + board * 5) % 9 == 0:
                shade = WOOD[1] if shade == WOOD[2] else WOOD[0]
            elif rng.random() < 0.08:
                shade = WOOD[3] if shade == WOOD[2] else WOOD[2]
            put(img, x, y, shade)
    return img


def iron_band(img, horizontal_rows=(), vertical_cols=()):
    for y in horizontal_rows:
        for x in range(16):
            put(img, x, y, IRON[1])
            put(img, x, y + 1, IRON[0])
        for x in (1, 14):
            put(img, x, y, IRON[2])
    for x in vertical_cols:
        for y in range(16):
            put(img, x, y, IRON[1])
            put(img, x + 1, y, IRON[0])


SEAL = """
..aaaaaa..
.aoOoooopa
aoOkooookp
aokkooookp
aooookooop
aokoooookp
aookkkkoop
.aoooooopa
..aaaaaa..
"""


def cache_front(cursed=False):
    img = planks(31)
    iron_band(img, horizontal_rows=(2, 12))
    if cursed:
        pal = {'a': '#1a0e24', 'o': '#7a3fb0', 'O': '#b98af0', 'p': '#4e2a78', 'k': '#f0d8ff'}
    else:
        pal = {'a': '#3a1404', 'o': '#ee8a2c', 'O': '#ffb35a', 'p': '#c4661a', 'k': '#ffe766'}
    stamp(img, 3, 4, SEAL, pal)
    return img


def cache_side():
    img = planks(32)
    iron_band(img, horizontal_rows=(2, 12), vertical_cols=(7,))
    return img


def cache_top():
    img = planks(33, vertical=True)
    iron_band(img, horizontal_rows=(7,))
    for x, y, c in ((3, 3, '#c8501a'), (4, 3, '#e07a2a'), (4, 4, '#a83a12'), (11, 11, '#e0a030'), (12, 11, '#b8701a'),
                    (12, 12, '#8a4a14'), (10, 2, '#7a3fb0')):
        put(img, x, y, c)
    return img


def cache_bottom():
    return planks(34, vertical=True)


# ---------------------------------------------------------------------------------------------------------------------
# Horseman's Head: a charred pumpkin with ember cracks and a face full of fire.

CHAR_RAMP = ['#140906', '#24100a', '#3a1a0e', '#5a2a14']


def char_side(seed=41):
    rng = Rng(seed)
    img = pumpkin_side(CHAR_RAMP, seed)
    # glowing cracks wandering down the grooves
    for start in (5, 11):
        x, y = start, rng.randint(0, 4)
        while y < 16:
            put(img, x, y, '#ff7a1c' if rng.random() < 0.7 else '#ffc04a')
            y += 1
            if rng.random() < 0.3:
                x = max(0, min(15, x + rng.choice((-1, 1))))
            if rng.random() < 0.12:
                y += 2
    return img


def char_top():
    img = pumpkin_top(CHAR_RAMP, ['#0e0604', '#2a140a'], 42)
    for x, y in ((5, 5), (10, 4), (4, 10), (11, 10), (7, 3), (12, 7)):
        put(img, x, y, '#ff7a1c')
    return img


HEAD_FACE = """
................
................
..x.........x...
.xxx.......xxx..
.xxxx.....xxxx..
..xxxx...xxxx...
................
......xx........
.......x........
x..............x
xx.x.xx.xx.x.xx.
.xxxxxxxxxxxxxx.
..x.xx.xx.xx.x..
................
................
................
"""


def char_face():
    img = pumpkin_side(CHAR_RAMP, 43)
    m = [[ch == 'x' for ch in row] for row in HEAD_FACE.strip('\n').split('\n')]
    for y in range(16):
        for x in range(16):
            if not m[y][x]:
                continue
            under = y == 0 or not m[y - 1][x]
            inner = all(0 <= x + dx < 16 and 0 <= y + dy < 16 and m[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            put(img, x, y, '#c43a0c' if under else '#fff6c0' if inner else '#ffb02e')
    return img


def flame_frames(count=8, seed=44):
    """Licking flames for the crossed planes on top of the head: 16x16 frames, brightest at the base."""
    frames = []
    for f in range(count):
        img = canvas()
        phase = f / count * math.pi * 2
        for x in range(16):
            # several tongues of flame, each rising and falling out of step
            h = 0
            for k, (centre, width, height) in enumerate(((4, 2.6, 11), (8, 3.2, 14), (12, 2.4, 10))):
                sway = math.sin(phase + k * 2.1) * 1.2
                d = abs(x - centre - sway) / width
                if d < 1:
                    h = max(h, height * (1 - d * d) * (0.85 + 0.15 * math.sin(phase * 2 + k)))
            for y in range(16):
                depth = 15 - y
                if depth >= h:
                    continue
                t = depth / max(1.0, h)
                if t < 0.35:
                    c = mix('#fff4b0', '#ffd24a', t / 0.35)
                elif t < 0.75:
                    c = mix('#ffd24a', '#ff7a1c', (t - 0.35) / 0.4)
                else:
                    c = mix('#ff7a1c', '#c42a0c', (t - 0.75) / 0.25)
                put(img, x, y, c)
        frames.append(img)
    return frames


def head_flame():
    return strip(flame_frames())


# ---------------------------------------------------------------------------------------------------------------------

def face_overlays():
    """Every face overlay texture: block/<kind>_face_<name>."""
    out = {}
    for name in faces.ORDER:
        out[f'carved_face_{name}'] = faces.carved(name)
        out[f'lit_face_{name}'] = faces.lit(name)
        out[f'soul_face_{name}'] = faces.soul(name)
    return out


BLOCKS = {
    'cursed_pumpkin_side': cursed_side,
    'cursed_pumpkin_top': cursed_top,
    'cursed_pumpkin_front': lambda: cursed_front(False),
    'cursed_pumpkin_front_awake': lambda: cursed_front(True),
    'hallowed_cache_front': lambda: cache_front(False),
    'hallowed_cache_front_cursed': lambda: cache_front(True),
    'hallowed_cache_side': cache_side,
    'hallowed_cache_top': cache_top,
    'hallowed_cache_bottom': cache_bottom,
    'horseman_head_side': char_side,
    'horseman_head_top': char_top,
    'horseman_head_face': char_face,
}
