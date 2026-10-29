"""Particles, status-effect icons, HUD textures (vignette, Spook flash, title ornament), the advancement background
and the mod's logo and icon."""
from __future__ import annotations

import math

from PIL import Image, ImageDraw, ImageFilter

from pixel import Rng, canvas, mix, put, rgb, stamp

# ---------------------------------------------------------------------------------------------------------------------
# Particles (8x8). Glowing ones are white so the provider's colour tints them.


def ember(frame):
    """A spark that dims as it rises: frame 0 is the brightest."""
    img = canvas(8, 8)
    shapes = [
        [(3, 3, 255), (4, 3, 230), (3, 4, 230), (4, 4, 255), (2, 3, 120), (5, 4, 120), (3, 2, 120), (4, 5, 120)],
        [(3, 3, 255), (4, 4, 230), (3, 4, 200), (4, 3, 200), (2, 4, 90), (5, 3, 90)],
        [(3, 3, 230), (4, 4, 200), (4, 3, 150)],
        [(4, 4, 200)],
    ]
    for x, y, v in shapes[frame]:
        put(img, x, y, (255, 255, 255, v))
    return img


LEAF_SHAPES = [
    """
...aa...
..abba..
.abbcba.
.abcbba.
..abba..
...ab...
....a...
........
""",
    """
........
.aa..aa.
.abaaba.
..abcba.
.abbcba.
..abba..
...aa...
....a...
""",
    """
....aa..
...abba.
..abcba.
.abcbba.
.abba...
..aa....
.a......
........
""",
    """
........
..aaa...
.abbba..
.abcbbaa
.abbcba.
..abbba.
...aaa..
........
""",
]
LEAF_COLOURS = [('#8a2a0e', '#c8501a', '#e8843a'), ('#8a4a0e', '#d8902a', '#f4c060'), ('#5a1a0e', '#a8301a', '#d8582a'),
                ('#6a3a14', '#a86a2a', '#c8904a')]


def leaf(index):
    a, b, c = LEAF_COLOURS[index]
    img = canvas(8, 8)
    stamp(img, 0, 0, LEAF_SHAPES[index], {'a': a, 'b': b, 'c': c})
    return img


def soft_dot(frame, count=4):
    """A round soft glow that shrinks over its life."""
    img = canvas(8, 8)
    r = 3.6 - frame * 0.7
    for y in range(8):
        for x in range(8):
            d = math.hypot(x - 3.5, y - 3.5)
            if d < r:
                v = int(255 * min(1.0, (1 - d / r) * 1.6))
                put(img, x, y, (255, 255, 255, v))
    return img


def wisp_flame(frame):
    img = canvas(8, 8)
    h = [5, 6, 5, 4][frame]
    for y in range(8):
        for x in range(8):
            nx = (x - 3.5) / 3.0
            ny = (y - 4.5) / h * 2
            if ny < 0:
                nx /= max(0.2, 1 + ny * 0.8)
            d = math.hypot(nx, ny)
            if d < 1:
                put(img, x, y, (255, 255, 255, int(255 * min(1.0, (1 - d) * 1.8))))
    return img


def sparkle(frame):
    img = canvas(8, 8)
    arm = [3, 2, 1, 1][frame]
    put(img, 3, 3, (255, 255, 255, 255))
    put(img, 4, 4, (255, 255, 255, 255))
    put(img, 3, 4, (255, 255, 255, 255))
    put(img, 4, 3, (255, 255, 255, 255))
    for i in range(1, arm + 1):
        v = 255 - i * 60
        for x, y in ((3 - i, 3), (4 + i, 4), (3, 3 - i), (4, 4 + i)):
            put(img, x, y, (255, 255, 255, v))
    return img


PARTICLES = {
    'ember': [ember(i) for i in range(4)],
    'autumn_leaf': [leaf(i) for i in range(4)],
    'wisp_flame': [wisp_flame(i) for i in range(4)],
    'soul_mote': [soft_dot(i) for i in range(4)],
    'candy_sparkle': [sparkle(i) for i in range(4)],
}

