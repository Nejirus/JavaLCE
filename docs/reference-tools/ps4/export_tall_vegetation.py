#!/usr/bin/env python3
"""Capture original bamboo/podzol/berries and their original placement rules."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_tall_vegetation import TallVegetationOracle

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=TallVegetationOracle(args.eboot,args.libc);rules=[];cases=[]
for kind in ['bamboo','berries']:
    for current in [0,1,8,9,10,11,20,31|(1<<16),37,106,175,319|(8<<16),320,363]:
        for below in [0,1,2,3,3|(1<<16),3|(2<<16),8,9,10,11,12,13,18,31,60,110,175,319,320,321,363]:rules.append(oracle.rule(kind,current,below))
positions=[[0,64,0],[-2493,1,2501],[5,0,-7],[-9,127,11],[17,254,-23],[3,256,7],[30000000,68,-30000000]]
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for kind in ['bamboo','podzol','berries']:
        for profile in ['grass','dirt','podzol','farmland','stone','water','sand','mixed_soil','replaceable','empty','leaves']:
            for position in positions:cases.append(oracle.vegetation(kind,seed,position,profile))
    for roof in [0,1,2,3,4,5,8,16,17,18]:
        for position in positions:cases.append(oracle.vegetation('bamboo',seed,position,'roof',roof))
    print('Original vegetation seed',seed,'rules',len(rules),'kernels',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x40e40','0xbffe0','0x3fb40','0x3e8c0','0x3ea00','0x40250','0x5548a0','0x172300','0x6cb170','0x6cb360','0x6cc260','0x6cc380','0x37a180'],
      'input_kind':'Original kernels, block property-list/final default assignments, placement rules, hash and berry property-range constructor. Block base/StateDefinition/canonical storage, registry lookup and material identities are controlled. Voxel writes succeed including out-of-height writes; not original whole-world or save captures.',
      'berry_age_count':oracle.berry_age_count,'property_counts':{str(k):len(v) for k,v in oracle.definitions.items()},'default_state_cases':oracle.default_cases,'data_cases':oracle.data_cases,'rules':rules,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-tall-vegetation.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(rules),len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
