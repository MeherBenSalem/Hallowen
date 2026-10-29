"""Hallowed's sound effects, synthesised from partials, noise and filters, encoded to mono OGG.

Creature voices that should sound like Minecraft (the Horseman's cackle, his steed) reuse vanilla sound files at
other pitches through sounds.json; everything else here is original.
"""
from __future__ import annotations

import numpy as np

from synth import (RATE, bandpass, bell, env_adsr, exp_decay, formant, glide, highpass, lowpass, noise, normalize, note,
                   organ, place, pluck, reverb, shimmer, t_axis, thump)


def _whoosh(seconds, seed, lo, hi, rising=True, q=1.6):
    n = int(RATE * seconds)
    src = noise(seconds, seed)
    out = np.zeros(n)
    steps = 28
    for i in range(steps):
        k = i / steps if rising else 1 - i / steps
        f = lo * (hi / lo) ** k
        seg = slice(i * n // steps, (i + 1) * n // steps)
        out[seg] = bandpass(src, max(60, f / q), min(RATE / 2 - 100, f * q))[seg]
    return out * np.hanning(n)


def witching_bell():
    """A great church bell: hum, prime, minor-third tierce, quint and nominal, each with its own long decay."""
    f = note('G2')
    t = t_axis(7.0)
    partials = [(0.5, 0.55, 6.0), (1.0, 1.0, 4.5), (1.19, 0.55, 3.5), (1.5, 0.35, 3.0), (2.0, 0.6, 2.4), (2.51, 0.25, 1.6),
                (2.99, 0.2, 1.2), (4.17, 0.12, 0.8), (5.43, 0.08, 0.5)]
    out = np.zeros_like(t)
    for ratio, amp, tau in partials:
        beat = 1 + 0.0015 * np.sin(2 * np.pi * 0.9 * t * ratio)
        out += amp * np.sin(2 * np.pi * f * ratio * beat * t) * np.exp(-t / tau)
    strike = highpass(noise(0.05, 3), 1500) * exp_decay(int(RATE * 0.05), 0.01) * 0.4
    place(out, strike, 0.0)
    out *= np.minimum(1, t / 0.004)
    return normalize(reverb(out, 3.0, 0.35, 1), 0.9, 0.1)


def whispers(seed):
    """Breathy, wordless whispering: syllables of shaped noise with moving vowel colour."""
    rng = np.random.default_rng(seed)
    total = 2.6
    out = np.zeros(int(RATE * total))
    t = 0.1
    while t < total - 0.4:
        length = rng.uniform(0.08, 0.24)
        n = int(RATE * length)
        src = noise(length, int(rng.integers(1e6)))
        vowel = [(rng.uniform(500, 900), 1.0), (rng.uniform(1400, 2600), 0.8), (rng.uniform(2800, 3600), 0.5)]
        syl = formant(src, vowel, q=4.0) + highpass(src, 4000) * 0.25 * (rng.random() < 0.3)
        env = np.hanning(n) ** 0.7
        place(out, syl * env, t, rng.uniform(0.4, 1.0))
        t += length + rng.uniform(0.02, 0.14)
    return normalize(reverb(out, 2.0, 0.45, seed), 0.6, 0.15)


def owl(seed):
    """A tawny owl: a soft, rising 'hoo' and a quavering 'hoo-oo-oo'."""
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 2.8))
    base = rng.uniform(380, 440)

    def hoot(seconds, f0, f1, quaver=0.0):
        t = t_axis(seconds)
        phase = glide(f0, f1, seconds)
        trem = 1 + quaver * np.sin(2 * np.pi * 9 * t)
        tone = np.sin(phase) + 0.25 * np.sin(2 * phase) + 0.06 * np.sin(3 * phase)
        breath = bandpass(noise(seconds, int(rng.integers(1e6))), f0 * 0.8, f0 * 2.5) * 0.12
        return (tone + breath) * trem * env_adsr(len(t), seconds * 0.25, 0.1, 0.0, seconds * 0.45, 0.8)

    place(out, hoot(0.45, base * 0.92, base, 0), 0.0, 0.8)
    place(out, hoot(1.1, base, base * 0.94, 0.35), 0.8, 1.0)
    return normalize(reverb(out, 2.2, 0.4, seed), 0.55, 0.2)


