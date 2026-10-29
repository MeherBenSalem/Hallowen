"""Data files: loot tables, recipes, tags, advancements and the jukebox song (data/hallowed and data/minecraft)."""
from __future__ import annotations

NS = 'hallowed'
CANDIES = ['candy_corn', 'caramel_apple', 'ghost_gummy', 'pumpkin_truffle', 'witch_lollipop', 'bat_wing_licorice',
           'eyeball_jawbreaker', 'soul_toffee']


def item_entry(name, count=None, weight=None, conditions=None):
    e = {'type': 'minecraft:item', 'name': name}
    mods = []
    if count is not None:
        lo, hi = count
        mods.append({'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}}
                    if lo != hi else {'type': 'minecraft:set_count', 'count': lo})
    if mods:
        e['modifier'] = mods
    if weight is not None:
        e['weight'] = weight
    if conditions:
        e['condition'] = conditions
    return e


# ---------------------------------------------------------------------------------------------------------------------
# Loot tables

def face_block_loot(block):
    return {
        'type': 'minecraft:block',
        'pools': [{
            'rolls': 1,
            'condition': {'type': 'minecraft:survives_explosion'},
            'entries': [{
                'type': 'minecraft:item', 'name': f'{NS}:{block}',
                'modifier': [{'type': 'minecraft:copy_state', 'block': f'{NS}:{block}', 'properties': ['face']}],
            }],
        }],
        'random_sequence': f'{NS}:blocks/{block}',
    }


def candy_pool(rolls_min, rolls_max):
    weights = {'candy_corn': 12, 'caramel_apple': 6, 'ghost_gummy': 8, 'pumpkin_truffle': 8, 'witch_lollipop': 5,
               'bat_wing_licorice': 5, 'eyeball_jawbreaker': 4, 'soul_toffee': 2}
    return {'rolls': {'type': 'minecraft:uniform', 'min': rolls_min, 'max': rolls_max},
            'entries': [item_entry(f'{NS}:{c}', (1, 2), w) for c, w in weights.items()]}


def cache_loot():
    cursed = {'type': 'minecraft:match_block', 'blocks': f'{NS}:hallowed_cache', 'state': {'cursed': 'true'}}
    return {
        'type': 'minecraft:block',
        'pools': [
            candy_pool(3, 5),
            {'rolls': 1, 'entries': [
                item_entry(f'{NS}:ectoplasm', (1, 3), 10),
                item_entry('minecraft:pumpkin_seeds', (2, 5), 8),
                item_entry('minecraft:gold_nugget', (3, 9), 8),
                item_entry('minecraft:emerald', (1, 3), 5),
                item_entry(f'{NS}:witch_hat', None, 2),
                item_entry(f'{NS}:treat_pail', None, 2),
                item_entry('minecraft:diamond', None, 1),
            ]},
            {'rolls': 1, 'condition': cursed, 'entries': [
                item_entry(f'{NS}:soul_toffee', (1, 2), 6),
                item_entry(f'{NS}:ectoplasm', (3, 6), 6),
                item_entry('minecraft:diamond', (1, 2), 3),
                item_entry(f'{NS}:music_disc_witching_hour', None, 2),
                item_entry(f'{NS}:cursed_pumpkin', None, 1),
            ]},
            candy_pool(2, 3) | {'condition': cursed},
        ],
        'random_sequence': f'{NS}:blocks/hallowed_cache',
    }


def simple_block_loot(block):
    return {'type': 'minecraft:block',
            'pools': [{'rolls': 1, 'condition': {'type': 'minecraft:survives_explosion'},
                       'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{block}'}]}],
            'random_sequence': f'{NS}:blocks/{block}'}


