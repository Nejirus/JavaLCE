#!/usr/bin/env python3
"""Capture original standalone vines and original attachment/exception rules."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_vines import VineOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
registry=Path(__file__).with_name('block-registry-native.json')
if hashlib.sha256(registry.read_bytes()).hexdigest()!='9d0f14e3fecb4bb735d7faaa7f36d3ebcba2921b64e30db34531ac5a7813082b':parser.error('Unexpected recovered block bindings')
oracle=VineOracle(args.eboot,args.libc);rules=[];cases=[]
for tile in sorted(oracle.ids.values()):
    for above in [0,9,106,50,1,20,260,274]:
        for direction in [0,1,2,3,4,5]:
            for side,top in [(0,0),(0,1),(1,0)]:rules.append(oracle.rule(tile,above,direction,side,top))
for seed in [0,1,9,82958734,-1,-987654321,-9223372036854775808,9223372036854775807]:
    for profile in ['grass','stone_wall','leaf_wall','glass_wall','trapdoor_wall','shulker_wall','mixed_walls','old_vines']:
        for position in [[0,64,0],[-2493,0,2501],[5,120,-7],[-9,127,11],[17,128,-23],[3,250,7]]:cases.append(oracle.vines(seed,position,profile))
    print('Original vines seed',seed,'rules',len(rules),'placements',len(cases),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':['0x775260','0x772800','0x7729e0'],'registry_bindings_sha256':hashlib.sha256(registry.read_bytes()).hexdigest(),
      'input_kind':'Original full vine kernel and attachment/exception routines. Original libc __dynamic_cast uses original Block/ShulkerBox RTTI and recovered registration/alias identities. Other block identities, voxel contexts, face-shape virtual results and canonical property storage are controlled. Not full registry/face-shape initialization or whole-world captures.',
      'exceptions':oracle.exception_inputs,'rules':rules,'cases':cases}
args.output_dir.mkdir(parents=True,exist_ok=True);path=args.output_dir/'ps4-vines.json.gz';path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(path.name,len(rules),len(cases),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
