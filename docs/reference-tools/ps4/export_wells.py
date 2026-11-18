#!/usr/bin/env python3
"""Capture original desert-well geometry, constructor states and sand predicate."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_wells import WellOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=WellOracle(args.eboot,args.libc);cases=[]
positions=[[0,64,0],[-2493,1,2501],[5,0,-7],[-9,2,11],[17,3,-23],[3,127,7],[9,254,-11]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for position in positions:
        for profile in ['sand','red_sand','stone','dirt','empty_below']:
            for offset in [0,-1,-2,-5,-64,3]:cases.append(oracle.well(seed,position,profile,offset))
        for x in range(-2,3):
            for z in range(-2,3):
                for levels in [[-1],[-2],[-1,-2]]:cases.append(oracle.well(seed,position,'holes',0,[[x,y,z] for y in levels]))
    print('Original well seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x1fa530','0x1fb400','0x1fb370','0xd0f00','0xd1080','0xd0ff0','0x22aa00','0x22aa40','0x684820','0x685550','0x5c33b0'],
      'input_kind':'Original well geometry/constructor, sand/slab variant initializers, sand predicate construction/evaluation and slab metadata getter. Enum-property/StateDefinition/canonical storage, slab half-enum identities, block defaults and voxel contexts are controlled; writes succeed. Not whole-world or save captures.',
      'variant_inputs':oracle.variant_inputs,'configurations':oracle.configurations,'predicate_cases':oracle.predicate_cases,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-wells.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
