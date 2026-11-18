#!/usr/bin/env python3
"""Capture crust scatter/fan kernels and postprocessing over ordered marker inputs."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_coral_decoration import CoralDecorationOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
    if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=CoralDecorationOracle(a.eboot,a.libc);cases=[]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
profiles=['shape_floor','shape_matching','shape_foreign','shape_dead','shape_air','shape_obstacle','shape_unsupported','shape_terrain','anemone_stone','hang_wet']
positions=[[0,55,0],[-2493,51,2501],[7,1,-11]]
for seed in seeds:
    for position in positions:
        for profile in profiles:
            for color in [0,1,2,4,5]:cases.append(n.decoration('scatter',seed,position,profile,color=color))
            cases.append(n.decoration('fan',seed,position,profile))
            for direction in range(6):cases.append(n.decoration('side_fan',seed,position,profile,direction=direction))
        for profile in ['shape_floor','shape_foreign','hang_wet','anemone_stone']:
            for kind in ['scatter','fan','side_fan']:
                for placement in ['all','none','checker']:
                    for writes in ['none','checker']:cases.append(n.decoration(kind,seed,position,profile,placement=placement,write_mode=writes))
        x,y,z=position;markers=[[x+dx,y+((dx+dz)%2),z+dz] for dx in range(-1,2) for dz in range(-1,2)]
        for profile in profiles[:8]:
            for ordered in [[],markers,list(reversed(markers))]:
                for writes in ['all','none','checker']:
                    for placement in ['all','none','checker']:cases.append(n.decoration('markers',seed,position,profile,write_mode=writes,placement=placement,markers=ordered))
    print('Original crust decoration seed',seed,'cases',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
      'entrypoints':['0x1c8940','0x1c8bd0','0x1c9430','0x1c95e0','0x1c9790','0x1c9860'],
      'input_kind':'Original complete marker postprocessing and scatter/fan helpers with explicit ordered marker-position inputs, canonical/material/support/effective-fluid/collision inputs and successful/rejected voxel-write outcomes. Direction facing bytes are bound from the original initializer argument stores. Original template marker extraction/hash-map order is not simulated; it is outside this fixture. Template bodies/world notifications and whole-world capture remain outside scope.','direction_facing_inputs':n.direction_facing_inputs,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-coral-decoration.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['writes']) for c in cases),sum(c['read_count'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