def chimes(seed):
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 3.0))
    scale = ['E6', 'G6', 'A6', 'B6', 'D7', 'E7']
    for i in range(rng.integers(4, 8)):
        f = note(scale[rng.integers(len(scale))])
        place(out, bell(f, 1.6, tau=0.6, brightness=0.6), rng.uniform(0, 1.6), rng.uniform(0.2, 0.5))
    return normalize(reverb(lowpass(out, 6000), 2.4, 0.6, seed), 0.45, 0.2)


def wind(seed):
    """A low moan of wind through bare branches: three resonances of noise, each swelling on its own slow cycle."""
    rng = np.random.default_rng(seed)
    seconds = 5.0
    n = int(RATE * seconds)
    t = np.arange(n) / RATE
    out = np.zeros(n)
    for i, centre in enumerate((300, 470, 720)):
        band = bandpass(noise(seconds, seed * 10 + i), centre * 0.9, centre * 1.12, order=2)
        swell = 0.5 + 0.5 * np.sin(2 * np.pi * t / seconds * rng.uniform(0.7, 1.6) + rng.uniform(0, 6.28))
        out += band * swell ** 2
    rumble = lowpass(noise(seconds, seed + 7), 160) * 0.4
    return normalize((out * 3 + rumble) * np.hanning(n) ** 0.6, 0.5)


def wisp_ambient(seed):
    t = t_axis(1.6)
    out = np.zeros_like(t)
    for i, n_ in enumerate(['C6', 'E6', 'G6', 'B6']):
        f = note(n_) * (1 + 0.002 * i)
        out += np.sin(2 * np.pi * f * t + i) * (0.5 + 0.5 * np.sin(2 * np.pi * (3 + i) * t + seed))
    out *= np.hanning(len(t))
    out += shimmer(1.6, seed, base=3500) * 0.6
    return normalize(reverb(out, 1.5, 0.5, seed), 0.45, 0.2)


def wisp_chime():
    out = np.zeros(int(RATE * 1.4))
    for i, n_ in enumerate(['E5', 'G5', 'B5', 'E6', 'G6']):
        place(out, bell(note(n_), 1.2, tau=0.45, brightness=0.5), i * 0.07, 0.55 - i * 0.05)
    place(out, shimmer(0.9, 5, base=3800), 0.2, 0.5)
    return normalize(reverb(out, 1.6, 0.45, 2), 0.7, 0.15)


def wisp_vanish():
    out = np.zeros(int(RATE * 1.3))
    place(out, _whoosh(0.9, 7, 3000, 400, rising=False), 0.0, 0.5)
    for i, n_ in enumerate(['B6', 'G6', 'E6', 'B5']):
        place(out, bell(note(n_), 0.7, tau=0.25, brightness=0.4), 0.05 + i * 0.06, 0.4)
    return normalize(reverb(out, 1.2, 0.4, 3), 0.7, 0.2)


def cache_open():
    rng = np.random.default_rng(11)
    out = np.zeros(int(RATE * 1.8))
    # creak: a slow, rough sweep of a buzzing tone
    seconds = 0.45
    phase = glide(140, 260, seconds, 0.7)
    saw = sum(np.sin(k * phase) / k for k in range(1, 18))
    roughness = 1 + 0.5 * rng.standard_normal(len(saw))
    creak = bandpass(saw * roughness, 300, 2400) * env_adsr(len(saw), 0.05, 0.1, 0.0, 0.2, 0.7)
    place(out, creak, 0.0, 0.5)
    place(out, thump(0.25, 90), 0.42, 0.9)
    for i, n_ in enumerate(['C6', 'E6', 'G6', 'C7', 'E7']):
        place(out, bell(note(n_), 1.0, tau=0.35, brightness=0.6), 0.45 + i * 0.045, 0.35)
    place(out, shimmer(1.0, 12, base=3200), 0.48, 0.8)
    return normalize(reverb(out, 1.4, 0.3, 4), 0.85, 0.15)


