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


# ---------------------------------------------------------------------------------------------------------------------
# Mod icon: the Horseman's Head (the real block textures, drawn as an inventory-style isometric block with its flame)
# in front of a pixel-art Harvest Moon on a Hallowed Night sky. Built on a 64x64 pixel grid and enlarged without
# smoothing, so it reads as pixel art at every size.

BAYER4 = ((0, 8, 2, 10), (12, 4, 14, 6), (3, 11, 1, 9), (15, 7, 13, 5))


def _logo_sky(n=64):
    """The backdrop at n x n: dithered violet sky, the moon with its halo, stars, bats and a churchyard horizon."""
    img = Image.new('RGBA', (n, n))
    px = img.load()
    bands = [rgb(c) for c in ('#0d0716', '#160b24', '#211031', '#2e143c', '#3d1a43', '#552443')]
    for y in range(n):
        t = y / (n - 1) * (len(bands) - 1)
        for x in range(n):
            i = min(len(bands) - 2, int(t))
            f = t - i
            px[x, y] = bands[i + (1 if f * 16 > BAYER4[y % 4][x % 4] else 0)]
    cx, cy, r = 22, 21, 15
    for y in range(n):
        for x in range(n):
            d = math.hypot(x - cx + 0.5, y - cy + 0.5)
            if r < d <= r + 1.6:
                px[x, y] = rgb('#9a3a30')
            elif r + 1.6 < d <= r + 3.8 and (x + y) % 2 == 0:
                px[x, y] = rgb('#5e2238')
    moon = {'rim': '#ffcf7a', 'body': '#ffb54d', 'shade': '#f08d2c', 'deep': '#d06a1c'}
    for y in range(n):
        for x in range(n):
            dx, dy = x - cx + 0.5, y - cy + 0.5
            d = math.hypot(dx, dy)
            if d > r:
                continue
            # light from the upper left; the lower right edge falls into shade
            lit = (-dx - dy) / (r * 1.4)
            c = moon['body']
            if d > r - 1.2 and lit > 0:
                c = moon['rim']
            elif lit < -0.45:
                c = moon['shade'] if (lit > -0.62 or (x + y) % 2) else moon['deep']
            px[x, y] = rgb(c)
    for (mx, my, mr) in ((16, 15, 2.8), (27, 26, 2.4), (28, 13, 1.6), (17, 27, 1.5)):
        for y in range(n):
            for x in range(n):
                if math.hypot(x - mx + 0.5, y - my + 0.5) <= mr and math.hypot(x - cx + 0.5, y - cy + 0.5) <= r - 1:
                    px[x, y] = rgb('#e7953a')
    for (x, y) in ((44, 6), (52, 3), (58, 9), (61, 21), (40, 15), (49, 26), (3, 41), (59, 34), (4, 4), (36, 2)):
        px[x, y] = rgb('#ffe6c2')
    for (x, y) in ((50, 15), (6, 33)):
        for ox, oy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            px[x + ox, y + oy] = rgb('#fff3dc' if (ox, oy) == (0, 0) else '#a98bb8')
    bat = ['X.......X', 'XX.X.X.XX', '.XXXXXXX.', '..X...X..']
    for bx, by in ((56, 25), (42, 8)):
        for j, row in enumerate(bat):
            for i, ch in enumerate(row):
                if ch == 'X':
                    px[bx + i - 4, by + j] = rgb('#120818')
    # a low hill with two leaning gravestones and a crooked fence along the bottom
    for x in range(n):
        h = 54 + round(2.2 * math.sin(x * 0.11 + 0.6) + 1.2 * math.sin(x * 0.29))
        for y in range(h, n):
            px[x, y] = rgb('#0b0510' if y > h else '#1b0d22')
    for gx, gy, w, hgt in ((6, 46, 5, 8), (54, 47, 4, 7)):
        for y in range(gy, gy + hgt):
            for x in range(gx, gx + w):
                top = y == gy and x in (gx, gx + w - 1)
                if not top:
                    px[x, y] = rgb('#2b1d33' if x == gx else '#1d1225')
    return img


