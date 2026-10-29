"""Block models, blockstates, item models and item model definitions (assets/hallowed/{models,blockstates,items})."""
from __future__ import annotations

import faces

NS = 'hallowed'
FACINGS = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
KINDS = {'carved_pumpkin': 'carved', 'jack_o_lantern': 'lit', 'soul_jack_o_lantern': 'soul'}
CANDIES = ['candy_corn', 'caramel_apple', 'ghost_gummy', 'pumpkin_truffle', 'witch_lollipop', 'bat_wing_licorice',
           'eyeball_jawbreaker', 'soul_toffee']


def face_model(kind, face):
    """A pumpkin with a carved overlay on its north face; vanilla's own texture stands in for the classic face."""
    if face == 'classic' and kind != 'soul':
        front = 'minecraft:block/carved_pumpkin' if kind == 'carved' else 'minecraft:block/jack_o_lantern'
        return {'parent': 'minecraft:block/orientable',
                'textures': {'front': front, 'side': 'minecraft:block/pumpkin_side', 'top': 'minecraft:block/pumpkin_top'}}
    overlay = {
        'from': [0, 0, 0], 'to': [16, 16, 16],
        'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#overlay', 'cullface': 'north'}},
    }
    if kind != 'carved':
        overlay['light_emission'] = 15
    return {
        'parent': 'minecraft:block/block',
        'textures': {
            'particle': 'minecraft:block/pumpkin_side',
            'side': 'minecraft:block/pumpkin_side',
            'top': 'minecraft:block/pumpkin_top',
            'overlay': f'{NS}:block/{kind}_face_{face}',
        },
        'elements': [
            {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
                'down': {'uv': [0, 0, 16, 16], 'texture': '#top', 'cullface': 'down'},
                'up': {'uv': [0, 0, 16, 16], 'texture': '#top', 'cullface': 'up'},
                'north': {'uv': [0, 0, 16, 16], 'texture': '#side', 'cullface': 'north'},
                'south': {'uv': [0, 0, 16, 16], 'texture': '#side', 'cullface': 'south'},
                'west': {'uv': [0, 0, 16, 16], 'texture': '#side', 'cullface': 'west'},
                'east': {'uv': [0, 0, 16, 16], 'texture': '#side', 'cullface': 'east'},
            }},
            overlay,
        ],
    }


def cache_model(cursed):
    front = f'{NS}:block/hallowed_cache_front' + ('_cursed' if cursed else '')
    return {
        'parent': 'minecraft:block/block',
        'textures': {
            'particle': f'{NS}:block/hallowed_cache_side', 'front': front, 'side': f'{NS}:block/hallowed_cache_side',
            'top': f'{NS}:block/hallowed_cache_top', 'bottom': f'{NS}:block/hallowed_cache_bottom',
        },
        'elements': [
            {'from': [1, 0, 2], 'to': [15, 12, 14], 'faces': {
                'down': {'uv': [1, 2, 15, 14], 'texture': '#bottom', 'cullface': 'down'},
                'up': {'uv': [1, 2, 15, 14], 'texture': '#top'},
                'north': {'uv': [1, 2, 15, 14], 'texture': '#front'},
                'south': {'uv': [1, 2, 15, 14], 'texture': '#side'},
                'west': {'uv': [2, 2, 14, 14], 'texture': '#side'},
                'east': {'uv': [2, 2, 14, 14], 'texture': '#side'},
            }},
            {'from': [7, 6, 1], 'to': [9, 10, 2], 'faces': {
                'north': {'uv': [7, 0, 9, 4], 'texture': '#side'},
                'west': {'uv': [0, 0, 1, 4], 'texture': '#side'},
                'east': {'uv': [0, 0, 1, 4], 'texture': '#side'},
                'up': {'uv': [7, 0, 9, 1], 'texture': '#side'},
                'down': {'uv': [7, 0, 9, 1], 'texture': '#side'},
            }},
        ],
    }


def cursed_model(awake):
    return {'parent': 'minecraft:block/orientable',
            'textures': {'front': f'{NS}:block/cursed_pumpkin_front' + ('_awake' if awake else ''),
                         'side': f'{NS}:block/cursed_pumpkin_side', 'top': f'{NS}:block/cursed_pumpkin_top'}}


