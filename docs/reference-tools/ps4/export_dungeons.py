#!/usr/bin/env python3
"""Capture original dungeon geometry, chest orientation, loot seeds and mob choice."""
import argparse
import gzip
import hashlib
import itertools
import json
import platform
from pathlib import Path
from native_dungeons import DungeonOracle,C,I

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):
    parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=DungeonOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['tunnel','one_hole','four_holes','no_holes','many_holes','floor_hole','ceiling_hole','unsupported','old_chests','old_spawner','wrong_entities']:
        for position in [[0,64,0],[-2493,30,2501],[5,0,-7]]:
            cases.append(oracle.dungeon(seed,position,profile))
    print('Original dungeon seed',seed,'cases',len(cases),flush=True)
namespace=C.string_at(oracle.game.base+0xe7ab10,18).decode('utf-16le')
path=C.string_at(oracle.game.base+0xe7ada2,42).decode('utf-16le')
types={name:{'key':oracle.call(pc,I,[],[]),'method':hex(pc),'rtti':hex(rtti)} for name,pc,rtti in [('skeleton',0x68d370,0x12d8de0),('zombie',0x7af830,0x12fe9e0),('spider',0x68e6c0,0x12da0b0)]}
meta={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x497e40','0x1746f0','0x176000','0x498a40'],
      'input_kind':'Controlled voxel/material/entity APIs, shared-pointer and entity-name/token bindings. Original mob-choice initializer and selection execute; not world saves or loot-content captures.',
      'loot_table':namespace+':'+path,'loot_reference':'0x140c3a0','loot_initializer':'0x152a1f..0x152a4b',
      'mob_choice_keys':oracle.mob_choice_keys,'entity_types':types}
args.output_dir.mkdir(parents=True,exist_ok=True)
def save(name,data):
    path=args.output_dir/name
    path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
    print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
save('ps4-dungeons.json.gz',{**meta,'cases':cases})
cases=[]
for neighbors in itertools.product([0,1,20,54],repeat=4):
    for facing in [2,3,4,5]:cases.append(oracle.chest(list(neighbors),facing))
save('ps4-chest-facing.json.gz',{**meta,'cases':cases})
