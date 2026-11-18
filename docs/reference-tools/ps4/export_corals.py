#!/usr/bin/env python3
"""Capture original hanging/floor coral kernels, rules, defaults and reuse."""
import argparse,gzip,hashlib,itertools,json,platform
from pathlib import Path
from native_corals import CoralOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
n=CoralOracle(args.eboot,args.libc);rules=[];cases=[];sequences=[]
ids=sorted({r['id'] for r in json.loads(Path(__file__).with_name('block-registry-native.json').read_text())['registrations']}|{0})
for tile in ids:
    for kind in ['plant','fan']:
        for meta in [0,8]:
            for face in [False,True]:rules.append(n.coral_rule(kind,tile|(meta<<16),1,9,face,'all'))
for below in [0,1,44,44|(8<<16),126|(8<<16),312|(8<<16)]:
    for above in [0,1,9,20]:
        for effective in [0,1,8,9]+[9|(m<<16) for m in [1,2,7,8,9,15]]:
            for face in [False,True]:
                for placement in ['all','none']:rules.append(n.coral_rule('plant',below,above,effective,face,placement))
print('Original coral rules',len(rules),flush=True)
seeds=[0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]
positions=[[0,55,0],[-2493,49,2501],[3,48,-7],[5,59,11],[-17,63,-23],[1,0,-1],[11,256,-13]]
profiles=['air','anemone_stone','anemone_live','anemone_dead','anemone_dirt','hang_wet','hang_north','hang_east','hang_checker','hang_dry','hang_live','hang_dead','hang_dirt','steps','flowing','waterlogged']
for seed in seeds:
    for kind in ['anemone','hang','fan','side_fan']:
        for profile in profiles:
            for position in positions:cases.append(n.coral(kind,seed,position,profile))
        for profile in ['anemone_stone','hang_north']:
            for position in positions[::2]:
                for placement in ['none','checker']:cases.append(n.coral(kind,seed,position,profile,placement))
                for loaded in ['none','checker']:cases.append(n.coral(kind,seed,position,profile,loaded=loaded))
        if kind in ['anemone','hang']:
            for position in positions:cases.append(n.coral(kind,seed,position,'hang_wet',client=True))
    for plane in itertools.permutations(n.HORIZONTAL):
        for profile in ['hang_north','hang_east','hang_dry']:cases.append(n.coral('hang',seed,[0,55,0],profile,plane=plane))
    for direction in range(6):
        for profile in ['hang_wet','hang_live','hang_dead','hang_dirt']:cases.append(n.coral('side_fan',seed,[3,55,-7],profile,direction=direction))
    print('Original coral seed',seed,'kernels',len(cases),flush=True)
steps=[('hang',p,[0,55,0]) for p in ['hang_dry','hang_north','hang_east','hang_wet','hang_checker']]+[('well','well_sand',[-19,64,23])]
for seed in range(64):sequences.append(n.sequence(seed,steps*2))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x5f4f30','0x1d1a50','0x1cc550','0x1cc720','0x1cf0f0','0x1cf2c0','0x200f70','0x1c6420','0x1c65c0','0x1390c0','0x1c9e90','0x1c62b0','0x1c6c50','0x1c9b90','0x1cb1f0','0x1c6920','0x1c7240','0x1ca3e0','0x1cb930','0x1fa530','0x1fb400','0x3eed30','0x37a180','0xdabb0'],
      'input_kind':'Original hanging coral/anemone and top/side-fan kernels, support rules, property constructors, default assignments, definitions and metadata getters. Explicit canonical property/StateDefinition/base-block storage, materials, solid-blocking/slab/face support, effective-fluid layers, collision placement, loaded chunks, client flag and successful writes. Stateful native Direction::Plane permutations and shared-RNG hanging-coral/desert-well reuse sequences execute. Main coral shapes/crust and world notifications are not captured here. No whole-world/save claim.',
      'properties':n.coral_property_inputs,'default_state_cases':n.default_cases,'definition_inputs':n.definition_inputs,'data_cases':n.data_cases,'rules':rules,'cases':cases,'sequences':sequences}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-corals.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(rules),len(cases),len(sequences),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