def block_models():
    out = {}
    for block, kind in KINDS.items():
        for face in faces.ORDER:
            out[f'{kind}_{face}'] = face_model(kind, face)
    out['hallowed_cache'] = cache_model(False)
    out['hallowed_cache_cursed'] = cache_model(True)
    out['cursed_pumpkin'] = cursed_model(False)
    out['cursed_pumpkin_awake'] = cursed_model(True)
    return out


def blockstates():
    out = {}
    for block, kind in KINDS.items():
        variants = {}
        for i, face in enumerate(faces.ORDER):
            for facing, y in FACINGS.items():
                v = {'model': f'{NS}:block/{kind}_{face}'}
                if y:
                    v['y'] = y
                variants[f'face={i},facing={facing}'] = v
        out[block] = {'variants': variants}
    variants = {}
    for cursed in (False, True):
        for facing, y in FACINGS.items():
            v = {'model': f'{NS}:block/hallowed_cache' + ('_cursed' if cursed else '')}
            if y:
                v['y'] = y
            variants[f'cursed={str(cursed).lower()},facing={facing}'] = v
    out['hallowed_cache'] = {'variants': variants}
    variants = {}
    for stage in range(5):
        for facing, y in FACINGS.items():
            v = {'model': f'{NS}:block/cursed_pumpkin' + ('_awake' if stage else '')}
            if y:
                v['y'] = y
            variants[f'facing={facing},stage={stage}'] = v
    out['cursed_pumpkin'] = {'variants': variants}
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Item models

def generated(texture):
    return {'parent': 'minecraft:item/generated', 'textures': {'layer0': texture}}


def scythe_model():
    """A long-handled weapon: held big and angled like a tool, flat in the GUI."""
    return {
        'parent': 'minecraft:item/handheld',
        'textures': {'layer0': f'{NS}:item/reaper_scythe'},
        'display': {
            'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 6.5, 1.5], 'scale': [1.45, 1.45, 0.85]},
            'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 6.5, 1.5], 'scale': [1.45, 1.45, 0.85]},
            'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.13, 3.6, 1.13], 'scale': [0.8, 0.8, 0.8]},
            'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.13, 3.6, 1.13], 'scale': [0.8, 0.8, 0.8]},
        },
    }


def horseman_head_model():
    """A charred jack o'lantern full of fire, with two crossed flame planes licking up from its crown."""
    flame = {'uv': [0, 0, 16, 16], 'texture': '#flame'}
    return {
        'parent': 'minecraft:block/block',
        'textures': {
            'particle': f'{NS}:block/horseman_head_side', 'side': f'{NS}:block/horseman_head_side',
            'top': f'{NS}:block/horseman_head_top', 'face': f'{NS}:block/horseman_head_face',
            'flame': f'{NS}:block/horseman_head_flame',
        },
        'elements': [
            {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
                'down': {'uv': [0, 0, 16, 16], 'texture': '#top'},
                'up': {'uv': [0, 0, 16, 16], 'texture': '#top'},
                'north': {'uv': [0, 0, 16, 16], 'texture': '#face'},
                'south': {'uv': [0, 0, 16, 16], 'texture': '#side'},
                'west': {'uv': [0, 0, 16, 16], 'texture': '#side'},
                'east': {'uv': [0, 0, 16, 16], 'texture': '#side'},
            }},
            {'from': [0, 0, 0], 'to': [16, 16, 16], 'light_emission': 15, 'faces': {
                'north': {'uv': [0, 0, 16, 16], 'texture': '#face'},
            }},
            {'from': [0.8, 12, 8], 'to': [15.2, 26, 8], 'light_emission': 15, 'shade_direction_override': 'up',
             'rotation': {'origin': [8, 16, 8], 'axis': 'y', 'angle': 45, 'rescale': True},
             'faces': {'north': flame, 'south': flame}},
            {'from': [8, 12, 0.8], 'to': [8, 26, 15.2], 'light_emission': 15, 'shade_direction_override': 'up',
             'rotation': {'origin': [8, 16, 8], 'axis': 'y', 'angle': 45, 'rescale': True},
             'faces': {'west': flame, 'east': flame}},
        ],
        'display': {
            'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.5, 0], 'scale': [0.5, 0.5, 0.5]},
            'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
            'fixed': {'rotation': [0, 0, 0], 'translation': [0, -1, 0], 'scale': [0.45, 0.45, 0.45]},
            'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
            'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
            'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
        },
    }


