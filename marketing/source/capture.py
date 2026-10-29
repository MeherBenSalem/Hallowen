#!/usr/bin/env python3
"""Takes the listing screenshots and GIFs in marketing/images/ from a running QA server and client.

Start them first (26.3, either loader), with the QA world and its seed:

    tools/qa/prepare_runs.sh 26.3 neoforge tools/qa/options.txt
    tools/qa/devrun.sh start-server 26.3 neoforge
    tools/qa/devrun.sh start-client 26.3 neoforge

then run `python3 marketing/source/capture.py [shot...]`. Every position below is in the QA world (seed
`hallowed-qa`): the plains village around its bell at -729 72 -995, the field east of it and the farm south-east.
Natural spawning is switched off while shooting, and actors are summoned with NoAI so they hold still.
"""
from __future__ import annotations

import math
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools' / 'qa'))
from client import PLAYER, Session  # noqa: E402

OUT = ROOT / 'marketing' / 'images' / 'screenshots'
GIFS = ROOT / 'marketing' / 'images' / 'gifs'
WORK = Path('/tmp/hallowed-capture')

s: Session


def view(x, y, z, tx, ty, tz, wait=3.0):
    s.tp(x, y, z, facing=(tx, ty, tz))
    time.sleep(wait)


def shot(name, hud=False):
    s.hud(hud)
    time.sleep(0.4)
    WORK.mkdir(parents=True, exist_ok=True)
    png = s.shot(WORK / f'{name}.png')
    s.hud(True)
    from PIL import Image
    OUT.mkdir(parents=True, exist_ok=True)
    Image.open(png).convert('RGB').save(OUT / f'{name}.jpg', quality=90, optimize=True, progressive=True)
    print('captured', name)


def record(name, seconds, crop, width, fps, action):
    """Records the screen while `action` runs, then encodes a palette-optimised GIF of the cropped region."""
    WORK.mkdir(parents=True, exist_ok=True)
    mp4 = WORK / f'{name}.mp4'
    rec = subprocess.Popen(['ffmpeg', '-y', '-loglevel', 'error', '-f', 'x11grab', '-framerate', '20', '-video_size', '1920x1080',
                            '-i', s.env['DISPLAY'] + '.0', '-t', str(seconds), '-c:v', 'libx264', '-preset', 'ultrafast', '-crf', '12', str(mp4)],
                           env=s.env)
    time.sleep(0.6)
    action()
    rec.wait()
    GIFS.mkdir(parents=True, exist_ok=True)
    w, h, x, y = crop
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-ss', '0.3', '-i', str(mp4), '-vf',
                    f'crop={w}:{h}:{x}:{y},fps={fps},scale={width}:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=150:stats_mode=diff[p];'
                    '[b][p]paletteuse=dither=bayer:bayer_scale=4:diff_mode=rectangle', str(GIFS / f'{name}.gif')], check=True)
    print('recorded', name)


def at_surface(x, z, block, on='#minecraft:dirt'):
    """Places a block on the ground at x, z, only where the ground is `on`, so running the script again adds nothing."""
    s.cmd(f'execute positioned {x} 200 {z} positioned over motion_blocking_no_leaves if block ~ ~-1 ~ {on} run setblock ~ ~ ~ {block}')


def lantern(x, z, face, kind='jack_o_lantern', facing='south', on='#minecraft:dirt'):
    at_surface(x, z, f'hallowed:{kind}[facing={facing},face={face}]', on)


def head(item, face):
    return f'{{id:"hallowed:{item}",count:1,components:{{"minecraft:block_state":{{face:"{face}"}}}}}}'


def still(entity, x, y, z, yaw, extra=''):
    s.cmd(f'summon {entity} {x} {y} {z} {{NoAI:1b,PersistenceRequired:1b,Rotation:[{yaw}f,0f]{extra}}}')


def clear_actors():
    for kind in ('hallowed:hollow_horseman', 'hallowed:spirit', 'hallowed:will_o_wisp', 'zombie', 'skeleton', 'villager', 'zombie_villager'):
        s.cmd(f'tp @e[type={kind},x=-729,y=72,z=-995,distance=..120] ~ -100 ~')
    time.sleep(1)
    s.cmd('kill @e[type=item]')


def night(time_of_day=18600):
    s.cmd('gamerule advance_time false', f'time set {time_of_day}', 'hallowed night start', 'weather clear 100000')


