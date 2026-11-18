#!/usr/bin/env python3
"""Capture original fluid ticks with every cached horizontal permutation."""
import argparse,gzip,hashlib,itertools,json,platform
from pathlib import Path
from native_liquids import LiquidOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
    if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=LiquidOracle(a.eboot,a.libc);cases=[]
for plane in itertools.permutations(n.HORIZONTAL):
    for kind in ['water','lava']:
        tile=8 if kind=='water' else 10
        for seed in [0,1,-1,82958734]:
            for level,primary,secondary in [(3,[[0,64,-1,tile],[1,64,0,tile|(2<<16)]],[]),(7,[[0,64,-1,tile],[0,65,0,tile]],[]),(1,[[0,64,-1,tile|(4<<16)],[-1,64,0,31]],[]),(3,[[0,64,0,20],[0,64,1,tile]],[[0,64,0,tile|(3<<16)]])]:
                c=n.tick(kind,seed,[0,64,0],level=level,primary=primary,secondary=secondary,drop_plants=True,horizontal_plane=plane)
                events=c.pop('events');c['event_count']=len(events);c['event_sha256']=hashlib.sha256(json.dumps(events,separators=(',',':')).encode()).hexdigest();c['effects']=[e for e in events if e[0] not in ['primary','secondary','pair','face']];cases.append(c)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x221e90','0x2215c0','0x200f70'],'input_kind':'Original fluid tick and cached horizontal Direction::Plane lookup over all 24 supplied permutations; the original hanging-coral mutator is separately captured in ps4-corals. Same controlled world/material/face/notification boundaries as ps4-liquids. Not a combined original coral-fluid-world capture.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-direction-liquids.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(c['event_count'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
