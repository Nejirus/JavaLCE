#!/usr/bin/env python3
"""Capture original PS4 ore writes, world reads, rule queries and following RNG."""
import argparse
import gzip
import hashlib
import json
import platform
from pathlib import Path

from native_ores import OreOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):
    parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:
        parser.error(f'Unexpected mcps4 input: {path}')
oracle=OreOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,-987654321,9223372036854775807]:
    for size in [1,2,3,4,8,16,17,33,49,64]:
        for position,profile,height in [([0,24,0],'stone',128),([-2499,70,2501],'mixed',64),([13,100,-17],'air',0),([-15,0,16],'mixed',128),([30,63,-32],'rules',128)]:
            cases.append(oracle.place(seed,size,position,profile,height,56 if size%2 else 1|(3<<16)))
    print('Original ore seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x503f60','0x504a30','0x67f1f0'],
      'input_kind':'Controlled block, height and generation-rule callbacks, original stone predicate/enum flags; not PS4 world saves',
      'predicate_cases':oracle.predicate_cases(),'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-ores.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
cases=[]
for seed in [0,1,-987654321,9223372036854775807]:
    for mode in ['default','ranges','disabled']:
        for origin,profile,height in [([0,0],'stone',128),([-2496,2480],'mixed',64)]:
            cases.append(oracle.decorate(seed,origin,mode,profile,height))
    print('Original ore decorator seed',seed,'cases',len(cases),flush=True)
data={**data,'entrypoints':['0x88970','0x1f1940','0x503f60','0x504a30','0x67f1f0'],
      'input_kind':'Original ore dispatcher, default settings and feature kernel; controlled block/height/rule callbacks and legacy state bindings; not PS4 world saves',
      'cases':cases}
path=args.output_dir/'ps4-ore-decoration.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
