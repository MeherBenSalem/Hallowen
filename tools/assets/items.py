"""Item textures (16x16): the eight candies, ectoplasm, the Treat Pail, the Witch Hat, the Reaper Scythe, the music
disc and the spawn eggs. Drawn as ASCII art with hand-picked ramps, in vanilla's style: soft shading and outlines
that are a dark shade of the object rather than black.
"""
from __future__ import annotations

from pixel import canvas, mix, put, rgb, stamp


def art(layers, palette):
    """Stamps several ASCII layers (later ones on top) with one palette."""
    img = canvas()
    for x, y, a in layers:
        stamp(img, x, y, a, palette)
    return img


# ---------------------------------------------------------------------------------------------------------------------
# Candy

CANDY_CORN_PAL = {
    'a': '#5c2a0c', 'b': '#7a3a12',
    'w': '#fff6e3', 'W': '#ffffff', 's': '#e2d2b4',
    'o': '#ff8a1c', 'O': '#ffb04f', 'p': '#d6600c',
    'y': '#ffd33a', 'Y': '#ffec8f', 'd': '#e09e1a', 'D': '#b97812',
}
CANDY_CORN_BACK = """
...aa...
..aWwa..
..awwsa.
.aOoopa.
.aOooopa
aYyyyyda
aYyyyyda
aDddddDa
.aaaaaa.
"""
CANDY_CORN_FRONT = """
....aa....
...aWwa...
...awwsa..
..aWwwwsa.
..aOooopa.
.aOooooopa
.aOooooopa
aYyyyyyyda
aYyyyyyyda
ayyyyyyyda
aDddddddDa
.aaaaaaaa.
"""


def candy_corn():
    return art([(1, 5, CANDY_CORN_BACK), (5, 2, CANDY_CORN_FRONT)], CANDY_CORN_PAL)


CARAMEL_APPLE = """
.......t........
.......T........
.......T........
.......t........
....acccccca....
...acCCccccca...
..acCcccccccsa..
..acccccccccsa..
.aaccsccccsccsa.
.arrcsrccsrcrra.
.arRrrrrcrrrrra.
.arRRrrrrrrrrra.
.arRrrrrrrrrhra.
..arrrrrrrrrha..
..aahrrrrrrhaa..
....aaaaaaaa....
"""
CARAMEL_APPLE_PAL = {
    'a': '#4a1410', 't': '#6b4a2a', 'T': '#c9a77a',
    'c': '#c8812a', 'C': '#f0b45a', 's': '#8a4c14',
    'r': '#d1352d', 'R': '#ff6f5c', 'h': '#9a1f1f',
}
SPRINKLES = [(5, 6, '#a65fe0'), (9, 5, '#7fd14f'), (11, 7, '#ff8a1c'), (6, 8, '#ff8a1c'), (8, 7, '#a65fe0')]


def caramel_apple():
    img = art([(0, 0, CARAMEL_APPLE)], CARAMEL_APPLE_PAL)
    for x, y, c in SPRINKLES:
        put(img, x, y, c)
    return img


GHOST_GUMMY = """
................
.....aaaaaa.....
....aWWggggaa...
...aWgggggggga..
...aWgggggggga..
..aWgkkgggkkgsa.
..aggkkgggkkgsa.
..agggggggggsga.
..agggggkkgggsa.
..aggggkggkgsga.
..agggggkkggssa.
..aggggggggsssa.
..agsgggsggsssa.
..asa.asga.asa..
..aa...aa...aa..
................
"""
GHOST_GUMMY_PAL = {
    'a': '#2f7a6c', 'g': (178, 246, 226, 225), 'W': (255, 255, 255, 240), 's': (120, 214, 192, 230),
    'k': '#1f4e49',
}


def ghost_gummy():
    return art([(0, 0, GHOST_GUMMY)], GHOST_GUMMY_PAL)


