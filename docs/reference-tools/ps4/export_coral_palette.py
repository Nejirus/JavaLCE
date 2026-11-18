#!/usr/bin/env python3
"""Capture native clock initialization and shared dead_coral palette draws."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_coral_palette import CoralPaletteOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=CoralPaletteOracle(a.eboot,a.libc);ticks=[0,1,-1,123456789000000,-123456789000000,-9223372036854775808,9223372036854775807]
for bit in [16,31,32,47,48,49,62]:
 for sign in [-1,1]:
  for delta in [-1,0,1]:ticks.append(sign*(1<<bit)+delta)
cases=[]
for tick in ticks:
 for prefix in [[],['double'],[5,4,3,2,1],['double',1073741825,'double',5]]:
  for count in [0,1,11,32]:cases.append(n.palette(tick,prefix,count))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x596c00','0xaf1940','0x3b0930','0x3c0e70','0x1c73d0','0x1c7240'],
 'input_kind':'Original default Random constructor, shared Mth draws and CoralBlock dead_coral parser/getData. Explicit clock-call return values, UTF-16 text and canonical property storage are supplied; no OS clock, game/module initialization or complete NBT/name/property loader executes. Module initializer 0x3da0f1 constructs Mth Random at 0x1418180. Original template loader 0x6bdc00 calls NBT state reader 0xc97ca0, whose 0xc98183 virtual parser call resolves to 0x1c73d0 for coral blocks. No world-seed initialization is substituted.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-coral-palette.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['states']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