def candy_eat(seed):
    """Crinkle of a wrapper and a sugary crunch."""
    rng = np.random.default_rng(seed)
    seconds = 0.32
    n = int(RATE * seconds)
    out = np.zeros(n)
    for _ in range(rng.integers(5, 9)):
        click = highpass(noise(0.012, int(rng.integers(1e6))), 2500) * exp_decay(int(RATE * 0.012), 0.003)
        place(out, click, rng.uniform(0, 0.12), rng.uniform(0.2, 0.5))
    for _ in range(rng.integers(2, 4)):
        crunch = bandpass(noise(0.06, int(rng.integers(1e6))), 900, 5000) * exp_decay(int(RATE * 0.06), 0.015)
        place(out, crunch, rng.uniform(0.1, 0.24), rng.uniform(0.6, 1.0))
    return normalize(out, 0.75)


def treat():
    out = np.zeros(int(RATE * 1.4))
    for i, n_ in enumerate(['C5', 'E5', 'G5', 'C6']):
        place(out, bell(note(n_), 1.0, tau=0.4, brightness=0.7), i * 0.08, 0.6)
    place(out, bell(note('E6'), 1.0, tau=0.5, brightness=0.6), 0.36, 0.5)
    place(out, shimmer(0.8, 14, base=3600), 0.3, 0.7)
    return normalize(reverb(out, 1.2, 0.3, 5), 0.8, 0.15)


def trick():
    out = np.zeros(int(RATE * 1.3))
    for i, n_ in enumerate(['B4', 'A#4', 'A4', 'F4']):
        place(out, pluck(note(n_), 0.45, 0.4, 20 + i), i * 0.11, 0.7)
    puff = bandpass(noise(0.5, 21), 300, 3000) * env_adsr(int(RATE * 0.5), 0.01, 0.1, 0.0, 0.35, 0.6)
    place(out, puff, 0.48, 0.5)
    return normalize(reverb(out, 1.0, 0.25, 6), 0.8, 0.15)


def wail(seconds, seed, f0, f1, vowel_shift=1.0, breath=0.35):
    """A ghostly voice: a sine-and-overtones tone with slow vibrato gliding between two pitches, through vowel
    formants, with a breathy noise layer."""
    rng = np.random.default_rng(seed)
    t = t_axis(seconds)
    vib = 1 + 0.018 * np.sin(2 * np.pi * rng.uniform(4.5, 6.0) * t) * np.minimum(1, t / 0.3)
    phase = glide(f0, f1, seconds, 1.2) * 1.0
    phase = np.cumsum(np.diff(np.concatenate([[0], phase])) * vib)
    tone = np.sin(phase) + 0.45 * np.sin(2 * phase) + 0.2 * np.sin(3 * phase) + 0.08 * np.sin(5 * phase)
    voiced = formant(tone, [(700 * vowel_shift, 1.0), (1100 * vowel_shift, 0.6), (2600, 0.25)], q=5)
    air = formant(noise(seconds, seed), [(800 * vowel_shift, 1.0), (2400, 0.5)], q=3) * breath
    return (voiced + air + tone * 0.15) * env_adsr(len(t), seconds * 0.2, 0.2, 0.0, seconds * 0.4, 0.8)


def spirit_ambient(seed):
    rng = np.random.default_rng(seed)
    f = rng.uniform(330, 420)
    out = wail(2.2, seed, f, f * rng.uniform(0.75, 1.25), rng.uniform(0.8, 1.15))
    return normalize(reverb(out, 2.4, 0.5, seed), 0.6, 0.2)


def spirit_hurt(seed):
    rng = np.random.default_rng(seed)
    f = rng.uniform(600, 760)
    out = wail(0.55, seed, f, f * 0.7, 1.3, 0.5)
    return normalize(reverb(out, 1.0, 0.35, seed), 0.75, 0.2)


