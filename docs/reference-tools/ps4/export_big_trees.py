#!/usr/bin/env python3
"""Capture original big oak, its constructor configuration and RNG split."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_big_trees import BigTreeOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=BigTreeOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','stone_obstacle','high_obstacle','off_axis','sky_obstacle','trunk_obstacle','leaves','glass','vine_column','sapling_column','protected']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,119,11],[17,247,-23],[3,254,7]]:
            for updates in [False,True]:
                for prepared in [False,True]:
                    initial=oracle.construct(updates,prepared=prepared)
                    cases.append({'updates':updates,'prepared':prepared,'log':17,'leaves':18,'constructor':initial,'placements':[oracle.place(seed,position,profile)]})
    print('Original big oak seed',seed,'cases',len(cases),flush=True)
for meta in range(16):
    for prepared in [False,True]:
        initial=oracle.construct(False,log_meta=meta,leaves_meta=meta,prepared=prepared)
        cases.append({'updates':False,'prepared':prepared,'log':17|(meta<<16),'leaves':18|(meta<<16),'constructor':initial,
                      'placements':[oracle.place(1,[0,64,0],'high_obstacle'),oracle.place(9,[-31,64,17],'grass'),oracle.place(-1,[31,64,-17],'off_axis')]})
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x7e020','0x7f040','0x7f050','0x7f230','0x7e190','0x7e6c0','0x7e880','0x7ea30','0x7eb90','0x7ede0','0x502cc0'],
      'input_kind':'Original big oak constructor and full geometry with controlled voxel/material/property bindings and supplied log/leaf default states. Clock-based initial Random seed replaced by zero because place unconditionally reseeds it; not a whole-world or registry-initializer capture.',
      'cases':cases,'leaf_data_cases':oracle.leaf_data_cases()}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-big-trees.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
