"""Carved pumpkin faces: eight designs, each drawn three ways (carved, lit, soul-lit).

A face is an ASCII mask over the 16x16 front of a pumpkin ('x' is cut away). The carved version shows the dark
inside of the pumpkin, darkest under the top lip of every cut; the lit versions glow from the edge inward, hottest in
the middle of each cut, like vanilla's jack o'lantern. Overlays are transparent everywhere but the cuts, so the
pumpkin underneath is whatever the resource pack says it is.
"""
from __future__ import annotations

from pixel import canvas, put

FACES = {
    # Upright triangle eyes, a small nose and a jagged three-tooth grin.
    'classic': """
................
................
...x.......x....
..xxx.....xxx...
.xxxxx...xxxxx..
.xxxxx...xxxxx..
.......x........
......xxx.......
................
.x............x.
.xxx.xxxxx.xxxx.
.xxxxxxxxxxxxxx.
..xxxx.xxx.xxx..
...xx...x...x...
................
................""",
    # Half-moon eyes and a huge grin of square teeth.
    'grin': """
................
................
..xxxx....xxxx..
.xxxxxx..xxxxxx.
.xxxxxx..xxxxxx.
................
................
x..............x
xx............xx
.xxx.xx..xx.xxx.
.xxxxxxxxxxxxxx.
..xxx.xxxx.xxx..
...xx..xx..xx...
....x......x....
................
................""",
    # Angry slanted eyes and a mouth with two long fangs.
    'fangs': """
................
................
.xx..........xx.
.xxxx......xxxx.
..xxxxx..xxxxx..
...xxxx..xxxx...
................
.......xx.......
................
..xxxxxxxxxxxx..
..xxxxxxxxxxxx..
..x.xxxxxxxx.x..
..x..xxxxxx..x..
.......xx.......
................
................""",
    # A cat: almond eyes with slit pupils, a little nose, a "w" mouth and whiskers.
    'cat': """
................
................
..xxxx....xxxx..
.xxx.xx..xx.xxx.
.xxx.xx..xx.xxx.
..xxxx....xxxx..
................
.......xx.......
........x.......
x.....xx.xx....x
.xx..x.....x.xx.
xx............xx
................
................
................
................""",
    # Tall hollow eyes, drooping brows and a gaping, screaming mouth.
    'scream': """
................
..x..........x..
...xx......xx...
..xxx......xxx..
..xxx......xxx..
..xxx......xxx..
...x........x...
................
......xxxx......
.....xxxxxx.....
.....xxxxxx.....
.....xxxxxx.....
.....xxxxxx.....
......xxxx......
................
................""",
    # One round eye, one winking, and a lopsided smirk.
    'wink': """
................
................
..xxx...........
.xxxxx..........
.xxxxx...xxxxx..
..xxx.....xxx...
................
................
.............xx.
.x..........xxx.
.xx.......xxxx..
..xxxxxxxxxxx...
....xxxxxxx.....
................
................
................""",
    # Skull: big hollow sockets, a nose hole and a stitched jaw.
    'skull': """
................
................
.xxxx......xxxx.
xxxxxx....xxxxxx
xxxxxx....xxxxxx
.xxxx......xxxx.
.......xx.......
......xxxx......
................
.xxxxxxxxxxxxxx.
.x.x.x.x.x.x.x..
.x.x.x.x.x.x.x..
.xxxxxxxxxxxxxx.
................
................
................""",
    # A bat carved across the whole face, wings spread.
    'bat': """
................
................
......x..x......
......xxxx......
x....xxxxxx....x
xx...xxxxxx...xx
xxxx.xxxxxx.xxxx
xxxxxxxxxxxxxxxx
.xxxxxxxxxxxxxx.
.xxxxxxxxxxxxxx.
..xx.xxxxxx.xx..
..x...xxxx...x..
.......xx.......
................
................
................""",
}

ORDER = ['classic', 'grin', 'fangs', 'cat', 'scream', 'wink', 'skull', 'bat']

CARVED = {'lip': '#2d0003', 'inside': '#441300', 'deep': '#3a1002'}
LIT = {'lip': '#c06a1c', 'edge': '#ffcb6a', 'mid': '#ffe766', 'core': '#fffcc9', 'wall': '#8a3f0c'}
SOUL = {'lip': '#1f6f86', 'edge': '#55d6e2', 'mid': '#a6f5f4', 'core': '#ecfffe', 'wall': '#5a2a10'}


def mask(name):
    rows = FACES[name].strip('\n').split('\n')
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), name
    return [[ch == 'x' for ch in row] for row in rows]


def _depth(m):
    """Distance (4-neighbourhood, capped at 3) from each cut pixel to the uncut pumpkin around it."""
    depth = [[0 if m[y][x] else -1 for x in range(16)] for y in range(16)]
    for level in range(1, 4):
        for y in range(16):
            for x in range(16):
                if depth[y][x] != level - 1:
                    continue
                inner = True
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if not (0 <= nx < 16 and 0 <= ny < 16) or not m[ny][nx] or depth[ny][nx] < level - 1:
                        inner = False
                if inner:
                    depth[y][x] = level
    return depth


def carved(name):
    m = mask(name)
    img = canvas()
    for y in range(16):
        for x in range(16):
            if not m[y][x]:
                continue
            under_lip = y == 0 or not m[y - 1][x]
            put(img, x, y, CARVED['lip'] if under_lip else CARVED['inside'])
    return img


def lit(name, pal=LIT):
    m = mask(name)
    d = _depth(m)
    img = canvas()
    for y in range(16):
        for x in range(16):
            if not m[y][x]:
                continue
            under_lip = y == 0 or not m[y - 1][x]
            if d[y][x] >= 2:
                colour = pal['core']
            elif d[y][x] == 1:
                colour = pal['mid']
            else:
                colour = pal['lip'] if under_lip else pal['edge']
                # Thin cuts (two pixels or less across) are all glow: there is no edge to see past.
                wide = sum(1 for dx in (-1, 1) if 0 <= x + dx < 16 and m[y][x + dx])
                tall = sum(1 for dy in (-1, 1) if 0 <= y + dy < 16 and m[y + dy][x])
                if wide == 0 or tall == 0:
                    colour = pal['mid'] if not under_lip or tall == 0 else pal['lip']
            put(img, x, y, colour)
    return walled(img, name, pal['wall'])


def soul(name):
    return lit(name, SOUL)


def walled(img, name, colour):
    """Darkens the pumpkin skin around each cut (the cut's inner wall), so a glowing face reads from afar."""
    m = mask(name)
    for y in range(16):
        for x in range(16):
            if m[y][x]:
                continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and m[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                put(img, x, y, colour)
    return img