PUMPKIN_TRUFFLE = """
................
.......oo.......
......oOOo......
.....aooOoa.....
....accoocca....
...acCcccccca...
...aCcccccccsa..
..acccccccccsa..
..accccccccsssa.
..aoqoqoqoqoqa..
..aqoqoqoqoqoa..
...aoqoqoqoqa...
...aqoqoqoqoa...
....aoqoqoqa....
....aaaaaaaa....
................
"""
PUMPKIN_TRUFFLE_PAL = {
    'a': '#2a130a', 'c': '#5a2f1b', 'C': '#8f5634', 's': '#3a1c10',
    'o': '#ff9a2e', 'O': '#ffd08a', 'q': '#2a1a14',
}


def pumpkin_truffle():
    img = art([(0, 0, PUMPKIN_TRUFFLE)], PUMPKIN_TRUFFLE_PAL)
    # cup flutes: alternate orange/black stripes, with a lighter left side
    for y in range(9, 14):
        for x in range(2, 14):
            p = img.getpixel((x, y))
            if p[3] and rgb('#ff9a2e')[:3] == p[:3] and x <= 5:
                put(img, x, y, '#ffb35a')
    return img


LOLLIPOP_STICK = """
.....bb.........
....bkb.........
...ss...........
..sS............
.ss.............
.s..............
"""
LOLLIPOP_STICK_PAL = {'s': '#efe3c8', 'S': '#ffffff', 'b': '#1c1226', 'k': '#3e2a52'}


LOLLIPOP_DISC = """
...aaaa...
.aagggPaa.
.agPPPgPa.
agPggggPga
agPgPPgPga
agPgWPgPga
agPgggPPga
.aPPPPPga.
.aagggggaa
...aaaa...
"""
LOLLIPOP_PAL = {'a': '#2c1240', 'P': '#9b4fd6', 'g': '#9be05a', 'W': '#ffffff'}


def witch_lollipop():
    """A purple and lime swirl on a paper stick, tied with a black bow."""
    img = canvas()
    stamp(img, 6, 0, LOLLIPOP_DISC, LOLLIPOP_PAL)
    put(img, 8, 2, '#d8b8ff')
    put(img, 7, 3, '#d8b8ff')
    stamp(img, 0, 10, LOLLIPOP_STICK, LOLLIPOP_STICK_PAL)
    return img


BAT_WING = """
................
................
.a.....aa.....a.
.aa...akka...aa.
.aka..akka..aka.
.akka.aklka.akka
.akkkakkkkakkkka
.akkkkkrkkrkkkka
.aklkkkkkkkkklka
.akklkkkkkkklkka
..akkkkkkkkkkka.
..aka.akkka.aka.
...a..akka...a..
.......aa.......
................
................
"""
BAT_WING_PAL = {'a': '#0c080f', 'k': '#2a2033', 'l': '#6a5878', 'r': '#e0413a'}


def bat_wing_licorice():
    img = art([(0, 0, BAT_WING)], BAT_WING_PAL)
    # sugar sparkle
    for x, y in ((4, 6), (11, 6), (8, 10)):
        put(img, x, y, '#9a86aa')
    return img


EYEBALL = """
................
.....aaaaaa.....
....aWWwwwwaa...
...aWwwwrwwwsa..
..aWwwwrwwwwssa.
..awwwgGGgwwwsa.
..awrgGkkGgwwsa.
..awwgkkHkgrwsa.
..awwgGkkGgwssa.
..awwwgGGgwwssa.
..awwrwwwwwssda.
...awwwwwrssda..
....assssssda...
.....aaaaaa.....
................
................
"""
EYEBALL_PAL = {
    'a': '#5a4e48', 'w': '#f4f0ea', 'W': '#ffffff', 's': '#d4cbbe', 'd': '#a89c8e',
    'r': '#cf2f2f', 'g': '#2f8f45', 'G': '#5fc46a', 'k': '#101016', 'H': '#ffffff',
}


def eyeball_jawbreaker():
    return art([(0, 0, EYEBALL)], EYEBALL_PAL)


SOUL_TOFFEE = """
................
................
................
.aa..........aa.
aBba........abBa
abBba.aaaa.abBba
.abBaaCttCaaBba.
..abaCttfTCaba..
..abaCtfFTCaba..
.abBaaCttCaaBba.
abBba.aaaa.abBba
aBba........abBa
.aa..........aa.
................
................
................
"""
SOUL_TOFFEE_PAL = {
    'a': '#0f3d47', 'b': '#2aa6b8', 'B': '#9cf6ff',
    'C': '#8a5a24', 't': '#d09040', 'T': '#f0c070', 'f': '#5ce6f0', 'F': '#e6ffff',
}


