#!/usr/bin/env python3
"""Capture original flower/grass selectors and canonical property conversion."""
import argparse,gzip,hashlib,json,math,platform
from pathlib import Path
from native_flora_selection import FloraSelectionOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=FloraSelectionOracle(args.eboot,args.libc)
modes=[('DEFAULT',0),*[('FOREST',v) for v in range(4)],('PLAINS',0),('SWAMP',0)]
seeds=[*range(64),-1,-987654321,-9223372036854775808,9223372036854775807]
positions=[[0,64,0],[1,0,-1],[47,255,48],[-49,64,49],[237,64,-301],[-2493,64,2501],[512,64,768],[4999,64,-5000],[-5000,64,4999]]
flowers=[];grass=[]
for kind,variant in modes:
    for seed in seeds:
        for position in positions:flowers.append(oracle.flower(kind,variant,seed,position))
    print('Original flower selector',kind,variant,'cases',len(flowers),flush=True)
for kind,variant in [('FOREST',1),('PLAINS',0)]:
    for seed in seeds:
        for noise in [-2.,-1.,math.nextafter(-.8,-math.inf),-.8,math.nextafter(-.8,math.inf),-.5,0.,.5,.9998,1.,math.nextafter(1.,-math.inf),2.]:flowers.append(oracle.flower(kind,variant,seed,[17,64,-31],noise))
for kind in oracle.GRASS:
    for seed in [*range(512),-1,-987654321,-9223372036854775808,9223372036854775807]:grass.append(oracle.grass(kind,seed))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':[hex(v) for v in oracle.FLOWERS.values()]+[hex(v) for v in oracle.GRASS.values()]+['0x34cbc0','0x2ea400','0x2e9b60','0x6d39c0','0x6d3750','0x52a300',hex(oracle.original_noise)],
      'input_kind':'Original flower enum initializer and selectors, native seed-2345 noise, selected feature property construction and original metadata getters. Grass enum IDs read from original initializer immediates. Canonical state/property storage and voxel-independent inputs supplied; additional cases bind exact noise boundary values. Not complete registry or whole-world captures.',
      'flower_types':oracle.flower_inputs,'grass_types':oracle.grass_inputs,'flowers':flowers,'grass':grass}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-flora-selection.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(flowers),len(grass),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
