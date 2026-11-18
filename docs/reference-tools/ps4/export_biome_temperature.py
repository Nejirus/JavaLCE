#!/usr/bin/env python3
"""Capture every registered biome's original positional temperature virtual."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_biome_temperature import BiomeTemperatureOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=BiomeTemperatureOracle(a.eboot,a.libc);cases=[]
for biome in n.biome_inputs:
 for x,z in [(0,0),(10,-20),(-2493,2501),(-27,26),(30000000,-30000000),(2147483647,-2147483648),(16777215,-16777216),(16777217,-16777218),(-1000000000,999999999)]:
  for y in [-2147483648,-1,0,63,64,65,66,127,255,256,2147483647]:cases.append(n.temperature(biome['id'],[x,y,z]))
for id in [48,49]:
 for x in range(-128,129,16):
  for z in range(-128,129,16):
   for y in [64,65,127,254,255]:cases.append(n.temperature(id,[x,y,z]))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x7f900','0x85390','0x4d7380','0x52a300','0x52a4a0'],'shared_noise_inputs':n.shared_noise_inputs,'simplex_numeric_initializers':n.simplex_numeric_initializers,
 'input_kind':'Original complete biome constructors and actual temperature virtuals/noise execute; base temperatures, dispatch entries and frozen-ocean temperature noise are outputs of original constructors. Shared temperature/wetland noise uses original constructors with fixed seeds/counts read from original argument instructions. Simplex skew constants are initialized from the original MOVABS immediate bits. Grouped startup does not execute. Existing initializer harness supplies registry/default/property/name-map storage and nine feature constructor/deletion bindings. No Java temperature/noise runs in the exporter. World biome lookup, full registry startup and whole-world generation are outside this numeric comparison.','biomes':n.biome_inputs,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-biome-temperature.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