def soul_toffee():
    return art([(0, 0, SOUL_TOFFEE)], SOUL_TOFFEE_PAL)


# ---------------------------------------------------------------------------------------------------------------------
# Other items

ECTOPLASM = """
................
.......aa.......
......agga......
.....agWgga.....
....agWgggga....
...agggggggga...
..agHgggggGgga..
..agggggggggga..
.agggGggggggggа.
.aggggggggggdga.
..aggdgggggdda..
..addaggggdda...
...aa.adgda.a...
.......ada..g...
........a.......
................
"""
ECTOPLASM_PAL = {
    'a': (31, 92, 38, 255), 'g': (122, 214, 74, 230), 'G': (200, 246, 140, 240), 'W': (240, 255, 210, 255),
    'H': (240, 255, 210, 255), 'd': (70, 150, 50, 235), 'а': (31, 92, 38, 255),
}


def ectoplasm():
    return art([(0, 0, ECTOPLASM)], ECTOPLASM_PAL)


TREAT_PAIL = """
......hHHh......
....hH....hh....
...h........h...
..h..........h..
..h..........h..
..aaaaaaaaaaaa..
.aRrrrrrrrrrrra.
.aoOooooooooopa.
.aOokkoooookkpa.
.aOkkkooookkkpa.
.aooooookoooopa.
.aokooooooookpa.
.aookkkkkkkkopa.
..aooookkoooa...
...aaaaaaaaaa...
................
"""
TREAT_PAIL_PAL = {
    'h': '#2e2a34', 'H': '#5e5868', 'a': '#5a2108', 'R': '#c25a12', 'r': '#a8480e',
    'o': '#ee8a2c', 'O': '#ffb35a', 'p': '#c4661a', 'k': '#3a1404',
}
PAIL_CANDY = """
.....c.y........
...cCwyyyp.vv...
..ccwwyypPvVvv..
"""
PAIL_CANDY_PAL = {'c': '#ff8a1c', 'C': '#ffb04f', 'w': '#fff6e3', 'y': '#ffd33a', 'p': '#9b4fd6', 'P': '#c48af0',
                  'v': '#2aa6b8', 'V': '#9cf6ff'}


def treat_pail(filled=False):
    img = art([(0, 0, TREAT_PAIL)], TREAT_PAIL_PAL)
    if filled:
        stamp(img, 0, 3, PAIL_CANDY, PAIL_CANDY_PAL)
        for x in range(2, 14):
            put(img, x, 5, '#5a2108')
    return img


WITCH_HAT = """
..........aa....
.........aKKa...
........aKka....
.......aKka.....
.......aKka.....
......aKkkka....
......aKkkka....
.....aKkkkkka...
.....aKkkkkka...
....abbbbBbbba..
....abbbbYbbba..
..aaKkkkkkkkkkaa
.aKKkkkkkkkkkkka
..aaaaaaaaaaaaa.
................
................
"""
WITCH_HAT_PAL = {'a': '#0d0812', 'K': '#4a3366', 'k': '#2a1c3a', 'b': '#7a3fb0', 'B': '#c48af0', 'Y': '#f0c24a'}


def witch_hat():
    return art([(0, 0, WITCH_HAT)], WITCH_HAT_PAL)


SCYTHE = """
................
.....aaaaaa.....
...aaSSSSSSaa...
..aSSssssssSSa..
.aSssddaaaadsSa.
aSsdaa.....aaSa.
SsEa.........kwa
sEa.........kwwa
Ea.........kpOk.
a.........kww.k.
.........kww....
........kww.....
.......kww......
......kgg.......
.....kgG........
....kgg.........
"""
SCYTHE_PAL = {
    'S': '#e4e8f0', 's': '#a7aebd', 'd': '#5f6577', 'a': '#262832', 'E': '#7fe9f0',
    'w': '#6e4a2c', 'k': '#2a1a10', 'g': '#5a2f7a', 'G': '#9b5fd0', 'p': '#ff8a1c', 'O': '#ffd08a',
}