def spirit_death():
    out = np.zeros(int(RATE * 2.2))
    place(out, wail(1.6, 31, 620, 180, 1.1, 0.4), 0.0, 0.9)
    place(out, _whoosh(1.4, 32, 2500, 200, rising=False), 0.4, 0.5)
    place(out, shimmer(1.2, 33, base=2600), 0.6, 0.4)
    return normalize(reverb(out, 2.0, 0.45, 7), 0.8, 0.2)


def spirit_charge(seed):
    out = wail(0.85, seed, 420, 980, 1.25, 0.55)
    return normalize(reverb(out, 1.2, 0.35, seed), 0.8, 0.2)


def horseman_summon():
    """The Horseman's arrival: a ground-shaking boom, a cracked bell and a rising inhuman scream."""
    out = np.zeros(int(RATE * 4.0))
    t = t_axis(3.5)
    boom_phase = glide(70, 28, 3.5, 0.5)
    boom = np.sin(boom_phase) * np.exp(-t / 0.9)
    place(out, boom, 0.0, 1.0)
    place(out, lowpass(noise(3.5, 41), 220) * np.exp(-t / 1.2), 0.0, 0.8)
    place(out, bell(note('C#3'), 3.0, tau=1.2, brightness=1.2), 0.05, 0.4)
    place(out, wail(1.8, 42, 220, 520, 0.9, 0.6), 0.6, 0.55)
    place(out, _whoosh(2.0, 43, 200, 3500, rising=True), 0.3, 0.4)
    return normalize(reverb(out, 3.0, 0.35, 8), 0.95, 0.15)


def horseman_death():
    out = np.zeros(int(RATE * 4.5))
    place(out, wail(2.4, 51, 300, 90, 0.85, 0.6), 0.0, 0.9)
    place(out, bell(note('G2'), 4.0, tau=1.4, brightness=0.8), 0.6, 0.6)
    place(out, _whoosh(2.5, 52, 3000, 120, rising=False), 0.3, 0.5)
    place(out, shimmer(2.0, 53, base=3000), 1.2, 0.6)
    return normalize(reverb(out, 3.0, 0.4, 9), 0.9, 0.15)


def swing(seed):
    out = _whoosh(0.42, seed, 400, 2400, rising=True, q=1.3)
    out += _whoosh(0.42, seed + 1, 2400, 700, rising=False, q=1.5) * 0.5
    return normalize(out, 0.75, 0.2)


def bomb_throw(seed):
    out = np.zeros(int(RATE * 0.8))
    place(out, _whoosh(0.6, seed, 300, 1800, rising=True), 0.0, 0.7)
    rng = np.random.default_rng(seed)
    crackle = (rng.random(int(RATE * 0.6)) > 0.992) * rng.uniform(-1, 1, int(RATE * 0.6))
    place(out, highpass(crackle, 2000), 0.05, 0.6)
    return normalize(out, 0.7, 0.2)


def bomb_explode(seed):
    rng = np.random.default_rng(seed)
    out = np.zeros(int(RATE * 1.8))
    t = t_axis(1.4)
    place(out, np.sin(glide(110, 35, 1.4, 0.4)) * np.exp(-t / 0.35), 0.0, 1.0)
    place(out, lowpass(noise(1.4, seed), 900) * np.exp(-t / 0.3), 0.0, 0.9)
    # wet pumpkin burst
    splat = bandpass(noise(0.18, seed + 1), 250, 1800) * exp_decay(int(RATE * 0.18), 0.05)
    place(out, splat, 0.01, 0.7)
    crackle = (rng.random(int(RATE * 1.2)) > 0.993) * rng.uniform(-1, 1, int(RATE * 1.2))
    place(out, highpass(crackle, 1800) * np.linspace(1, 0, int(RATE * 1.2)), 0.1, 0.7)
    return normalize(reverb(out, 1.4, 0.25, seed), 0.95, 0.15)