# ---------------------------------------------------------------------------------------------------------------------
# Status effect icons (18x18).

SUGAR_RUSH = """
..................
..........aa......
.........abba.....
........abwba.....
.......abwwba.....
......abwwwba.....
.....aoooooba.....
....aoOoooopa.....
....aOoooooopa....
...ayYyyyyyyda....
...aYyyyyyyyda..s.
...ayyyyyyyyda.sS.
...adddddddda...s.
....aaaaaaaa......
..s...............
.sSs..............
..s...............
..................
"""
SUGAR_CRASH = """
..................
..................
....aaaaaaaa......
...adddddddda.....
...ayyyyyyyyda....
...aYyyyyyyyda....
....aOoooooopa....
....aoooooopa.....
.....aoooopa......
......awwba.......
.......aba........
........a.........
..................
....z.............
...zzz..z.........
....z..zzz....z...
.......z.z...zzz..
..................
"""
SPOOKED = """
..................
......aaaaaa......
....aawwwwwwaa....
...awwwwwwwwwwa...
..awwwwwwwwwwwwa..
..awwkkwwwwkkwwa..
..awkkkkwwkkkkwa..
..awkkkkwwkkkkwa..
..awwkkwwwwkkwwa..
..awwwwwkkwwwwwa..
..awwwwkkkkwwwwa..
..awwwwkkkkwwwwa..
..awwwwwkkwwwwwa..
..awwwwwwwwwwwwa..
..awwawwwawwwawa..
..awa.awa.awa.aa..
..aa...a...a......
..................
"""
SPIRIT_WARD = """
..................
........aa........
.......aFFa.......
......afFFfa......
......afFffa......
.....afFFffa......
.....afFfffa......
....afFFfffffa....
....aSSSSSSSSa....
...aSssSSSSssSa...
...aSsSSSSSSsSa...
...aSssSSSSssSa...
....aSSSSSSSSa....
.....aSSSSSSa.....
......aSSSSa......
.......aSSa.......
........aa........
..................
"""


def effect_icons():
    candy = {'a': '#5c2a0c', 'w': '#fff6e3', 'b': '#e2d2b4', 'o': '#ff8a1c', 'O': '#ffb04f', 'p': '#d6600c', 'y': '#ffd33a',
             'Y': '#ffec8f', 'd': '#e09e1a', 's': '#ffe98a', 'S': '#ffffff'}
    crash = dict(candy, o='#a8783a', O='#c8985a', p='#8a5a24', y='#b8a060', Y='#d0b878', d='#8a7840', z='#8a7aa8')
    ghost = {'a': '#2a3a4a', 'w': '#eef8ff', 'k': '#1a1a2a'}
    ward = {'a': '#0f3d47', 'f': '#2aa6b8', 'F': '#9cf6ff', 'S': '#4fd2de', 's': '#ecfffe'}
    out = {}
    for name, art, pal in (('sugar_rush', SUGAR_RUSH, candy), ('sugar_crash', SUGAR_CRASH, crash), ('spooked', SPOOKED, ghost),
                           ('spirit_ward', SPIRIT_WARD, ward)):
        img = canvas(18, 18)
        stamp(img, 0, 0, art, pal)
        out[name] = img
    return out


# ---------------------------------------------------------------------------------------------------------------------
# HUD textures.

def vignette(size=256):
    """White, clear in the middle and opaque at the edges (tinted and faded by the overlay)."""
    img = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    px = img.load()
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            nx, ny = (x - c) / c, (y - c) / c
            d = math.hypot(nx * 0.92, ny * 1.05)
            v = max(0.0, (d - 0.45) / 0.85)
            px[x, y] = (255, 255, 255, int(255 * min(1.0, v ** 1.6)))
    return img


