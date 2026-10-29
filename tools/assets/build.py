#!/usr/bin/env python3
"""Writes every generated texture, sound, model, language file and data file into a version workspace.

  python3 tools/assets/build.py            # the 26.3 workspace
  python3 tools/assets/build.py 26.3 --no-sounds
"""
from __future__ import annotations

import argparse
import json
import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))

import blocks  # noqa: E402
import data  # noqa: E402
import entities  # noqa: E402
import items  # noqa: E402
import misc  # noqa: E402
import models  # noqa: E402
from lang import EN_US  # noqa: E402
from pixel import canvas, put, strip  # noqa: E402

ROOT = HERE.parent.parent
NS = 'hallowed'


def write_json(path: Path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')


def save(img, path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, optimize=True)


def witch_hat_worn():
    """Texture for the worn Witch Hat: rows are the brim, the band and the cone tiers, darkening toward the tip."""
    img = canvas()
    rows = ['#1c1226', '#2e1f3d', '#7a3fb0', '#7a3fb0', '#5a2a88', '#2e1f3d', '#2a1c3a', '#241832', '#2e1f3d', '#2a1c3a',
            '#221630', '#2a1c3a', '#241832', '#1e1428', '#1c1226', '#160e1e']
    for y, c in enumerate(rows):
        for x in range(16):
            put(img, x, y, c)
            if y >= 5 and x % 5 == 1:
                put(img, x, y, '#3a2850')
    for x in (7, 8):
        for y in (2, 3):
            put(img, x, y, '#f0c24a')
    put(img, 7, 2, '#ffe08a')
    return img


def build(workspace: str, with_sounds: bool):
    res = ROOT / workspace / 'common' / 'src' / 'main' / 'resources'
    assets = res / 'assets' / NS
    datadir = res / 'data' / NS
    for sub in ('textures', 'models', 'blockstates', 'items', 'particles', 'lang'):
        shutil.rmtree(assets / sub, ignore_errors=True)
    for sub in ('loot_table', 'recipe', 'tags', 'advancement', 'jukebox_song'):
        shutil.rmtree(datadir / sub, ignore_errors=True)

    tex = assets / 'textures'
    for name, fn in items.ITEMS.items():
        save(fn(), tex / 'item' / f'{name}.png')
    save(witch_hat_worn(), tex / 'item' / 'witch_hat_worn.png')
    for name, fn in blocks.BLOCKS.items():
        save(fn(), tex / 'block' / f'{name}.png')
    for name, img in blocks.face_overlays().items():
        save(img, tex / 'block' / f'{name}.png')
    save(blocks.head_flame(), tex / 'block' / 'horseman_head_flame.png')
    write_json(tex / 'block' / 'horseman_head_flame.png.mcmeta', {'animation': {'frametime': 2, 'interpolate': True}})
    for name, fn in entities.ENTITIES.items():
        save(fn(), tex / 'entity' / f'{name}.png')
    for name, frames in misc.PARTICLES.items():
        for i, frame in enumerate(frames):
            save(frame, tex / 'particle' / f'{name}_{i}.png')
        write_json(assets / 'particles' / f'{name}.json', {'textures': [f'{NS}:{name}_{i}' for i in range(len(frames))]})
    for name, img in misc.effect_icons().items():
        save(img, tex / 'mob_effect' / f'{name}.png')
    for name, fn in misc.GUI.items():
        save(fn(), tex / f'{name}.png')

    for name, model in models.block_models().items():
        write_json(assets / 'models' / 'block' / f'{name}.json', model)
    for name, model in models.item_models().items():
        write_json(assets / 'models' / 'item' / f'{name}.json', model)
    for name, state in models.blockstates().items():
        write_json(assets / 'blockstates' / f'{name}.json', state)
    for name, definition in models.item_definitions().items():
        write_json(assets / 'items' / f'{name}.json', definition)

    write_json(assets / 'lang' / 'en_us.json', dict(sorted(EN_US.items())))

    for name, table in data.loot_tables().items():
        write_json(datadir / 'loot_table' / f'{name}.json', table)
    for name, recipe in data.recipes().items():
        write_json(datadir / 'recipe' / f'{name}.json', recipe)
    for name, tag in data.tags().items():
        write_json(datadir / 'tags' / f'{name}.json', tag)
    for name, tag in data.minecraft_tags().items():
        write_json(res / 'data' / 'minecraft' / 'tags' / f'{name}.json', tag)
    for name, adv in data.advancements().items():
        write_json(datadir / 'advancement' / f'{name}.json', adv)
    for name, song in data.jukebox_songs().items():
        write_json(datadir / 'jukebox_song' / f'{name}.json', song)

    icon = misc.logo(128)
    save(icon, res / f'{NS}.png')
    save(misc.logo(256), res / 'logo.png')

    if with_sounds:
        import music
        import numpy as np
        import sounds
        import synth
        shutil.rmtree(assets / 'sounds', ignore_errors=True)
        for name, fn in sounds.SOUNDS.items():
            synth.write_ogg(trim(fn(), np), assets / 'sounds' / f'{name}.ogg')
        synth.write_ogg(music.render(), assets / 'sounds' / 'music' / 'witching_hour.ogg')
        write_json(assets / 'sounds.json', sounds.sounds_json())
    print(f'wrote assets into {res}')


def trim(x, np):
    """Drops the silent end of a reverb tail."""
    loud = np.nonzero(np.abs(x) > 0.002)[0]
    if len(loud) == 0:
        return x
    end = min(len(x), loud[-1] + int(44100 * 0.05))
    out = x[:end].copy()
    fade = min(len(out), int(44100 * 0.03))
    out[-fade:] *= np.linspace(1, 0, fade)
    return out


if __name__ == '__main__':
    ap = argparse.ArgumentParser()
    ap.add_argument('workspace', nargs='?', default='26.3')
    ap.add_argument('--no-sounds', action='store_true')
    args = ap.parse_args()
    build(args.workspace, not args.no_sounds)