def setup():
    s.cmd('gamerule spawn_mobs false', 'gamerule spawn_monsters false', f'gamemode spectator {PLAYER}', f'effect clear {PLAYER}')
    s.focus()
    clear_actors()
    night()
    # The bell yard: lanterns on both sides of the path, a hay bale lantern and a small pumpkin patch.
    for x, z, face in ((-743, -990, 2), (-736, -990, 5), (-744, -994, 7), (-736, -994, 1)):
        lantern(x, z, face)
    lantern(-741, -999, 3, 'soul_jack_o_lantern')
    at_surface(-745, -997, 'minecraft:pumpkin')
    at_surface(-746, -996, 'minecraft:pumpkin')
    lantern(-745, -995, 6, 'carved_pumpkin')
    at_surface(-733, -990, 'minecraft:hay_block')
    lantern(-733, -990, 4, on='minecraft:hay_block')
    for grass in ('short_grass', 'tall_grass'):
        s.cmd(f'fill -746 69 -1000 -731 74 -983 air replace minecraft:{grass}')
        s.cmd(f'fill -700 71 -995 -680 72 -982 air replace minecraft:{grass}')


def village():
    still('zombie', -738.5, 71, -994.5, 15, f',equipment:{{head:{head("jack_o_lantern", 1)}}}')
    still('skeleton', -743.5, 71, -998.5, -20, f',equipment:{{head:{head("carved_pumpkin", 6)},mainhand:{{id:"minecraft:bow",count:1}}}}')
    still('hallowed:spirit', -746.5, 74.6, -1001.5, 20)
    still('hallowed:spirit', -731.5, 77.8, -1006.5, -10)
    s.cmd('summon hallowed:will_o_wisp -745.5 73.2 -992.5')
    view(-740, 72, -985, -737, 73, -1003, wait=6)
    shot('village-witching-hour')


def witching():
    view(-740, 72, -985, -737, 73.5, -1003, wait=3)
    s.cmd('time set 17940', 'gamerule advance_time true')
    time.sleep(5.1)
    shot('witching-hour', hud=True)
    s.cmd('gamerule advance_time false')


def moon():
    s.cmd('time set 20750')
    s.tp(-711.5, 74.0, -991.5, 98, -22)
    time.sleep(6)
    shot('harvest-moon')


def horseman():
    clear_actors()
    still('hallowed:hollow_horseman', -739.5, 71, -994.5, 105)
    still('zombie', -744.5, 71, -993.5, 30, f',equipment:{{head:{head("jack_o_lantern", 1)}}}')
    time.sleep(1)
    s.cmd('kill @e[type=item]')
    view(-743.2, 70.9, -991.2, -739.8, 73.9, -994.5, wait=5)
    shot('horseman')
    clear_actors()


def summoning():
    s.cmd('tick freeze', 'setblock -738 71 -996 hallowed:cursed_pumpkin[facing=south,stage=4]',
          'setblock -741 71 -997 hallowed:soul_jack_o_lantern[facing=south,face=6]',
          'setblock -735 71 -997 hallowed:soul_jack_o_lantern[facing=south,face=6]')
    view(-737.6, 71.3, -992.4, -738.0, 71.4, -996.0, wait=4)
    shot('cursed-pumpkin')
    s.cmd('tick unfreeze', 'setblock -738 71 -996 air', 'setblock -741 71 -997 air', 'setblock -735 71 -997 air')
    time.sleep(1)
    view(-738.0, 71.4, -991.0, -738.0, 72.8, -997.0, wait=1)
    s.cmd('setblock -738 71 -997 hallowed:cursed_pumpkin[facing=south,stage=4]')
    time.sleep(0.75)
    shot('horseman-arrives', hud=True)
    clear_actors()
    time.sleep(7)
    view(-738.0, 71.6, -989.0, -738.0, 72.9, -997.0, wait=2)
    s.hud(False)
    record('horseman-rises', 5.0, (1024, 576, 448, 280), 720, 14,
           lambda: s.cmd('setblock -738 71 -997 hallowed:cursed_pumpkin[facing=south,stage=4]'))
    s.hud(True)
    clear_actors()


def phase_two():
    s.cmd('setblock -738 71 -997 air')
    still('hallowed:hollow_horseman', -738.5, 71, -996.5, 0, ',mounted:0b')
    still('hallowed:spirit', -735.5, 72.4, -998.5, 20)
    still('hallowed:spirit', -741.5, 72.8, -999.0, -20)
    time.sleep(1)
    s.cmd('kill @e[type=item]')
    view(-738.5, 71.6, -991.5, -738.5, 72.6, -996.5, wait=4)
    shot('horseman-phase-two')
    clear_actors()