def spook(size=128):
    """The Spook flash: a pale screaming face that blooms out of the dark."""
    s = 4
    big = Image.new('RGBA', (size * s, size * s), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    W = size * s
    # head: an oval narrowing into a ragged shroud
    d.ellipse((W * 0.18, W * 0.05, W * 0.82, W * 0.75), fill=(232, 244, 248, 235))
    pts = [(W * 0.2, W * 0.45), (W * 0.8, W * 0.45), (W * 0.86, W * 0.98), (W * 0.74, W * 0.86), (W * 0.62, W * 0.99),
           (W * 0.5, W * 0.87), (W * 0.38, W * 0.99), (W * 0.26, W * 0.86), (W * 0.14, W * 0.98)]
    d.polygon(pts, fill=(222, 238, 244, 225))
    # eyes and mouth
    d.ellipse((W * 0.3, W * 0.25, W * 0.45, W * 0.45), fill=(12, 16, 26, 255))
    d.ellipse((W * 0.55, W * 0.25, W * 0.70, W * 0.45), fill=(12, 16, 26, 255))
    d.ellipse((W * 0.40, W * 0.52, W * 0.60, W * 0.80), fill=(10, 12, 20, 255))
    d.ellipse((W * 0.35, W * 0.31, W * 0.39, W * 0.35), fill=(170, 240, 255, 255))
    d.ellipse((W * 0.60, W * 0.31, W * 0.64, W * 0.35), fill=(170, 240, 255, 255))
    big = big.filter(ImageFilter.GaussianBlur(s * 0.8))
    img = big.resize((size, size), Image.LANCZOS)
    # pixelate to match the game's look
    return img.resize((size // 2, size // 2), Image.NEAREST).resize((size, size), Image.NEAREST)


ORNAMENT = """
.......gg.......
......gg........
....aaaaaaaa....
..aaoOOooooopaa.
.aoOOoooooooopa.
aoOoykoooookyopa
aoOokkyoooykkopa
aoooooookoooooopa
aoooooookoooooopa
aoyooooooooooyopa
aooykyykkyykyoopa
.aooyykyykyyoopa.
..aaoooooooopaa..
....aaaaaaaa.....
................
................
"""


def ornament():
    img = canvas(16, 16)
    stamp(img, 0, 0, ORNAMENT, {'g': '#3f6b24', 'a': '#5a2108', 'o': '#ee8a2c', 'O': '#ffb35a', 'p': '#c4661a',
                                'k': '#3a1404', 'y': '#ffe766'})
    return img


def adv_background():
    """A dark violet night tile with a few stars, for the advancement tab."""
    rng = Rng(77)
    img = canvas(16, 16)
    for y in range(16):
        for x in range(16):
            c = mix('#1a1026', '#120a1a', y / 15)
            if rng.random() < 0.08:
                c = mix(c, '#2a1a3a', 0.6)
            put(img, x, y, c)
    for x, y in ((3, 4), (11, 2), (8, 11), (14, 13)):
        put(img, x, y, '#6a5a8a')
    put(img, 11, 2, '#c8b8e8')
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Logo and icon: a grinning jack o'lantern under a swollen harvest moon, a bat crossing it.


def logo(size=128):
    s = 4
    W = size * s
    img = Image.new('RGBA', (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # rounded night badge
    d.rounded_rectangle((0, 0, W - 1, W - 1), radius=W * 0.18, fill=(20, 12, 30, 255))
    # sky gradient
    sky = Image.new('RGBA', (W, W))
    sp = sky.load()
    for y in range(W):
        t = y / W
        c = mix('#2a1440', '#0c0714', t)
        for x in range(W):
            sp[x, y] = c
    mask = Image.new('L', (W, W), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, W - 1, W - 1), radius=W * 0.18, fill=255)
    img.paste(sky, (0, 0), mask)
    d = ImageDraw.Draw(img)
    # harvest moon with a glow
    glow = Image.new('RGBA', (W, W), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((W * 0.18, W * 0.02, W * 0.86, W * 0.70), fill=(255, 150, 50, 120))
    glow = glow.filter(ImageFilter.GaussianBlur(W * 0.06))
    img.alpha_composite(Image.composite(glow, Image.new('RGBA', (W, W), (0, 0, 0, 0)), mask))
    d.ellipse((W * 0.27, W * 0.09, W * 0.77, W * 0.59), fill=(255, 176, 77, 255))
    d.ellipse((W * 0.36, W * 0.18, W * 0.46, W * 0.26), fill=(240, 150, 60, 255))
    d.ellipse((W * 0.58, W * 0.34, W * 0.66, W * 0.41), fill=(240, 150, 60, 255))
    # bat across the moon
    bx, by, bs = W * 0.56, W * 0.22, W * 0.11
    bat = [(bx - bs * 1.6, by), (bx - bs * 1.0, by - bs * 0.35), (bx - bs * 0.55, by - bs * 0.05), (bx - bs * 0.2, by - bs * 0.45),
           (bx, by - bs * 0.15), (bx + bs * 0.2, by - bs * 0.45), (bx + bs * 0.55, by - bs * 0.05), (bx + bs * 1.0, by - bs * 0.35),
           (bx + bs * 1.6, by), (bx + bs * 1.0, by + bs * 0.05), (bx + bs * 0.55, by + bs * 0.4), (bx, by + bs * 0.2),
           (bx - bs * 0.55, by + bs * 0.4), (bx - bs * 1.0, by + bs * 0.05)]
    d.polygon(bat, fill=(26, 14, 36, 255))
    # jack o'lantern
    cx, cy = W * 0.5, W * 0.72
    rx, ry = W * 0.36, W * 0.24
    for i, (ox, sc, col) in enumerate(((-0.55, 0.62, (196, 92, 22)), (0.55, 0.62, (196, 92, 22)), (-0.25, 0.82, (226, 116, 30)),
                                        (0.25, 0.82, (226, 116, 30)), (0, 0.9, (240, 132, 38)))):
        d.ellipse((cx + ox * rx - rx * sc * 0.55, cy - ry, cx + ox * rx + rx * sc * 0.55, cy + ry), fill=col)
    d.rounded_rectangle((cx - W * 0.03, cy - ry - W * 0.07, cx + W * 0.03, cy - ry + W * 0.02), radius=W * 0.01, fill=(60, 96, 34))
    glowc = (255, 226, 110, 255)
    d.polygon([(cx - W * 0.2, cy - W * 0.03), (cx - W * 0.12, cy - W * 0.13), (cx - W * 0.05, cy - W * 0.03)], fill=glowc)
    d.polygon([(cx + W * 0.2, cy - W * 0.03), (cx + W * 0.12, cy - W * 0.13), (cx + W * 0.05, cy - W * 0.03)], fill=glowc)
    mouth = [(cx - W * 0.24, cy + W * 0.03), (cx - W * 0.16, cy + W * 0.06), (cx - W * 0.13, cy + W * 0.02), (cx - W * 0.08, cy + W * 0.07),
             (cx - W * 0.03, cy + W * 0.03), (cx + W * 0.03, cy + W * 0.07), (cx + W * 0.08, cy + W * 0.03), (cx + W * 0.13, cy + W * 0.07),
             (cx + W * 0.16, cy + W * 0.03), (cx + W * 0.24, cy + W * 0.03), (cx + W * 0.16, cy + W * 0.15), (cx, cy + W * 0.18),
             (cx - W * 0.16, cy + W * 0.15)]
    d.polygon(mouth, fill=glowc)
    # rim
    ImageDraw.Draw(img).rounded_rectangle((W * 0.01, W * 0.01, W * 0.99, W * 0.99), radius=W * 0.18, outline=(238, 138, 44, 255),
                                          width=int(W * 0.025))
    return img.resize((size, size), Image.LANCZOS)


GUI = {
    'gui/vignette': vignette,
    'gui/spook': spook,
    'gui/ornament': ornament,
    'gui/advancements/backgrounds/hallowed': adv_background,
}