def reaper_scythe():
    return art([(0, 0, SCYTHE)], SCYTHE_PAL)


DISC = """
................
................
................
................
....aaaaaaaa....
..aagggggggggaa.
.agggGGgggggggga
aggggggoOogggggga
agggggoopoogggga
.aggggggoogggggga
..aagggggggggaa.
....aaaaaaaa....
................
................
................
................
"""


def music_disc():
    pal = {'a': '#101014', 'g': '#26262e', 'G': '#4a4a58', 'o': '#ff8a1c', 'O': '#ffd08a', 'p': '#3a1404'}
    img = canvas()
    rows = [r[:16] for r in DISC.strip('\n').split('\n')]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                put(img, x, y, pal[ch])
    # grooves
    for x in range(3, 13, 3):
        put(img, x, 9, '#1c1c24')
    return img


EGG = """
................
......aaaa......
.....aHhhba.....
....aHhhhbba....
...aHhhhhhbba...
...ahhhhhhhba...
..aHhhhhhhhhba..
..ahhhhhhhhhba..
..ahhhhhhhhhba..
..ahhhhhhhhbba..
..abhhhhhhhbba..
...abhhhhhbba...
...abbbbbbbba...
....aabbbbaa....
......aaaa......
................
"""


def spawn_egg(base, shade, dark, outline, spots, spot_shade, highlight, pattern):
    pal = {'a': outline, 'h': base, 'H': highlight, 'b': shade}
    img = art([(0, 0, EGG)], pal)
    for x, y, kind in pattern:
        if img.getpixel((x, y))[3]:
            put(img, x, y, spots if kind == 's' else spot_shade if kind == 'S' else dark)
    return img


def spirit_egg():
    return spawn_egg('#cfeff2', '#8fc7cf', '#2f5c66', '#2f5c66', '#2f5c66', '#4f8f99', '#ffffff',
                     [(6, 6, 's'), (9, 6, 's'), (6, 7, 's'), (9, 7, 's'), (7, 10, 'S'), (8, 10, 'S'), (7, 9, 'S'), (8, 9, 'S'),
                      (4, 11, 'S'), (11, 11, 'S'), (5, 12, 'S'), (10, 12, 'S')])


def wisp_egg():
    return spawn_egg('#2a3a1c', '#1a2612', '#0e160a', '#0e160a', '#c8f07a', '#86cf45', '#4f6a30',
                     [(7, 7, 's'), (8, 7, 's'), (7, 8, 's'), (8, 8, 's'), (6, 8, 'S'), (9, 8, 'S'), (7, 6, 'S'), (8, 9, 'S'),
                      (4, 5, 'S'), (11, 10, 'S'), (5, 11, 's'), (10, 4, 's')])


def horseman_egg():
    return spawn_egg('#1c1622', '#120d17', '#0a070c', '#0a070c', '#ff8a1c', '#c4500e', '#3a2e44',
                     [(5, 6, 's'), (6, 6, 's'), (9, 6, 's'), (10, 6, 's'), (6, 7, 'S'), (9, 7, 'S'), (5, 9, 's'), (6, 10, 's'),
                      (7, 10, 's'), (8, 10, 's'), (9, 10, 's'), (10, 9, 's'), (7, 11, 'S'), (8, 11, 'S')])


ITEMS = {
    'candy_corn': candy_corn,
    'caramel_apple': caramel_apple,
    'ghost_gummy': ghost_gummy,
    'pumpkin_truffle': pumpkin_truffle,
    'witch_lollipop': witch_lollipop,
    'bat_wing_licorice': bat_wing_licorice,
    'eyeball_jawbreaker': eyeball_jawbreaker,
    'soul_toffee': soul_toffee,
    'ectoplasm': ectoplasm,
    'treat_pail': lambda: treat_pail(False),
    'treat_pail_filled': lambda: treat_pail(True),
    'witch_hat': witch_hat,
    'reaper_scythe': reaper_scythe,
    'music_disc_witching_hour': music_disc,
    'spirit_spawn_egg': spirit_egg,
    'will_o_wisp_spawn_egg': wisp_egg,
    'hollow_horseman_spawn_egg': horseman_egg,
}
