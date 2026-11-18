#!/usr/bin/env python3
"""Capture original blue ice, ice patches and ice spike kernels."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_ice import IceOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=IceOracle(args.eboot,args.libc);cases=[]
positions=[[0,64,0],[-2493,1,2501],[5,2,-7],[-9,51,11],[17,253,-23],[3,256,7]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for kind in ['patch','spike']:
        for position in positions:
            for profile in ['snow','snow_only','dirt','grass','ice','stone','snow_layers','water','air','overhang','root_holes','mixed','buried']:
                for updates in [False,True]:cases.append(oracle.ice(kind,seed,position,profile,updates=updates))
    for position in [[0,62,0],[-2493,1,2501],[5,63,-7],[-9,64,11],[17,-1,-23]]:
        for profile in ['air','flowing','ice_below','ice_above','ice_side','blue_only','current_air','mixed','crowded']:cases.append(oracle.ice('blue',seed,position,profile))
    for size in [3,4,5,7,8]:
        for state in [79,174,273]:cases.append(oracle.ice('patch',seed,[0,64,0],'mixed',size=size,state=state))
    print('Original ice seed',seed,'kernels',len(cases),flush=True)
for seed in range(256):cases.append(oracle.ice('spike',seed,[0,90,0],'snow_only'))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0xd1300','0x374370','0x3745d0','0x3df280','0x2a4960'],
      'input_kind':'Original blue-ice/ice-patch/ice-spike kernels and shared Feature write helper. Direction-vector order/storage, block defaults/canonical storage/material identities, patch size/target, sea level, voxel profiles and successful writes are supplied. Original random choices, float geometry and ordered probes/writes execute; not original downstream notifications, full registry or whole-world/save captures.',
      'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-ice.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