def _iso_face(dst, tex, origin, u, v, shade=1.0, keep_bright=False):
    """Draws a 16x16 texture onto the parallelogram origin + s*u + t*v (s, t in 0..1) with nearest sampling."""
    tex = tex.convert('RGBA')
    ux, uy = u
    vx, vy = v
    det = ux * vy - uy * vx
    w, h = dst.size
    out = dst.load()
    src = tex.load()
    xs = [origin[0], origin[0] + ux, origin[0] + vx, origin[0] + ux + vx]
    ys = [origin[1], origin[1] + uy, origin[1] + vy, origin[1] + uy + vy]
    for y in range(max(0, int(min(ys))), min(h, int(max(ys)) + 1)):
        for x in range(max(0, int(min(xs))), min(w, int(max(xs)) + 1)):
            px, py = x + 0.5 - origin[0], y + 0.5 - origin[1]
            s = (px * vy - py * vx) / det
            t = (ux * py - uy * px) / det
            if not (0 <= s < 1 and 0 <= t < 1):
                continue
            c = src[int(s * 16), int(t * 16)]
            if c[3] < 128:
                continue
            k = shade
            if keep_bright and (c[0] + c[1]) > 330:
                k = 1.0
            out[x, y] = (int(c[0] * k), int(c[1] * k), int(c[2] * k), 255)


def _logo_head(scale, frame=5):
    """The Horseman's Head as an inventory block on a 256 canvas, texel steps (2, 1) * scale across, 2.5 * scale up."""
    from blocks import BLOCKS, head_flame
    side, top, face = BLOCKS['horseman_head_side'](), BLOCKS['horseman_head_top'](), BLOCKS['horseman_head_face']()
    flame = head_flame().crop((0, frame * 16, 16, frame * 16 + 16))
    W = 256
    img = Image.new('RGBA', (W, W), (0, 0, 0, 0))
    ax, ay, up = 2 * scale * 16, 1 * scale * 16, int(2.5 * scale * 16)
    bx, by = W // 2 + 12, W - 24
    _iso_face(img, face, (bx - ax, by - ay - up), (ax, ay), (0, up), shade=0.86, keep_bright=True)
    _iso_face(img, side, (bx, by - up), (ax, -ay), (0, up), shade=0.6)
    _iso_face(img, top, (bx - ax, by - up - ay), (ax, -ay), (ax, ay), shade=1.0)
    # The flame is a pair of crossed planes through the middle of the head (model y 12..26); in this view one faces
    # the camera, so it draws as a billboard. Only what rises above the top face shows.
    fw, fh = 7 * 16 * scale // 2, 5 * 16 * scale // 2
    fl = flame.resize((fw, fh), Image.NEAREST)
    centre = by - up - ay
    base = centre + 4 * 5 * scale // 2
    visible = fl.crop((0, 0, fw, fh - (base - centre)))
    img.alpha_composite(visible, (bx - fw // 2, base - fh))
    return img


def _logo_glow(n=64):
    """Ember light the head throws on the hill under it, as dithered pixels."""
    img = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    px = img.load()
    for y in range(52, n):
        for x in range(n):
            d = math.hypot((x - 35) / 1.7, (y - 58) / 0.8)
            if d < 7:
                px[x, y] = rgb('#6e2616')
            elif d < 11 and (x + y) % 2 == 0:
                px[x, y] = rgb('#4a1a18')
    return img


def logo(size=128):
    """The mod icon: 256 is native (4 screen pixels per sky pixel); other sizes are resampled from it."""
    sky = _logo_sky()
    sky.alpha_composite(_logo_glow())
    big = sky.resize((256, 256), Image.NEAREST)
    big.alpha_composite(_logo_head(2))
    # a two-pixel frame: dark outside, ember inside, with clipped corners
    px = big.load()
    for y in range(256):
        for x in range(256):
            e = min(x, y, 255 - x, 255 - y)
            corner = min(x, 255 - x) + min(y, 255 - y)
            if corner < 12:
                px[x, y] = (0, 0, 0, 0)
            elif e < 4 or corner < 16:
                px[x, y] = rgb('#120818')
            elif e < 8 or corner < 20:
                px[x, y] = rgb('#c4501a')
    if size == 256:
        return big
    return big.resize((size, size), Image.NEAREST if size % 256 == 0 or 256 % size == 0 and size >= 256 else Image.LANCZOS)


GUI = {
    'gui/vignette': vignette,
    'gui/spook': spook,
    'gui/ornament': ornament,
    'gui/advancements/backgrounds/hallowed': adv_background,
}