def reap():
    out = np.zeros(int(RATE * 1.4))
    place(out, _whoosh(0.7, 61, 300, 3000, rising=True, q=1.3), 0.0, 0.8)
    place(out, bell(note('E5'), 1.0, tau=0.35, brightness=1.4) * 0.6, 0.28, 0.5)
    hiss = highpass(noise(0.8, 62), 3500) * env_adsr(int(RATE * 0.8), 0.02, 0.1, 0.0, 0.6, 0.5)
    place(out, hiss, 0.3, 0.25)
    return normalize(reverb(out, 1.2, 0.3, 10), 0.85, 0.2)


def spook():
    """A jump-scare stinger: a dissonant orchestral stab and a shriek."""
    out = np.zeros(int(RATE * 1.6))
    t = t_axis(1.2)
    stab = np.zeros_like(t)
    for f in (note('C3'), note('C#3'), note('F#3'), note('G4'), note('G#4')):
        ph = 2 * np.pi * f * t
        stab += sum(np.sin(k * ph) / k for k in range(1, 10))
    stab = lowpass(stab, 5000) * env_adsr(len(t), 0.005, 0.15, 0.0, 0.8, 0.5)
    place(out, stab, 0.0, 0.8)
    place(out, wail(0.7, 71, 900, 1400, 1.4, 0.5), 0.0, 0.6)
    place(out, highpass(noise(0.15, 72), 1500) * exp_decay(int(RATE * 0.15), 0.03), 0.0, 0.5)
    return normalize(reverb(out, 1.4, 0.3, 11), 0.95, 0.2)


def heartbeat():
    out = np.zeros(int(RATE * 0.62))
    place(out, thump(0.2, 52), 0.0, 1.0)
    place(out, thump(0.2, 46), 0.2, 0.7)
    return normalize(lowpass(out, 400), 0.85, 0.2)


def cursed_pulse():
    out = np.zeros(int(RATE * 1.6))
    place(out, thump(0.45, 40), 0.0, 1.0)
    t = t_axis(1.3)
    chord = sum(np.sin(2 * np.pi * note(n_) * t) for n_ in ('A2', 'D#3', 'G3'))
    place(out, chord * env_adsr(len(t), 0.1, 0.2, 0.0, 0.8, 0.6) * 0.3, 0.05, 1.0)
    place(out, shimmer(1.0, 81, base=2200) * 0.5, 0.1, 0.5)
    return normalize(reverb(out, 1.6, 0.4, 12), 0.85, 0.2)


# file name -> generator
SOUNDS = {
    'witching_bell': witching_bell,
    'whispers1': lambda: whispers(1), 'whispers2': lambda: whispers(2), 'whispers3': lambda: whispers(3),
    'owl1': lambda: owl(1), 'owl2': lambda: owl(2),
    'chimes1': lambda: chimes(1), 'chimes2': lambda: chimes(2),
    'wind1': lambda: wind(1), 'wind2': lambda: wind(2),
    'wisp_ambient1': lambda: wisp_ambient(1), 'wisp_ambient2': lambda: wisp_ambient(2),
    'wisp_chime': wisp_chime, 'wisp_vanish': wisp_vanish,
    'cache_open': cache_open,
    'candy_eat1': lambda: candy_eat(1), 'candy_eat2': lambda: candy_eat(2), 'candy_eat3': lambda: candy_eat(3),
    'treat': treat, 'trick': trick,
    'spirit_ambient1': lambda: spirit_ambient(1), 'spirit_ambient2': lambda: spirit_ambient(2),
    'spirit_ambient3': lambda: spirit_ambient(3),
    'spirit_hurt1': lambda: spirit_hurt(1), 'spirit_hurt2': lambda: spirit_hurt(2),
    'spirit_death': spirit_death,
    'spirit_charge1': lambda: spirit_charge(1), 'spirit_charge2': lambda: spirit_charge(2),
    'horseman_summon': horseman_summon, 'horseman_death': horseman_death,
    'swing1': lambda: swing(1), 'swing2': lambda: swing(2),
    'bomb_throw1': lambda: bomb_throw(1), 'bomb_throw2': lambda: bomb_throw(2),
    'bomb_explode1': lambda: bomb_explode(1), 'bomb_explode2': lambda: bomb_explode(2),
    'reap': reap, 'spook': spook, 'heartbeat': heartbeat, 'cursed_pulse': cursed_pulse,
}


