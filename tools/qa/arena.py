"""Builds a small flat QA arena in the sky and parks the QA player at its edge."""
import sys
import time

sys.path.insert(0, '.')
from client import Session

AX, AY, AZ = 0, 150, 0


def build(s):
    s.cmd(f'forceload add {AX - 32} {AZ - 32} {AX + 32} {AZ + 32}')
    s.cmd(f'fill {AX - 16} {AY - 1} {AZ - 16} {AX + 16} {AY - 1} {AZ + 16} minecraft:grass_block')
    s.cmd(f'fill {AX - 16} {AY - 2} {AZ - 16} {AX + 16} {AY - 2} {AZ + 16} minecraft:dirt')
    s.cmd(f'fill {AX - 16} {AY} {AZ - 16} {AX + 16} {AY + 12} {AZ + 16} minecraft:air')


def park(s, x=AX, z=AZ + 8, yaw=180, pitch=8):
    s.cmd(f'tp Trickster {x} {AY} {z} {yaw} {pitch}')


if __name__ == '__main__':
    s = Session(display=':1')
    build(s)
    park(s)
    time.sleep(4)
