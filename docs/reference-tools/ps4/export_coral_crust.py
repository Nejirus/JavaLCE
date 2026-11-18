#!/usr/bin/env python3
"""Capture original crust dispatch, offset helpers and value-copy rotations."""
import argparse,gzip,hashlib,itertools,json,platform
from pathlib import Path
from native_coral_crust import CoralCrustOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
    if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=CoralCrustOracle(a.eboot,a.libc,a.assets);cases=[]
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
profiles=['crust_floor','crust_shallow','crust_deep','crust_steps','crust_live','crust_dead','crust_air']
positions=[[0,45,0],[-2493,1,2501],[13,53,-17],[-9,54,11],[0,60,0],[2147483000,45,-2147483001]]
orders=[(0,1,2,3),(3,2,1,0),(1,0,3,2),(2,3,0,1)]
for seed in seeds:
    for profile in profiles:
        for position in positions:
            for order in orders:
                for geometry,post in [(0,0),(1,1),(3,3)]:cases.append(n.crust(seed,position,profile,order,geometry,post))
    for order in itertools.permutations(range(4)):cases.append(n.crust(seed,[0,45,0],'crust_floor',order))
    for position in positions:cases.append(n.crust(seed,position,'crust_floor',client=True))
    print('Original coral crust seed',seed,'cases',len(cases),flush=True)
for seed in range(64):cases.append(n.crust(seed,[0,45,0],'crust_floor'))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
      'entrypoints':['0x1c7650','0x1c84b0','0x1c8650','0x5c2450','0x6c1d60','0x6c2ec0','0x6c2ed0','0x6c5060','0x6c2390'],
      'input_kind':'Original crust dispatcher, offsets, original by-value Random rotation (population RNG unchanged), settings constructor and template-position RNG path. Rotation objects/order, registry/template IDs, canonical/material/solid-blocking inputs and template placement/postprocess callbacks are supplied. Callbacks consume 0/1/3 original nextLong draws from separate template and population streams. Template bodies, markers, world changes and notifications are not captured. No full geometry or whole-world claim.',
      'offsets':n.offset_inputs,'templates':n.template_inputs,'crust_ids':n.crust_ids,'outcropping_ids':n.outcrop_ids,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-coral-crust.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['events']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
