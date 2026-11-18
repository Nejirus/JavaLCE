#!/usr/bin/env python3
"""Capture original BiomeDecorator count defaults, call order and shared RNG."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_decoration import DecorationOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=DecorationOracle(args.eboot,args.libc)
cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for mode in ['default','all','disabled','negative']:
        for height in ['flat','dynamic','zero','negative','one']:
            for origin in [[0,0,0],[-2493,0,2501],[5,7,-7]]:cases.append(oracle.decorate(seed,origin,mode,height))
    print('Original biome decoration seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x86c00','0x87760'],
      'input_kind':'Original constructor count/flag stores and entire main dispatcher execute. Controlled feature constructors/virtuals, biome selectors and height helpers; each place/post/ore callback consumes one original nextLong. Selectors consume original nextInt. No geometry or complete-world parity claim.',
      'defaults':oracle.defaults,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-biome-decoration.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
