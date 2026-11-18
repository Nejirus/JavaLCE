#!/usr/bin/env python3
"""Capture original snow eligibility, neighbor placement and actual layer increments."""
import argparse,gzip,hashlib,json,platform
from pathlib import Path
from native_snow import SnowOracle,C,P,I
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--libc',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,d in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=d:parser.error(f'Unexpected mcps4 input: {p}')
n=SnowOracle(a.eboot,a.libc);cases=[];defaults=[]
for tile,block in n.blocks.items():
 state=n.read(block+0x190,'Q');mat=n.read(block+0x170,'Q');defaults.append({'tile':tile,'state':n.unpacked(state),'solid_render':n.solid_render(tile),'special_material':mat in [n.game.read(p,'Q') for p in [0x14187a0,0x14188e8,0x14188f0]],'blocks_motion':n.read(mat+8,'I')==1,'liquid':n.read(mat+8,'I')==2})
for current in [0,78,78|(6<<16),78|(7<<16),80,9]:
 for below in [0,1,18,20,79,174,208,78,78|(6<<16),80,212]:
  for cap in [0.,.25,1.]:
   for check in [False,True]:
    for accumulate in [False,True]:
     for bits in [0,0x3e19999a,0x3e19999b,0x7fc00000]:cases.append(n.snow('rule',[0,64,0],[[0,64,0,current],[0,63,0,below]],temperature_bits=bits,cap=cap,check_height=check,accumulate=accumulate))
for y in [-1,0,255,256]:
 for light in [0,11,12]:
  for check in [False,True]:cases.append(n.snow('rule',[-2493,y,2501],[[-2493,y-1,2501,1]],light=light,check_height=check))
for current in [0,78,78|(1<<16),78|(6<<16),78|(7<<16)]:
 for profile in ['flat','no_north','no_north_south','blocked','snow','snow_block']:
  blocks=[[15,64,-17,current]]+[[15+dx,63,-17+dz,1] for dx,dz in [(0,0),(0,-1),(0,1),(-1,0),(1,0)]]
  for i,(dx,dz) in enumerate([(0,-1),(0,1),(-1,0),(1,0)]):
   tile=1 if profile=='blocked' or profile=='no_north' and i==0 or profile=='no_north_south' and i<2 else 78|(i<<16) if profile=='snow' else 80 if profile=='snow_block' else 0
   blocks.append([15+dx,64,-17+dz,tile])
  for cap in [0.,.25,1.]:
   for spread in [False,True]:
    for check in [False,True]:
     for chunk in [False,True]:
      for height in [63,64,65]:cases.append(n.snow('full',[15,64,-17],blocks,cap=cap,check_height=check,accumulate=spread,chunk_present=chunk,cached_height=height))
for meta in range(8):
 for accumulate in [False,True]:
  blocks=[[0,64,0,78|(meta<<16)],[0,63,0,1]]
  cases.append(n.snow('state',[0,64,0],blocks,accumulate=accumulate));cases.append(n.snow('full',[0,64,0],blocks,accumulate=accumulate,write_mode='none'));cases.append(n.snow('full',[0,64,0],blocks,accumulate=accumulate,enabled=False))
# Explicit alternate default-state storage exercises guards that the ordinary
# constructor's layer-one default does not reach; these are not real save claims.
for default_meta in [6,7]:
 for current in [0,78]:
  for cap in [0.,.25,1.]:
   for accumulate in [False,True]:
    cases.append(n.snow('rule',[0,64,0],[[0,64,0,current],[0,63,0,78]],cap=cap,accumulate=accumulate,default_snow_meta=default_meta))
# Full snow placement also executes the original uncached height scan.
for current in [0,78,78|(6<<16),78|(7<<16)]:
 blocks=[[15,64,-17,current]]+[[15+dx,63,-17+dz,1] for dx,dz in [(0,0),(0,-1),(0,1),(-1,0),(1,0)]]
 for mask in [0,31,65535]:
  for cap in [0.,.25]:
   for write_mode in ['all','none']:cases.append(n.snow('full',[15,64,-17],blocks,cap=cap,cached_height=255,section_mask=mask,write_mode=write_mode))
maximum_depth_cases=[]
for bits in [0,1,0x007fffff,0x00800000,0x80000000,0x3dffffff,0x3e000000,0x3e800000,0x3f800000,0x4d7fffff,0x4d800000,0xcd800000,0xcd800001,0x7f7fffff,0xff7fffff,0x7f800000,0xff800000,0x7fc00000,0xffc00000]:
 n.w(n.biome+0x1bc,'I',bits);maximum_depth_cases.append({'bits':f'{bits:08x}','result':n.call(0x852c0,I,[P],[n.biome])})
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],
 'entrypoints':['0x3fe7a0','0x3fede0','0x6386a0','0x638810','0x6394a0','0x3f2e90','0x7a4d0','0x852c0','0x3f7870','0x3f7da0'],
 'input_kind':'Original snow eligibility with non-null depth output, full snow placer, layer increments, default assignment/getData, block/default-state lookups and cached/uncached height paths execute. Canonical state/property storage (including explicit alternate default-snow values in guard cases), base block/StateDefinition initialization, default materials/support classifications, biome temperature/maximum depth, game-rule boolean, supplied primary voxels, air-only secondary layer, chunk presence/cache bytes, section occupancy and write outcomes are inputs. The TLS/lock-wrapped section accessor is supplied as an emptiness result. Original default-state getter is restored from unmodified bytes. No complete block/material/biome registry, compressed chunk palette decoding, downstream physics or whole-world parity capture.','maximum_depth_cases':maximum_depth_cases,'snow_default':n.snow_default,'snow_data':n.snow_data,'default_inputs':defaults,'cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-snow.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['writes']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