def spirit_loot():
    return {
        'type': 'minecraft:entity',
        'pools': [{'rolls': 1, 'entries': [{
            'type': 'minecraft:item', 'name': f'{NS}:ectoplasm',
            'modifier': [
                {'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 2}},
                {'type': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting',
                 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': 1}},
            ]}]}],
        'random_sequence': f'{NS}:entities/spirit',
    }


def horseman_loot():
    return {
        'type': 'minecraft:entity',
        'pools': [
            {'rolls': 1, 'entries': [item_entry(f'{NS}:reaper_scythe', None, 1), item_entry(f'{NS}:horseman_head', None, 1)]},
            {'rolls': 1, 'condition': {'type': 'minecraft:random_chance', 'chance': 0.35},
             'entries': [item_entry(f'{NS}:music_disc_witching_hour')]},
            {'rolls': 1, 'entries': [item_entry(f'{NS}:ectoplasm', (4, 8))]},
            {'rolls': 1, 'entries': [item_entry(f'{NS}:cursed_pumpkin')],
             'condition': {'type': 'minecraft:random_chance', 'chance': 0.15}},
        ],
        'random_sequence': f'{NS}:entities/hollow_horseman',
    }


def loot_tables():
    out = {f'blocks/{b}': face_block_loot(b) for b in ('carved_pumpkin', 'jack_o_lantern', 'soul_jack_o_lantern')}
    out['blocks/hallowed_cache'] = cache_loot()
    out['blocks/cursed_pumpkin'] = simple_block_loot('cursed_pumpkin')
    out['entities/spirit'] = spirit_loot()
    out['entities/hollow_horseman'] = horseman_loot()
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Recipes

def shaped(pattern, key, result, count=1, category='misc'):
    r = {'type': 'minecraft:crafting_shaped', 'category': category, 'key': key, 'pattern': pattern, 'result': {'id': result}}
    if count != 1:
        r['result']['count'] = count
    return r


def shapeless(ingredients, result, count=1, category='misc'):
    r = {'type': 'minecraft:crafting_shapeless', 'category': category, 'ingredients': ingredients, 'result': {'id': result}}
    if count != 1:
        r['result']['count'] = count
    return r


def recipes():
    n = lambda s: f'{NS}:{s}'  # noqa: E731
    return {
        'treat_pail': shaped(['S S', 'P P', ' C '], {'S': 'minecraft:string', 'P': 'minecraft:pumpkin', 'C': 'minecraft:carved_pumpkin'},
                             n('treat_pail'), category='equipment'),
        'witch_hat': shaped([' L ', ' W ', 'WSW'], {'L': 'minecraft:leather', 'W': 'minecraft:black_wool', 'S': 'minecraft:gold_nugget'},
                            n('witch_hat'), category='equipment'),
        'soul_jack_o_lantern': shaped(['A', 'B'], {'A': 'minecraft:carved_pumpkin', 'B': 'minecraft:soul_torch'},
                                      n('soul_jack_o_lantern'), category='building'),
        'cursed_pumpkin': shaped(['EBE', 'EJE', 'EBE'], {'E': n('ectoplasm'), 'B': 'minecraft:bone', 'J': n('soul_jack_o_lantern')},
                                 n('cursed_pumpkin')),
        'candy_corn': shapeless(['minecraft:sugar', 'minecraft:wheat', 'minecraft:orange_dye'], n('candy_corn'), 4),
        'caramel_apple': shaped(['S', 'A', 'T'], {'S': 'minecraft:sugar', 'A': 'minecraft:apple', 'T': 'minecraft:stick'},
                                n('caramel_apple')),
        'ghost_gummy': shapeless([n('ectoplasm'), 'minecraft:sugar', 'minecraft:slime_ball'], n('ghost_gummy'), 3),
        'pumpkin_truffle': shapeless(['minecraft:pumpkin_pie', 'minecraft:cocoa_beans', 'minecraft:sugar'], n('pumpkin_truffle'), 2),
        'eyeball_jawbreaker': shapeless(['minecraft:spider_eye', 'minecraft:sugar', 'minecraft:sugar'], n('eyeball_jawbreaker'), 2),
        'soul_toffee': shapeless([n('ectoplasm'), 'minecraft:sugar', 'minecraft:honey_bottle', 'minecraft:soul_sand'], n('soul_toffee'), 2),
    }


# ---------------------------------------------------------------------------------------------------------------------
# Tags

def tags():
    head_costumes = ['minecraft:carved_pumpkin', 'minecraft:jack_o_lantern', '#minecraft:skulls', f'{NS}:carved_pumpkin',
                     f'{NS}:jack_o_lantern', f'{NS}:soul_jack_o_lantern', f'{NS}:witch_hat', f'{NS}:horseman_head']
    return {
        'item/candy': {'values': [f'{NS}:{c}' for c in CANDIES]},
        'item/costumes': {'values': head_costumes},
        'entity_type/costume_wearers': {'values': ['minecraft:zombie', 'minecraft:husk', 'minecraft:drowned', 'minecraft:skeleton',
                                                   'minecraft:stray', 'minecraft:bogged', 'minecraft:zombie_villager',
                                                   'minecraft:wither_skeleton']},
        'entity_type/no_candy': {'values': ['minecraft:slime', 'minecraft:magma_cube', 'minecraft:silverfish', 'minecraft:endermite']},
    }


def minecraft_tags():
    return {
        'item/gaze_disguise_equipment': {'replace': False, 'values': [f'{NS}:carved_pumpkin']},
    }


# ---------------------------------------------------------------------------------------------------------------------
# Advancements

def adv(name, icon, frame='task', parent='root', criteria=None, requirements=None, hidden=False, toast=True, chat=True,
        background=None):
    a = {
        'display': {
            'icon': {'id': icon},
            'title': {'translate': f'advancements.{NS}.{name}.title'},
            'description': {'translate': f'advancements.{NS}.{name}.description'},
            'frame': frame, 'show_toast': toast, 'announce_to_chat': chat, 'hidden': hidden,
        },
        'criteria': criteria or {'granted': {'trigger': 'minecraft:impossible'}},
    }
    if background:
        a['display']['background'] = background
    if requirements:
        a['requirements'] = requirements
    else:
        a['requirements'] = [[k] for k in a['criteria']]
    if parent:
        a['parent'] = f'{NS}:{parent}'
    return a


def advancements():
    eat = lambda c: {'trigger': 'minecraft:consume_item', 'conditions': {'item': {'items': f'{NS}:{c}'}}}  # noqa: E731
    any_candy = {f'eat_{c}': eat(c) for c in CANDIES}
    return {
        'root': adv('root', f'{NS}:candy_corn', parent=None, criteria=any_candy, requirements=[list(any_candy)],
                    toast=False, chat=False, background=f'{NS}:gui/advancements/backgrounds/hallowed'),
        'witching_hour': adv('witching_hour', 'minecraft:bell'),
        'carve_a_face': adv('carve_a_face', 'minecraft:shears'),
        'trick_or_treat': adv('trick_or_treat', f'{NS}:treat_pail', parent='witching_hour'),
        'tricked': adv('tricked', f'{NS}:ghost_gummy', parent='trick_or_treat'),
        'dressed_for_the_occasion': adv('dressed_for_the_occasion', f'{NS}:witch_hat', parent='trick_or_treat'),
        'neighbourhood_round': adv('neighbourhood_round', f'{NS}:caramel_apple', 'goal', parent='dressed_for_the_occasion'),
        'sweet_tooth': adv('sweet_tooth', f'{NS}:witch_lollipop', 'challenge', criteria={f'eat_{c}': eat(c) for c in CANDIES}),
        'sugar_crash': adv('sugar_crash', f'{NS}:candy_corn', parent='root', hidden=True),
        'follow_the_light': adv('follow_the_light', f'{NS}:hallowed_cache', parent='witching_hour'),
        'ghostbuster': adv('ghostbuster', f'{NS}:ectoplasm', parent='witching_hour', criteria={
            'killed': {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': {
                'type': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'minecraft:entity_type': f'{NS}:spirit'}}}}}),
        'cursed_pumpkin': adv('cursed_pumpkin', f'{NS}:cursed_pumpkin', parent='ghostbuster', criteria={
            'has': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': f'{NS}:cursed_pumpkin'}]}}}),
        'headless_no_more': adv('headless_no_more', f'{NS}:horseman_head', 'challenge', parent='cursed_pumpkin'),
        'grim_harvest': adv('grim_harvest', f'{NS}:reaper_scythe', 'goal', parent='headless_no_more'),
    }


def jukebox_songs():
    return {'witching_hour': {
        'comparator_output': 13,
        'description': {'translate': f'jukebox_song.{NS}.witching_hour'},
        'length_in_seconds': 129.0,
        'sound_event': f'{NS}:music_disc.witching_hour',
    }}
