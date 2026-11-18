#!/usr/bin/env python3
"""Capture original huge-mushroom and melon block geometry and RNG."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_mushrooms import MushroomOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=MushroomOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','mycelium','stone','blocked','trunk_obstacle','leaves','glass','replaceable']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,244,11],[17,254,-23]]:
            for forced in [0,99,100]:
                for updates in [False,True]:cases.append(oracle.geometry('huge',seed,position,profile,forced,updates))
            cases.append(oracle.geometry('melon',seed,position,profile))
    print('Original mushroom/melon seed',seed,'cases',len(cases),flush=True)
for seed in range(32):
    for forced in [0,99,100]:cases.append(oracle.geometry('huge',seed,[0,64,0],'grass',forced))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x370120','0x4642c0','0x94050','0x2a4960'],
      'input_kind':'Original kernels, material/replaceability rules and feature write helper with controlled voxels/materials/solid-render classification. Enum IDs read from original initializer instructions; property storage supplied. Not full world saves.',
      'huge_enum_inputs':oracle.enum_inputs,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-mushrooms-melons.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
