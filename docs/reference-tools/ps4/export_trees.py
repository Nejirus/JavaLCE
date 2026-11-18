#!/usr/bin/env python3
"""Capture recovered original tree kernels, block probes, writes and RNG."""
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
for kind in ['ground_bush','pine']:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
            for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
                for updates in [False,True]:
                    cases.append(oracle.tree(kind,seed,position,profile,7 if seed&1 else 5,17|((3 if kind=='ground_bush' else 1)<<16),18|((0 if kind=='ground_bush' else 1)<<16),updates))
        print('Original tree',kind,'seed',seed,'cases',len(cases),flush=True)
for kind in ['trunk','fallen']:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected','gaps','wide_gaps','raised']:
            for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
                for updates in [False,True]:
                    for species in [0,2,3]:
                        for jungle in ([False,True] if kind=='trunk' else [False]):
                            cases.append(oracle.tree(kind,seed,position,profile,7 if seed&1 else 5,17|(species<<16),18,updates,jungle))
        print('Original tree',kind,'seed',seed,'cases',len(cases),flush=True)
for seed in range(32):
    for species in [0,2,3]:cases.append(oracle.tree('fallen',seed,[0,64,0],'grass',8,17|(species<<16),18,False))
for kind in ['basic','birch']:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
            for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
                for updates in [False,True]:
                    for variant in [False,True]:
                        species=2 if kind=='birch' else 3 if variant else 0
                        cases.append(oracle.tree(kind,seed,position,profile,5 if kind=='birch' else 7 if variant else 4,17|(species<<16),18|(species<<16),updates,kind=='basic' and variant,kind=='birch' and variant))
        print('Original tree',kind,'seed',seed,'cases',len(cases),flush=True)
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected','shallow_water','deep_water','flowing_surface']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
            for updates in [False,True]:cases.append(oracle.tree('swamp',seed,position,profile,5,17,18,updates))
    print('Original tree swamp seed',seed,'cases',len(cases),flush=True)
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
            for updates in [False,True]:cases.append(oracle.tree('savanna',seed,position,profile,5,162,161,updates))
    print('Original tree savanna seed',seed,'cases',len(cases),flush=True)
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,125,-23],[3,250,7]]:
            for updates in [False,True]:cases.append(oracle.tree('roof',seed,position,profile,6,162|(1<<16),161|(1<<16),updates))
    print('Original tree roof seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x33b160','0x5345c0','0x6f5eb0','0x8f390','0x6ca560','0x5c6ec0','0x5bfc60','0x5c1550','0x6f5b60','0x6f6950','0x6caff0','0x1c390','0x1c430','0x1c560','0x1cb80','0x5036e0','0x774820','0x1a9010','0x2a4960'],
      'input_kind':'Original kernels and shared tree predicates/dirt/write helpers execute with controlled voxel/material/solid-render/protection APIs and supplied feature states/heights. Not constructor/biome-selector or whole-world captures.',
      'predicates':oracle.tree_predicates(),'log_data_cases':oracle.log_data_cases(),'plant_data_cases':oracle.plant_data_cases(),'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-trees.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
