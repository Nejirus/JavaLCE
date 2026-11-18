#!/usr/bin/env python3
"""Capture original precipitation heights with every registered block material."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from read_block_constructors import recover
from native_block_heights import BlockHeightOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 and a C compiler are required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
flow=recover(a.eboot);n=BlockHeightOracle(a.eboot,a.libc,flow);cases=[]
for row in flow['registrations']:
    block=row['id']
    for top in [-1,0,63,254,255]:
        for secondary in [False,True]:cases.append(n.heights([-2493,2147483647,2501],255,65535,[-1,0] if secondary else [top,block],[top,block] if secondary else [-1,0]))
    # Mixed layers demonstrate that primary motion blocking wins above fluid,
    # while secondary solidity is ignored by this original scan.
    cases.append(n.heights([-17,-2147483648,31],255,65535,[40,block],[80,9]))
    cases.append(n.heights([-17,-2147483648,31],255,65535,[40,9],[80,block]))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x3f7870','0x3f7da0','0xd06a0','0xd0670'],
 'input_kind':'Constructor-selected native block materials and original BlockState material getters feed original precipitation-height scans twice per case. All 345 registered block identities including air occur in primary and secondary layers, extreme surface heights and mixed fluid/solid columns. No supplied solid/liquid classifiers. Column states, section occupancy, byte cache and prototype state storage are controlled; the compressed section TLS/lock wrapper is supplied, not executed. Constructor color/unused/property boundaries are as documented by export_block_materials. Ordered events are little-endian signed-int32 tuples: section=(0,y,empty,0,0), primary=(1,x,y,z,id), secondary=(2,x,y,z,id). Neither full compressed storage, state-property conversion, lighting/physics nor whole world generation is captured.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-block-heights.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(c['events'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
