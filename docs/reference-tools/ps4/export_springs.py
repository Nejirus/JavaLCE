#!/usr/bin/env python3
"""Capture original spring placement and immediate-tick calls; fluid tick supplied."""
import argparse,gzip,hashlib,itertools,json,platform
from pathlib import Path
from native_springs import SpringOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=SpringOracle(args.eboot,args.libc);cases=[]
positions=[[0,64,0],[-2493,1,2501],[5,0,-7],[-9,127,11],[17,254,-23],[3,256,7]]
contexts=[[1,1,0],[1,1,1|(1<<16)],[0,1,0],[1,3,0],[1,1,3],[3,1,1]]
for si,seed in enumerate([0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]):
    for target in [8,10]:
        for ci,(above,below,current) in enumerate(contexts):
            for pi,sides in enumerate(itertools.product([0,1,3],repeat=4)):
                cases.append(oracle.spring(seed,positions[(pi+si)%len(positions)],above,below,current,list(sides),target,tick_draws=[0,1,3][(ci+pi+si)%3]))
        for position in positions:
            for reject in [False,True]:cases.append(oracle.spring(seed,position,1,1,0,[1,1,1,0],target,True,reject,3))
    print('Original spring seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x672c40','0x3fe550'],
      'input_kind':'Original spring placement and immediate-tick dispatch. Voxel/default-state/protection inputs and thread-local flag storage are controlled. The fluid block tick is a callback consuming the specified number of original nextLong values; fluid propagation is not validated by these captures.',
      'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-springs.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
