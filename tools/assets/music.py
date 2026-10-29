"""'Witching Hour', the music disc: an original waltz in D minor, about two minutes, rendered note by note.

Theremin-like lead, music box, pizzicato bass with oom-pah-pah chords, a soft organ, a wordless choir, a ticking
clock in the intro and the midnight bell in the bridge.
"""
from __future__ import annotations

import numpy as np

from synth import RATE, bell, env_adsr, exp_decay, formant, highpass, lowpass, noise, normalize, note, organ, place, pluck, \
    reverb, t_axis

BPM = 132
BEAT = 60 / BPM
BAR = BEAT * 3

CHORDS = {
    'Dm': ['D3', 'F3', 'A3'], 'Gm': ['G2', 'Bb2', 'D3'], 'A7': ['A2', 'C#3', 'G3'], 'A': ['A2', 'C#3', 'E3'],
    'Bb': ['Bb2', 'D3', 'F3'], 'F': ['F2', 'A2', 'C3'], 'C': ['C3', 'E3', 'G3'], 'Dm/C': ['C3', 'F3', 'A3'],
    'Bb6': ['Bb2', 'D3', 'G3'], 'Gm/Bb': ['Bb2', 'D3', 'G3'],
}

# (chord, melody) per bar; melody is three beats, '.' holds the previous note, '-' is a rest.
A_SECTION = [
    ('Dm', 'D5 . A4'), ('Dm', 'F5 E5 D5'), ('Gm', 'G5 . D5'), ('A7', 'C#5 . A4'),
    ('Dm', 'D5 E5 F5'), ('Bb', 'G5 F5 E5'), ('Gm', 'D5 Bb4 G4'), ('A7', 'A4 . -'),
    ('Dm', 'D5 . A4'), ('Dm', 'F5 E5 D5'), ('Gm', 'Bb5 . A5'), ('Gm', 'G5 F5 E5'),
    ('Bb', 'F5 D5 Bb4'), ('A7', 'E5 C#5 A4'), ('Dm', 'D5 . .'), ('A7', 'A4 C#5 E5'),
]
B_SECTION = [
    ('F', 'A5 . F5'), ('C', 'G5 E5 C5'), ('Dm', 'F5 . D5'), ('A', 'E5 . C#5'),
    ('Bb', 'D5 F5 Bb5'), ('F', 'A5 . F5'), ('Gm', 'G5 Bb5 G5'), ('A7', 'A5 . -'),
    ('Dm/C', 'F5 E5 D5'), ('Bb6', 'D5 C#5 D5'), ('Gm/Bb', 'Bb4 A4 G4'), ('A', 'A4 . C#5'),
    ('Dm', 'D5 F5 A5'), ('Gm', 'Bb5 A5 G5'), ('A7', 'F5 E5 C#5'), ('Dm', 'D5 . .'),
]
INTRO = [('Dm', '- - -'), ('Dm', '- - -'), ('Gm', '- - -'), ('A7', '- - -')] * 2
BRIDGE = [('Dm', '- - -'), ('Bb', '- - -'), ('Gm', '- - -'), ('A7', '- - -')] * 2
OUTRO = [('Dm', 'D5 . A4'), ('Gm', 'Bb4 . G4'), ('A7', 'A4 . C#5'), ('Dm', 'D5 . .'), ('Dm', '- - -'), ('Dm', '- - -'),
         ('Dm', '- - -'), ('Dm', '- - -')]

# section, lead instrument, accompaniment level
FORM = [
    (INTRO, None, 0.0),
    (A_SECTION, 'theremin', 1.0),
    (B_SECTION, 'celesta', 1.0),
    (A_SECTION, 'theremin', 1.2),
    (BRIDGE, None, 0.4),
    (A_SECTION, 'both', 1.3),
    (OUTRO, 'celesta', 0.6),
]


def theremin(freq, prev, seconds, seed):
    """A singing sine with vibrato that slides in from the previous note."""
    t = t_axis(seconds)
    slide = 0.07
    f = np.full_like(t, freq)
    if prev:
        k = np.minimum(1, t / slide)
        f = prev * (freq / prev) ** (k * k * (3 - 2 * k))
    vib = 1 + 0.012 * np.sin(2 * np.pi * 5.6 * t + seed) * np.minimum(1, t / 0.25)
    phase = 2 * np.pi * np.cumsum(f * vib) / RATE
    tone = np.sin(phase) + 0.18 * np.sin(2 * phase) + 0.05 * np.sin(3 * phase)
    return tone * env_adsr(len(t), 0.06, 0.1, 0.0, min(0.25, seconds * 0.35), 0.85)


