#!/usr/bin/env python3
"""Capture original frozen/warm ocean and ice-biome wrappers."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_ocean_wrappers import OceanWrapperOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=OceanWrapperOracle(args.eboot,args.libc);cases=[]
origins=[[0,0,0],[-2493,0,2501],[12,64,-13],[-33,-64,129]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for kind in oracle.WRAPPERS:
        for variant in [0,1]:
            for origin in origins:
                for height in ['zero','one','flat','sloped']:
                    for outcome in ['always','never','mixed']:
                        for pickles in ([-2,0,1,4,7] if kind=='WARM_OCEAN' else [0]):cases.append(oracle.decorate(kind,variant,seed,origin,height,outcome,pickles))
    print('Original ocean/ice wrapper seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x4d74f0','0x4fb580','0x373b20'],
      'input_kind':'Original complete frozen/warm ocean and ice-biome wrappers. Variant flag, pickle attempt count, nonnegative world heights, base decoration and feature geometry/outcome callbacks are controlled; every feature/base callback consumes one original nextLong. This proves wrapper call/coordinate/RNG flow, not full shared population geometry or world output.',
      'feature_bindings':oracle.feature_bindings,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-ocean-wrappers.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
