#!/usr/bin/env python3
"""Capture original giant-tree kernels and post-placement ground passes."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_trees import TreeOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=TreeOracle(args.eboot,args.libc)
cases=[]
for kind in ['mega_pine','mega_pine_post']:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
            for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,235,-23],[3,250,7]]:
                for updates in [False,True]:
                    for tall in [False,True]:cases.append(oracle.tree(kind,seed,position,profile,13,17|(1<<16),18|(1<<16),updates,tall=tall))
        print('Original giant tree',kind,'seed',seed,'cases',len(cases),flush=True)
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,235,-23],[3,250,7]]:
            for updates in [False,True]:cases.append(oracle.tree('mega_jungle',seed,position,profile,10,17|(3<<16),18|(3<<16),updates,extra_range=20))
    print('Original giant tree mega_jungle seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x4517f0','0x450720','0x4523a0','0x4523f0','0x4525b0','0x4526e0','0x452730','0x4528a0','0x451c40','0x451f70','0x452120'],
      'input_kind':'Original full kernels and shared giant-tree helpers with controlled voxel/material/property bindings. Feature heights/ranges/states and podzol state supplied; not full constructor/biome-selector or whole-world captures.',
      'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-mega-trees.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
