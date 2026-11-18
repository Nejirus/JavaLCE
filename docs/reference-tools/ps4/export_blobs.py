#!/usr/bin/env python3
"""Capture original taiga boulder kernels and position-iterator ordering."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_blobs import BlobOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=BlobOracle(args.eboot,args.libc);cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','stone','empty','sand','leaves','water']:
        for position in [[0,64,0],[-2493,1,2501],[5,3,-7],[-9,4,11],[17,127,-23],[3,255,7]]:
            for radius in [-1,0,1,2,3]:
                for state in [48,1|(5<<16)]:cases.append(oracle.blob(seed,position,profile,radius,state))
    print('Original boulder seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0xb85d0','0xc0700','0x745170'],'input_kind':'Original complete BlockBlobFeature and block-position iterator with controlled voxel APIs, default target states and radius inputs. Not complete biome/whole-world captures.','cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-blobs.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
