#!/usr/bin/env python3
"""Capture native block material selections/stores and state linkage."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from read_block_constructors import recover
from native_block_materials import BlockMaterialOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ['x86_64','AMD64']:parser.error('Linux x86-64 and a C compiler are required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
    if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
flow=recover(a.eboot);contexts=[]
for ordinal in [0,7,15]:
    n=BlockMaterialOracle(a.eboot,a.libc,flow,ordinal);rows=n.registrations()
    if [(r['id'],r['material_alias']) for r in rows]!=[(r['id'],r['material_alias']) for r in flow['registrations']]:raise ValueError('Static material origins and original CPU constructor outputs disagree')
    contexts.append({'supplied_color_ordinal':ordinal,'blocks':rows})
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x957a0 (static only)','0x91fb0','0x92150','0xd06a0','0xd0670'],
 'input_kind':'345 registration-to-allocation/constructor associations (including air), independently checked against the 344 named registration extractor. Symbolic constructor prefixes recover material argument origins through 213 constructor functions; only the two original base material stores occur in those bodies. Native CPU prefixes independently select material under three supplied enum/color contexts, stop at the base call using a C setjmp boundary, then execute that complete original base constructor and both original BlockState accessors. Host allocators, original libc string routines, synthetic unused/enum/color arguments and prototype state storage are supplied; material initialization/traits and prefix selection/store results are native outputs. Original module startup, root registry execution, property/default/shape construction and whole world behavior are outside this comparison.',
 'constructor_functions':flow['constructor_functions'],'nonself_helpers':flow['nonself_helpers'],'material_stores':flow['material_stores'],'primary_vtables':flow['primary_vtables'],'setter_targets':flow['setter_targets'],'contexts':contexts}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-block-materials.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(contexts),sum(len(c['blocks']) for c in contexts),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
