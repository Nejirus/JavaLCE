#!/usr/bin/env python3
"""Capture original initialized material traits and all core flag-setter orders."""
import argparse,gzip,hashlib,itertools,json,platform
from pathlib import Path
from native_materials import MaterialOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=MaterialOracle(a.eboot,a.libc);cases=[]
for name in n.types:
 for flags in range(16):
  for push in [0,1,2]:cases.append(n.sample(name,flags,push,[]))
  for order in itertools.permutations(n.SETTERS):cases.append(n.sample(name,flags,0,order))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x44c270','0x44cd50','0x44cc20','0x44cc80','0x44cc90','0x44cca0','0x44ccc0','0x44ccf0'],
 'input_kind':'Complete original isolated Material and MaterialColor initializers execute with host allocator bindings. Initialized aliases, color words, defined flags, class dispatch and core virtuals are outputs, not supplied material classifications. Separate synthetic field combinations and every order of four original setters verify core virtuals and mutation/return semantics. No grouped game startup, Java material algorithm, block-to-material assignment, voxel world or physics runs. Padding and slot 0x80 reading an unwritten byte at +0x22 are outside this defined-field comparison. Color words remain original numeric values, with no guessed RGB conversion.','materials':n.initialized(),'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-materials.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(data['materials']),len(n.types),len(cases),sum(len(c['steps']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
