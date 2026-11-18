#!/usr/bin/env python3
"""Capture original scatter, soil, survival and double-plant placement code."""
import argparse
import gzip
import hashlib
import itertools
import json
import platform
from pathlib import Path
from native_flora import FloraOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=FloraOracle(args.eboot,args.libc)
meta={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':[hex(pc) for pc in oracle.FEATURES.values()]+['0x1390c0','0x6d2d70','0x138df0','0x138ed0','0x1f6f00','0x780740','0x780790','0x159560','0x5b3f80','0x213a40','0x213d50','0x214150','0x4a0830'],
      'input_kind':'Controlled voxel/material/fluid APIs, dimension flags, enum/property bindings and protection boxes. Original scatter AND plant placement/survival code execute. Not whole-world captures or a complete block/material registry initialization.'}
args.output_dir.mkdir(parents=True,exist_ok=True)
def save(name,cases):
    path=args.output_dir/name
    path.write_bytes(gzip.compress(json.dumps({**meta,'cases':cases},separators=(',',':')).encode(),mtime=0))
    print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)

cases=[]
profiles=['grass','dirt','farmland','stone','sand','wet_sand','waterlogged','frosted','lava_side','water','flowing','flowing_level','ice','packed_ice','canopy','terracotta','protected','podzol','mycelium','dark','bright']
for kind in oracle.FEATURES:
    for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
        state={'flower':37 if seed&1 else 38|(8<<16),'tall_grass':31|((1+(seed&1))<<16),'dead_bush':32,'waterlily':111,'cactus':81,'reeds':83,'double_plant':175|((seed%6)<<16),'mushroom':39+(seed&1)}[kind]
        for profile in profiles:
            for position,ceiling in [([0,64,0],False),([-2493,127,2501],False),([5,0,-7],False),([-9,126,11],True)]:
                cases.append(oracle.scatter(kind,seed,position,profile,state,ceiling))
    print('Original scatter',kind,'cases',len(cases),flush=True)
save('ps4-flora.json.gz',cases)

cases=[]
below_states=[0,1,2,3,3|(2<<16),12,12|(1<<16),60,9,9|(3<<16),8,8|(1<<16),79,174,212,159,172,81,83,175,175|(8<<16)]
for state in [37,38,31|(2<<16),32,111,175|(4<<16),175|(8<<16)]:
    for below in below_states:
        for current in [0,1,31]:
            for above in [0,175|(8<<16)]:
                for kind in ['may_place','can_stay']:
                    blocks=[[0,63,0,below],[0,64,0,current],[0,65,0,above]]
                    cases.append(oracle.rule(kind,state,[0,64,0],blocks,[]))
for y in [-1,0,255,256]:
    for below in below_states:
        cases.append(oracle.rule('can_stay',111,[0,y,0],[[0,y-1,0,below]],[]))
for neighbors in itertools.product([0,1,9,10,18],repeat=4):
    for below in [0,12,81,2]:
        for above in [0,1,9]:
            blocks=[[0,63,0,below],[0,65,0,above]]+[[oracle.DIRECTIONS[d][0],64,oracle.DIRECTIONS[d][2],tile] for d,tile in zip(oracle.HORIZONTAL,neighbors)]
            cases.append(oracle.rule('can_stay',81,[0,64,0],blocks,[]))
for below in [0,1,2,3,12,83]:
    for current in [0,1,83]:
        for direction in oracle.HORIZONTAL:
            dx,dy,dz=oracle.DIRECTIONS[direction]
            for neighbor in [0,1,9,8,212,79,20]:
                for fluid in [False,True]:
                    blocks=[[0,63,0,below],[0,64,0,current],[dx,63,dz,neighbor]]
                    cases.append(oracle.rule('can_stay',83,[0,64,0],blocks,[[dx,63,dz]] if fluid else []))
for state in [39,40]:
    for y in [-1,0,64,255,256]:
        for below in below_states+[110]:
            for current in [0,1,31]:
                for brightness in [0,12,13,15]:
                    for kind in ['may_place','can_stay']:
                        cases.append(oracle.rule(kind,state,[0,y,0],[[0,y-1,0,below],[0,y,0,current]],[],brightness))
save('ps4-plant-rules.json.gz',cases)
