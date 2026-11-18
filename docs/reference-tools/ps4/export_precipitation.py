#!/usr/bin/env python3
"""Capture original precipitation-height scans, cache writes and repeated queries."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_precipitation import PrecipitationOracle
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=PrecipitationOracle(a.eboot,a.libc);cases=[]
profiles=[([64,1],[-1,0]),([40,2],[120,1]),([16,1],[75,2]),([-1,0],[-1,0]),([-1,0],[128,1]),([254,1],[-1,0]),([255,1],[-1,0]),([254,3],[255,2])]
for mask in [0]+[1<<i for i in range(1,16)]+[65535,0x5555,0xaaaa]:
 for cached in [0,64,254,255]:
  for primary,secondary in profiles:
   for pos in [[-1,64,-17],[-2493,-1,2501],[2147483647,255,-2147483648],[0,-2147483648,0]]:cases.append(n.heights(pos,cached,mask,primary,secondary))
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'entrypoints':['0x3f7870','0x3f7da0'],
 'input_kind':'Original chunk precipitation-height scan, highest-section selector and byte cache execute twice per case. Section occupancy and primary/secondary material flags are explicit synthetic storage inputs. The TLS/lock-wrapped compressed-section accessor 0x223f10 is replaced by its supplied empty result; neither platform TLS/locks nor compressed palette decoding execute. Materials use all four blocks-motion/liquid combinations, not a claimed block registry. Cache index masks, mutation hashes, repeated reads, height edges and query/classifier order are compared. Not complete original chunk storage, height-cache invalidation, downstream physics or whole-world generation.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-precipitation.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['events']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