def doorstep():
    """Treat and trick at the same villager spot; the config is switched to force each outcome, then restored."""
    s.cmd(f'gamemode creative {PLAYER}', f'clear {PLAYER}')
    for i, item in enumerate(['hallowed:treat_pail', 'hallowed:candy_corn 12', 'hallowed:caramel_apple 3', 'hallowed:witch_lollipop 2',
                              'hallowed:reaper_scythe', 'hallowed:jack_o_lantern', 'minecraft:shears', 'hallowed:ghost_gummy 5',
                              'hallowed:soul_toffee 2']):
        s.cmd(f'item replace entity {PLAYER} hotbar.{i} with {item}')
    s.cmd(f'item replace entity {PLAYER} armor.head with hallowed:witch_hat')
    s.key('1')
    for name, profession, delay in (('trick-or-treat', 'cleric', 0.45), ('spook', 'fletcher', 0.12)):
        s.cmd('tp @e[type=villager,x=-739.5,y=71,z=-994.5,distance=..2] ~ -100 ~')
        still('villager', -739.5, 71, -994.5, 0, f',VillagerData:{{type:"minecraft:plains",profession:"minecraft:{profession}",level:2}}')
        print(f'Set the trick-or-treat config to force a {"treat" if name == "trick-or-treat" else "spook"}, '
              'run /hallowed reload, then press Enter.')
        input()
        view(-739.5, 71, -990.4, -739.5, 72.3, -994.5, wait=3)
        s.click(3)
        time.sleep(delay)
        shot(name, hud=True)
    s.cmd(f'gamemode spectator {PLAYER}')


def carving():
    s.cmd('fill -712 71 -1002 -701 75 -998 air')
    for i in range(8):
        x = -710 + i
        if i == 0:
            s.cmd(f'setblock {x} 71 -1000 minecraft:carved_pumpkin[facing=south]', f'setblock {x} 72 -1000 minecraft:jack_o_lantern[facing=south]')
        else:
            s.cmd(f'setblock {x} 71 -1000 hallowed:carved_pumpkin[facing=south,face={i}]',
                  f'setblock {x} 72 -1000 hallowed:jack_o_lantern[facing=south,face={i}]')
        s.cmd(f'setblock {x} 73 -1000 hallowed:soul_jack_o_lantern[facing=south,face={i}]')
    view(-706.5, 71.7, -993.5, -706.5, 72.4, -1000, wait=5)
    shot('carved-faces')
    s.cmd(f'gamemode creative {PLAYER}', f'item replace entity {PLAYER} hotbar.0 with minecraft:shears',
          f'item replace entity {PLAYER} hotbar.1 with minecraft:torch', 'setblock -706 71 -996 minecraft:carved_pumpkin[facing=south]')
    s.key('1')
    view(-705.5, 71, -993.4, -705.5, 71.45, -995.5, wait=2.5)

    def carve():
        for _ in range(7):
            s.click(3)
            time.sleep(0.5)
        s.key('2')
        time.sleep(0.3)
        s.click(3)
    record('carving', 12, (960, 720, 480, 220), 560, 12, carve)
    s.cmd('setblock -706 71 -996 air', f'gamemode spectator {PLAYER}')


def ward():
    for x, z, face, kind in ((-695, -990, 1, 'jack_o_lantern'), (-692, -991, 4, 'soul_jack_o_lantern'), (-689, -990, 6, 'jack_o_lantern'),
                             (-686, -991, 2, 'jack_o_lantern'), (-683, -990, 7, 'soul_jack_o_lantern')):
        s.cmd(f'setblock {x} 71 {z} hallowed:{kind}[facing=south,face={face}]')
    for x, y, z, yaw in ((-693.5, 73.6, -999.5, 10), (-688.5, 74.6, -1003.5, -5), (-684.5, 73.2, -997.5, -15), (-691.0, 76.2, -1009.5, 5),
                         (-697.5, 74.0, -1004.5, 20)):
        still('hallowed:spirit', x, y, z, yaw)
    s.cmd('time set 19000')
    view(-689.5, 71.9, -983.5, -689.5, 73.6, -1000, wait=6)
    shot('lantern-ward')
    for x, y, z, yaw in ((-676.5, 73.4, -1006.5, 10), (-673.0, 74.6, -1010.5, -15), (-679.5, 75.2, -1012.0, 20)):
        still('hallowed:spirit', x, y, z, yaw)
    view(-675.6, 73.4, -1002.6, -676.0, 74.6, -1007.0, wait=4)
    shot('spirits')


