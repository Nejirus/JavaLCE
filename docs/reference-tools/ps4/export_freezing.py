#!/usr/bin/env python3
"""Capture the native final-population freezing rule and query order."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_freezing import FreezingOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=FreezingOracle(a.eboot,a.libc);cases=[]
for tile in [8,9]:
 for meta in range(16):
  for bits in [0xff800000,0x80000000,0,0x3e199999,0x3e19999a,0x3e19999b,0x3f800000,0x7f800000,0x7fc00000,0xffc00000]:
   for y in [-2147483648,-1,0,63,255,256,2147483647]:
    for light in [-1,0,9,10,15]:cases.append(n.freeze([-2493,y,2501],tile|(meta<<16),bits,light))
ids=sorted({0}|{r['id'] for r in json.loads(Path(__file__).with_name('block-registry-native.json').read_text())['registrations']})
for tile in ids:
 for meta in [0,15]:
  for bits in [0,0x3e19999a,0x3e19999b]:
   for light in [0,10]:cases.append(n.freeze([0,64,0],tile|(meta<<16),bits,light))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x3dcbc0'],'temperature_threshold_bits':f'{n.game.read(0xeaba30,"I"):08x}',
 'input_kind':'Original final-population freezing rule executes with controlled biome/temperature, block-light and canonical primary state/property storage. Float boundary/NaN cases, unsigned height gate, query order, block identities and all water levels are compared. No Java rule runs in the exporter. Biome temperature computation, world effects and whole-world generation are outside this comparison.','registered_ids':ids,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-freezing.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(c['result'] for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
