#!/usr/bin/env python3
"""Capture original natural pumpkins, their default properties and mayPlace."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_pumpkins import PumpkinOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
registry=Path(__file__).with_name('block-registry-native.json')
if hashlib.sha256(registry.read_bytes()).hexdigest()!='9d0f14e3fecb4bb735d7faaa7f36d3ebcba2921b64e30db34531ac5a7813082b':parser.error('Unexpected recovered block bindings')
oracle=PumpkinOracle(args.eboot,args.libc);rules=[];cases=[]
for current in [0,1,8,9,10,11,20,31|(1<<16),37,38,106,175]:
    for below in [0,1,2,3,18,20,60,257]:
        for support in [False,True]:
            for nonreplaceable_air in [False,True]:rules.append(oracle.rule(current,below,support,nonreplaceable_air))
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','dirt','podzol','farmland','stone','water','replaceable','leaves','protected','unsupported','non_replaceable_air']:
        for position in [[0,64,0],[-2493,1,2501],[5,0,-7],[-9,127,11],[17,254,-23],[3,256,7]]:cases.append(oracle.pumpkins(seed,position,profile))
    print('Original pumpkin seed',seed,'rules',len(rules),'placements',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x1722d0','0x172300','0x58e9f0','0x590450','0x5903a0','0x58ff60'],
      'input_kind':'Original pumpkin-configured BushFeature, PumpkinBlock final default assignments, property-list constructor and mayPlace. Block base construction, StateDefinition storage, canonical property storage, voxel contexts and face-support virtual results are controlled; not full block registry or world-save captures.',
      'target_id':oracle.pumpkin_id,'properties':oracle.property_inputs,'defaults':oracle.default_cases,'rules':rules,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-pumpkins.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(rules),len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