def wisps():
    at_surface(-662, -1016, 'hallowed:hallowed_cache[facing=south]', '#minecraft:dirt')
    view(-660.5, 69.9, -997.5, -662.0, 67.8, -1010.0, wait=3)
    s.cmd('summon hallowed:will_o_wisp -661.6 68.2 -1004.2', 'summon hallowed:will_o_wisp -664.5 69.0 -1013.5 {cursed:1b}')
    time.sleep(1.2)
    shot('will-o-wisp')
    s.cmd('kill @e[type=hallowed:will_o_wisp]', f'gamemode survival {PLAYER}', f'effect give {PLAYER} resistance 600 4 true', f'clear {PLAYER}')
    s.tp(-661.5, 66, -1012.6, facing=(-661.5, 66.45, -1015.5))
    time.sleep(2)
    shot('hallowed-cache')
    s.hud(False)
    s.click(3)
    time.sleep(0.15)
    shot('cache-burst')
    s.cmd(f'gamemode spectator {PLAYER}')


def scythe():
    s.cmd('fill -713 70 -995 -699 70 -981 minecraft:farmland[moisture=7]', 'fill -713 71 -995 -699 73 -981 air',
          'fill -713 71 -995 -699 71 -981 minecraft:wheat[age=7]', 'fill -707 70 -995 -707 70 -981 minecraft:water',
          'fill -707 71 -995 -707 71 -981 air', f'gamemode survival {PLAYER}', f'clear {PLAYER}',
          f'item replace entity {PLAYER} weapon.mainhand with hallowed:reaper_scythe', f'effect give {PLAYER} night_vision 60 0 true')
    s.tp(-704.5, 71, -985.5, 165, 30)
    time.sleep(4)
    s.click(3)
    time.sleep(0.25)
    shot('reaper-scythe', hud=True)
    s.cmd(f'effect clear {PLAYER}')


def inventory():
    s.cmd(f'clear {PLAYER}')
    items = ['hallowed:candy_corn 16', 'hallowed:caramel_apple 8', 'hallowed:ghost_gummy 12', 'hallowed:pumpkin_truffle 6',
             'hallowed:witch_lollipop 4', 'hallowed:bat_wing_licorice 9', 'hallowed:eyeball_jawbreaker 3', 'hallowed:soul_toffee 2',
             'hallowed:ectoplasm 21', 'hallowed:reaper_scythe', 'hallowed:horseman_head', 'hallowed:treat_pail', 'hallowed:music_disc_witching_hour',
             'hallowed:cursed_pumpkin', 'hallowed:carved_pumpkin', 'hallowed:jack_o_lantern', 'hallowed:soul_jack_o_lantern', 'hallowed:hallowed_cache',
             'hallowed:spirit_spawn_egg', 'hallowed:will_o_wisp_spawn_egg', 'hallowed:hollow_horseman_spawn_egg', 'minecraft:shears',
             'minecraft:torch 32', 'minecraft:soul_torch 32', 'minecraft:pumpkin 12', 'minecraft:carved_pumpkin 4', 'minecraft:sugar 24']
    for i, item in enumerate(items):
        s.cmd(f'item replace entity {PLAYER} inventory.{i} with {item}')
    for i, item in enumerate(['hallowed:treat_pail', 'hallowed:candy_corn 12', 'hallowed:reaper_scythe', 'hallowed:jack_o_lantern 8', 'minecraft:shears',
                              'hallowed:ghost_gummy 4', 'hallowed:caramel_apple 5', 'hallowed:soul_toffee 2', 'hallowed:witch_lollipop 3']):
        s.cmd(f'item replace entity {PLAYER} hotbar.{i} with {item}')
    s.cmd(f'item replace entity {PLAYER} armor.head with hallowed:witch_hat')
    time.sleep(9)  # let the recipe and advancement toasts pass
    s.key('e')
    time.sleep(1.5)
    s.move(742, 622)
    time.sleep(0.8)
    shot('inventory', hud=True)
    s.key('Escape')
    for adv in ('root', 'sweet_tooth', 'witching_hour', 'ghostbuster'):
        s.cmd(f'advancement grant {PLAYER} only hallowed:{adv}')
    time.sleep(9)
    s.key('l')
    time.sleep(1.5)
    s.move(1250, 470)
    shot('advancements', hud=True)
    s.key('Escape')


SHOTS = {'village': village, 'witching': witching, 'moon': moon, 'horseman': horseman, 'summoning': summoning, 'phase_two': phase_two,
         'doorstep': doorstep, 'carving': carving, 'ward': ward, 'wisps': wisps, 'scythe': scythe, 'inventory': inventory}

if __name__ == '__main__':
    wanted = sys.argv[1:] or list(SHOTS)
    s = Session()
    setup()
    for name in wanted:
        SHOTS[name]()
