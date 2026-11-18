#!/usr/bin/env python3
"""Capture original biome tree choices, constructed fields and placed geometry."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_tree_selection import TreeSelectionOracle

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
oracle=TreeSelectionOracle(args.eboot,args.libc)
modes=[('DEFAULT',0),('HILLS',0),*[('FOREST',v) for v in range(4)],('MUTATED_FOREST',0),('JUNGLE',0),('JUNGLE',1),
       ('BAMBOO_JUNGLE',0),('SAVANNA',0),('PLAINS',0),('SWAMP',0),*[('TAIGA',v) for v in range(3)]]
cases=[];geometry=[]
for kind,variant in modes:
    for seed in [*range(512),-1,-987654321,-9223372036854775808,9223372036854775807]:cases.append(oracle.selection(kind,variant,seed))
    for seed in [0,1,2,3,4,5,6,7,8,9,82958734,-1]:
        for profile in ['grass','high_obstacle','leaves']:
            for position in [[0,64,0],[-2493,119,2501]]:geometry.append(oracle.selection(kind,variant,seed,position,profile))
    print('Original tree selector',kind,variant,'cases',len(cases),'placements',len(geometry),flush=True)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7',
      'eboot_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],
      'entrypoints':[hex(v) for v in oracle.SELECTORS.values()]+[hex(p) for p,g in oracle.STATE_INITIALIZERS]+['0x53d910','0x503330','0x502590','0x4baa60','0x4b9e40'],
      'input_kind':'Original selectors and full selected tree geometry/lifecycles. Species enums, cached states, final log/leaf default assignments and feature constructors execute. Base block construction, StateDefinition storage/factories, materials and voxel contexts are controlled; not complete block-registry or world-save captures.',
      'wood_types':oracle.wood_inputs,'default_state_cases':oracle.default_cases,'cached_states':oracle.initialized_states,'cases':cases,'geometry':geometry}
args.output_dir.mkdir(parents=True,exist_ok=True)
path=args.output_dir/'ps4-tree-selection.json.gz'
path.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0))
print(path.name,len(cases),len(geometry),hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
