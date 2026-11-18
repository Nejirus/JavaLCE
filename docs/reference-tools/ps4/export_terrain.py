#!/usr/bin/env python3
"""Replay original PS4 density, base terrain and surface-noise routines.

Linux x86-64 and the pinned mcps4 executable/library are required. Biome
rectangles and numeric depth/scale fields are controlled inputs, not an
independent capture of the game's biome constructors or of a complete world.
"""
import argparse
import gzip
import hashlib
import json
import platform
import struct
from pathlib import Path

from native_terrain import TerrainOracle

ROOT = Path(__file__).resolve().parents[3]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot', type=Path, required=True)
parser.add_argument('--libc', type=Path, required=True)
parser.add_argument('--output-dir', type=Path, required=True)
parser.add_argument('--terrain-input', type=Path,
                    default=ROOT / 'src/test/resources/worldgen/ps4-terrain-vectors.json')
parser.add_argument('--surface-input', type=Path,
                    default=ROOT / 'src/test/resources/worldgen/ps4-surface-vectors.json')
parser.add_argument('--biome-fields', type=Path,
                    default=Path(__file__).with_name('biome-depth-scale-inputs.json'))
args = parser.parse_args()
if platform.system() != 'Linux' or platform.machine() not in ('x86_64', 'AMD64'):
    parser.error('Linux x86-64 is required')
expected = {
    args.eboot: 'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',
    args.libc: '1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816',
}
for path, digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest() != digest:
        parser.error(f'Unexpected mcps4 input: {path}')
args.output_dir.mkdir(parents=True, exist_ok=True)
properties = json.loads(args.biome_fields.read_text())
oracle = TerrainOracle(args.eboot, args.libc, properties)
generators = {}


def generator(seed):
    if seed not in generators:
        generators[seed] = oracle.generator(seed)
    return generators[seed]


def require_bits(actual, expected_values, label):
    if len(actual) != len(expected_values):
        raise RuntimeError(f'{label}: length mismatch')
    for i, (got, want) in enumerate(zip(actual, expected_values)):
        if struct.pack('<d', got) != struct.pack('<d', want):
            raise RuntimeError(f'{label}[{i}]: original={got!r}, fixture={want!r}')


base_cases = []
for case in json.loads(args.terrain_input.read_text())['cases']:
    seed, width = case['seed'], case['width']
    cx, cz = case['chunk']
    gen = generator(seed)
    values = oracle.get_heights(gen, cx, cz, case['biomes'])
    require_bits(values, case['values'], f'density {seed} {cx},{cz}')
    blocks = oracle.prepare(gen, width, cx, cz, case['biomes'])
    base_cases.append({'seed': seed, 'width': width, 'chunk': [cx, cz],
                       'blocks': list(blocks)})
    print(f'Original density/base {seed} {width} {cx},{cz}: matched', flush=True)
surface_count = 0
for case in json.loads(args.surface_input.read_text())['cases']:
    cx, cz = case['chunk']
    values = oracle.surface_noise(generator(case['seed']), cx, cz)
    require_bits(values, case['values'], f'surface noise {case["seed"]} {cx},{cz}')
    surface_count += len(values)
meta = {
    'repository': 'https://github.com/Nejirus/mcps4',
    'commit': '37e258085773f11599cf0ffe29119926769554b7',
    'eboot_sha256': expected[args.eboot],
    'libc_sha256': expected[args.libc],
    'input_kind': 'Controlled biome rectangles and numeric depth/scale fields; not PS4 world saves',
    'biome_fields_source': properties['note'],
    'entrypoints': ['0x507470', '0x506dd0', '0x627bf0'],
    'cases': base_cases,
}
path = args.output_dir / 'ps4-base-chunks.json.gz'
path.write_bytes(gzip.compress(json.dumps(meta, separators=(',', ':')).encode(), mtime=0))
print(f'{len(base_cases) * 425} density doubles and {surface_count} surface-noise doubles matched by bits')
print(path.name, hashlib.sha256(path.read_bytes()).hexdigest())
