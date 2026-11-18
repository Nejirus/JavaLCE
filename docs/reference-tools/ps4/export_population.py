#!/usr/bin/env python3
"""Capture original PS4 population ordering and shared RNG with controlled features."""
import argparse
import gzip
import hashlib
import json
import platform
from pathlib import Path
from native_population import PopulationOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
parser.add_argument('--native-mobs',action='store_true',help='Execute the original empty population mob helper')
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):
    parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=PopulationOracle(args.eboot,args.libc,native_mobs=args.native_mobs)
cases=[]
for seed in [0,1,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for cx,cz in [(0,0),(-27,26),(159,-160),(-2147483648,2147483647)]:
        for mode in ['default','structures_off','village','lakes_off','no_features','all_flags_off','desert','desert_hills','mutated_desert','forced_lakes','low_sea','no_dungeons','alternating']:
            cases.append(oracle.populate(seed,cx,cz,mode))
    print('Original population seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoint':'0x508bb0','input_kind':'Controlled feature/world callbacks; every structure/lake/dungeon/decorator/mob callback consumes one original nextLong. Not final world output.',
      'cases':cases}
if args.native_mobs:
    data['entrypoints']=['0x508bb0','0x4a6450']
    data['input_kind']='Original population dispatcher and unmodified mob helper 0x4a6450 (single RET, no RNG draws). Other structure/lake/dungeon/decorator callbacks consume one supplied original nextLong; freeze/snow/world effects are controlled. Not final world output.'
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/('ps4-population-native-mobs.json.gz' if args.native_mobs else 'ps4-population.json.gz')
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