def witch_hat_worn():
    """The Witch Hat as worn: a wide brim and a crooked cone in four tiers with a buckled band."""
    t = '#hat'

    def box(f, to, uv_front, uv_top=None, rot=None):
        faces_ = {k: {'uv': uv_front, 'texture': t} for k in ('north', 'south', 'east', 'west')}
        faces_['up'] = {'uv': uv_top or uv_front, 'texture': t}
        faces_['down'] = {'uv': uv_top or uv_front, 'texture': t}
        e = {'from': f, 'to': to, 'faces': faces_}
        if rot:
            e['rotation'] = rot
        return e

    return {
        'textures': {'hat': f'{NS}:item/witch_hat_worn', 'particle': f'{NS}:item/witch_hat_worn'},
        'elements': [
            box([-3, 15, -3], [19, 16, 19], [0, 0, 16, 1], [0, 0, 16, 16]),
            box([2, 16, 2], [14, 18, 14], [0, 2, 16, 4]),
            box([3, 18, 3], [13, 21, 13], [0, 5, 16, 8]),
            box([4.5, 21, 4.5], [11.5, 24, 11.5], [0, 8, 16, 11]),
            box([6, 24, 6], [10, 27, 10], [0, 11, 16, 14], rot={'origin': [8, 24, 8], 'axis': 'x', 'angle': -22.5}),
            box([7, 26.5, 7.5], [9, 29, 9.5], [0, 14, 16, 16], rot={'origin': [8, 26, 8], 'axis': 'x', 'angle': -45}),
        ],
        'display': {'head': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]}},
    }


def item_models():
    out = {}
    for name in CANDIES + ['ectoplasm', 'treat_pail', 'treat_pail_filled', 'witch_hat', 'music_disc_witching_hour',
                           'spirit_spawn_egg', 'will_o_wisp_spawn_egg', 'hollow_horseman_spawn_egg']:
        out[name] = generated(f'{NS}:item/{name}')
    out['reaper_scythe'] = scythe_model()
    out['horseman_head'] = horseman_head_model()
    out['witch_hat_worn'] = witch_hat_worn()
    return out


def item_definitions():
    def model(m):
        return {'type': 'minecraft:model', 'model': m}

    out = {}
    for name in CANDIES + ['ectoplasm', 'music_disc_witching_hour', 'spirit_spawn_egg', 'will_o_wisp_spawn_egg',
                           'hollow_horseman_spawn_egg', 'reaper_scythe', 'horseman_head']:
        out[name] = {'model': model(f'{NS}:item/{name}')}
    out['treat_pail'] = {'model': {
        'type': 'minecraft:range_dispatch', 'property': 'minecraft:bundle/fullness',
        'entries': [{'threshold': 0.0001, 'model': model(f'{NS}:item/treat_pail_filled')}],
        'fallback': model(f'{NS}:item/treat_pail'),
    }}
    out['witch_hat'] = {'model': {
        'type': 'minecraft:select', 'property': 'minecraft:display_context',
        'cases': [{'when': 'head', 'model': model(f'{NS}:item/witch_hat_worn')}],
        'fallback': model(f'{NS}:item/witch_hat'),
    }}
    for block, kind in KINDS.items():
        out[block] = {'model': {
            'type': 'minecraft:select', 'property': 'minecraft:block_state', 'block_state_property': 'face',
            'cases': [{'when': str(i), 'model': model(f'{NS}:block/{kind}_{face}')} for i, face in enumerate(faces.ORDER)],
            'fallback': model(f'{NS}:block/{kind}_grin'),
        }}
    out['hallowed_cache'] = {'model': model(f'{NS}:block/hallowed_cache')}
    out['cursed_pumpkin'] = {'model': model(f'{NS}:block/cursed_pumpkin')}
    return out
