#!/usr/bin/env python3
"""Capture original PS4 lake masks, ordered world queries, writes and RNG."""
import argparse
import gzip
import hashlib
import json
import platform
from pathlib import Path
from native_lakes import LakeOracle

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
oracle=LakeOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,-987654321,9223372036854775807]:
    for tile in [9,11]:
        for profile in ['ground','mushroom','cold','flowing','bright','flooded','leaking','empty','protected']:
            for pos in [[0,70,0],[-2493,70,2501],[7,4,-8]]:
                cases.append(oracle.lake(seed,pos,profile,tile))
    print('Original lake seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoint':'0x3db900','input_kind':'Controlled block/material, light, biome and generation-rule callbacks; not PS4 world saves','cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-lakes.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