def entry(name, **kw):
    e = {'name': name}
    e.update(kw)
    return e


def mine(*names, **kw):
    return [entry(f'hallowed:{n}', **kw) for n in names]


def vanilla(*names, **kw):
    return [entry(f'minecraft:{n}', **kw) for n in names]


def sounds_json():
    def ev(key, sounds, subtitle=True, **extra):
        d = {'sounds': sounds}
        if subtitle:
            d['subtitle'] = f'subtitles.hallowed.{key}'
        d.update(extra)
        return key, d

    events = [
        ev('event.witching_bell', mine('witching_bell', attenuation_distance=64)),
        ev('ambient.whispers', mine('whispers1', 'whispers2', 'whispers3', volume=0.5)),
        ev('ambient.owl', mine('owl1', 'owl2', volume=0.45)),
        ev('ambient.chimes', mine('chimes1', 'chimes2', volume=0.4)),
        ev('ambient.wind', mine('wind1', 'wind2', volume=0.5)),
        ev('entity.wisp.ambient', mine('wisp_ambient1', 'wisp_ambient2')),
        ev('entity.wisp.chime', mine('wisp_chime')),
        ev('entity.wisp.vanish', mine('wisp_vanish')),
        ev('block.hallowed_cache.open', mine('cache_open')),
        ev('item.candy.eat', mine('candy_eat1', 'candy_eat2', 'candy_eat3')),
        ev('event.treat', mine('treat')),
        ev('event.trick', mine('trick')),
        ev('block.pumpkin.carve', vanilla('block/pumpkin/carve1', 'block/pumpkin/carve2')),
        ev('entity.spirit.ambient', mine('spirit_ambient1', 'spirit_ambient2', 'spirit_ambient3')),
        ev('entity.spirit.hurt', mine('spirit_hurt1', 'spirit_hurt2')),
        ev('entity.spirit.death', mine('spirit_death')),
        ev('entity.spirit.charge', mine('spirit_charge1', 'spirit_charge2')),
        ev('entity.hollow_horseman.laugh', vanilla('entity/witch/celebrate', pitch=0.55) + vanilla('entity/witch/celebrate', pitch=0.62)),
        ev('entity.hollow_horseman.summon', mine('horseman_summon', attenuation_distance=64)),
        ev('entity.hollow_horseman.charge', vanilla('mob/horse/angry1', pitch=0.72) + vanilla('mob/horse/skeleton/death', pitch=1.2)),
        ev('entity.hollow_horseman.hurt', vanilla('mob/wither_skeleton/hurt1', 'mob/wither_skeleton/hurt2', 'mob/wither_skeleton/hurt3',
                                                   pitch=0.65)),
        ev('entity.hollow_horseman.death', mine('horseman_death', attenuation_distance=48)),
        ev('entity.hollow_horseman.dismount', vanilla('mob/horse/skeleton/death', pitch=0.75)),
        ev('entity.hollow_horseman.swing', mine('swing1', 'swing2')),
        ev('entity.pumpkin_bomb.throw', mine('bomb_throw1', 'bomb_throw2')),
        ev('entity.pumpkin_bomb.explode', mine('bomb_explode1', 'bomb_explode2')),
        ev('item.reaper_scythe.reap', mine('reap')),
        ev('event.spook', mine('spook')),
        ev('effect.spooked.heartbeat', mine('heartbeat')),
        ev('block.cursed_pumpkin.pulse', mine('cursed_pulse')),
        ev('music_disc.witching_hour', mine('music/witching_hour', stream=True), subtitle=False),
    ]
    return dict(events)
