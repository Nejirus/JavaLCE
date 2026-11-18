#!/usr/bin/env python3
"""Capture original coral shape helpers, combined dispatch and full feature."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_coral_shapes import CoralShapeOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
    if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=CoralShapeOracle(a.eboot,a.libc);cases=[]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
profiles=['shape_floor','shape_matching','shape_foreign','shape_dead','shape_air','shape_obstacle','shape_unsupported','shape_terrain']
positions=[[0,55,0],[-2493,51,2501],[7,1,-11],[-9,254,17]]
for seed in seeds:
    for kind in ['cross','diagonal']:
        for profile in profiles:
            for position in positions[::2]:
                for radius in [0,1,3]:
                    for increasing in [False,True]:
                        for chance in [0.,.1,.5,1.]:cases.append(n.shape(kind,seed,position,profile,state=259|(2<<16),radius=radius,increasing=increasing,chance=chance))
    for kind in ['shape0','shape1','rare','patch']:
        for profile in profiles:
            for position in positions:
                for meta in [0,2,4,5,10]:cases.append(n.shape(kind,seed,position,profile,state=259|(meta<<16)))
        for placement in ['none','checker']:
            for position in positions:cases.append(n.shape(kind,seed,position,'shape_floor',placement=placement))
    for direction in [2,3,4,5]:
        for short in [False,True]:
            for profile in profiles:
                for meta in [0,4,10]:cases.append(n.shape('branch',seed,[0,55,0],profile,state=259|(meta<<16),direction=direction,short=short))
    for kind in ['dispatch','full']:
        for profile in profiles:
            for position in positions:
                for placement in ['all','none','checker']:cases.append(n.shape(kind,seed,position,profile,placement=placement))
    for y in [-1,0,48,49,58,59,60,255,256]:
        for profile in ['anemone_stone','anemone_live','anemone_dead','air']:cases.append(n.shape('full',seed,[0,y,0],profile))
        cases.append(n.shape('full',seed,[0,y,0],'anemone_stone',client=True))
    print('Original coral shapes seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
      'entrypoints':['0x1cc130','0x1cc3d0','0x1cc8e0','0x1ccf80','0x1cd950','0x1ce3d0','0x1cf440','0x1cf6f0','0x1cfe00','0x1d04c0','0x1cc550','0x1cc720'],
      'input_kind':'Original complete coral shape helpers, vector append order, fallthrough dispatcher and full feature including floor/side fans. Native property/default/direction storage and explicit world/material/solid-blocking/support/collision/loaded-chunk boundaries from native_corals; writes succeed. No Java feature geometry runs in the exporter. No crust/templates, original full registry/world notifications or whole-world/save claim.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-coral-shapes.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['writes']) for c in cases),sum(c['read_count'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