def celesta(freq, seconds):
    return bell(freq, max(seconds, 1.2), tau=0.7, brightness=0.45)


def choir(chord, seconds, seed):
    t = t_axis(seconds)
    out = np.zeros_like(t)
    for i, n_ in enumerate(chord):
        f = note(n_) * 2
        vib = 1 + 0.006 * np.sin(2 * np.pi * (5 + i * 0.3) * t + i)
        phase = 2 * np.pi * np.cumsum(f * vib) / RATE
        saw = sum(np.sin(k * phase) / k for k in range(1, 12))
        out += saw
    voiced = formant(out, [(400, 1.0), (800, 0.6), (2600, 0.15)], q=5)
    return voiced * env_adsr(len(t), seconds * 0.35, 0.1, 0.0, seconds * 0.35, 0.9)


def tick(seed):
    click = highpass(noise(0.03, seed), 2500) * exp_decay(int(RATE * 0.03), 0.004)
    return click


def timpani(freq):
    t = t_axis(1.2)
    return (np.sin(2 * np.pi * freq * t) + 0.4 * np.sin(2 * np.pi * freq * 1.5 * t)) * np.exp(-t / 0.35) \
        + lowpass(noise(1.2, int(freq)), 300) * np.exp(-t / 0.08) * 0.4


def render():
    bars = sum(len(section) for section, _, _ in FORM)
    total = bars * BAR + 6
    lead = np.zeros(int(RATE * total))
    accomp = np.zeros_like(lead)
    pad = np.zeros_like(lead)
    fx = np.zeros_like(lead)
    rng = np.random.default_rng(3)
    t0 = 0.6
    bar_index = 0
    prev = None
    for s_i, (section, instrument, level) in enumerate(FORM):
        for chord_name, melody in section:
            start = t0 + bar_index * BAR
            chord = CHORDS[chord_name]
            # oom-pah-pah
            if level > 0:
                place(accomp, pluck(note(chord[0]) / 2, 1.0, 0.35, bar_index, 0.994), start, 0.9 * level)
                for beat in (1, 2):
                    for j, n_ in enumerate(chord[1:] + [chord[0]]):
                        place(accomp, pluck(note(n_) * 2, 0.5, 0.5, bar_index * 7 + beat * 3 + j, 0.990), start + beat * BEAT,
                              0.28 * level)
                place(pad, organ(note(chord[0]), BAR, attack=0.3, release=0.4), start, 0.18 * level)
                place(pad, organ(note(chord[2]), BAR, attack=0.3, release=0.4), start, 0.12 * level)
            # intro and bridge: ticking clock and a music-box arpeggio
            if instrument is None:
                for beat in range(3):
                    place(fx, tick(bar_index * 3 + beat), start + beat * BEAT, 0.35)
                arp = [chord[0], chord[1], chord[2], chord[1], chord[2], chord[0]]
                for k, n_ in enumerate(arp):
                    place(lead, celesta(note(n_) * 4, 1.2), start + k * BEAT / 2, 0.22)
                if section is BRIDGE:
                    place(pad, choir(chord, BAR + 0.5, bar_index), start, 0.35)
                    if bar_index % 4 == 0:
                        place(fx, bell(note('D2'), 5.0, tau=2.2, brightness=0.8), start, 0.5)
            # melody
            if instrument is not None:
                notes = melody.split()
                k = 0
                while k < 3:
                    n_ = notes[k]
                    length = 1
                    while k + length < 3 and notes[k + length] == '.':
                        length += 1
                    if n_ not in ('.', '-'):
                        f = note(n_)
                        dur = length * BEAT
                        at = start + k * BEAT
                        if instrument in ('theremin', 'both'):
                            place(lead, theremin(f, prev, dur + 0.05, k + bar_index), at, 0.55)
                            prev = f
                        if instrument in ('celesta', 'both'):
                            place(lead, celesta(f * (2 if instrument == 'celesta' else 4), dur), at, 0.35 if instrument == 'both' else 0.5)
                    elif n_ == '-':
                        prev = None
                    k += length
            if s_i == 5 and bar_index % 2 == 0:
                place(fx, timpani(note(chord[0]) / 2), start, 0.5)
            bar_index += 1
    mix = lead * 1.0 + accomp * 0.8 + pad * 0.7 + fx * 0.8
    mix = lowpass(mix, 11000)
    mix = reverb(mix, 2.6, 0.32, 21, damp=6000)
    fade = int(RATE * 4)
    mix[-fade:] *= np.linspace(1, 0, fade)
    return normalize(mix, 0.92)


LENGTH = sum(len(section) for section, _, _ in FORM) * BAR + 0.6
