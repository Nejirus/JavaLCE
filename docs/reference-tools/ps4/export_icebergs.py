#!/usr/bin/env python3
"""Capture the complete original iceberg generator and retained feature state."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_icebergs import IcebergOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=IcebergOracle(args.eboot,args.libc);cases=[]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['ocean','empty','stone','mixed','caps']:
        for position,sea in [([0,64,0],63),([-2493,1,2501],0),([17,253,-23],255)]:cases.append(oracle.iceberg(seed,position,profile,sea))
    print('Original iceberg seed',seed,'kernels',len(cases),flush=True)
for seed in range(128):cases.append(oracle.iceberg(seed,[0,64,0]))
for i,seed in enumerate([9,0,47,1,-987654321,82958734,63,127,9,0,47,1]):cases.append(oracle.iceberg(seed,[-2493,127,2501],'caps' if i&1 else 'ocean',63,reuse=i!=0))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x372190','0x3721e0','0x3727b0','0x372a30','0x372e50','0x373230','0x3733b0'],
      'input_kind':'Original complete iceberg constructor/generator, shape/placement, smoothing, carve dispatcher/cutter and original libc double sin/cos. Canonical/property/default-state storage, material identities, direction storage, sea level, voxel profiles and successful writes are supplied. Following RNG and retained feature fields are captured. No grouped startup, downstream notifications, full original registry or whole-world/save capture.',
      'constructor_fields':oracle.constructor_fields,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-icebergs.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
