#!/usr/bin/env python3
"""Capture original PS4 surfaces and their four-carver continuation (Linux x86-64)."""
from native_surfaces import SurfaceOracle,C,P,U,I,D
from pathlib import Path
import argparse,struct,json,hashlib,gzip,platform
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',type=Path,required=True)
parser.add_argument('--libc',type=Path,required=True)
parser.add_argument('--output-dir',type=Path,required=True)
a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
expected={a.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',a.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for p,h in expected.items():
 if hashlib.sha256(p.read_bytes()).hexdigest()!=h:parser.error(f'Unexpected mcps4 file: {p}')
a.output_dir.mkdir(parents=True,exist_ok=True)
meta={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':expected[a.eboot],'libc_sha256':expected[a.libc],'initial_cached_seed':0,'input_kind':'Controlled voxel buffers, biome fields and shared surface noise inputs; not PS4 world saves'}
def save(name,cases):
 path=a.output_dir/name;raw=json.dumps({**meta,'cases':cases},separators=(',',':')).encode();path.write_bytes(gzip.compress(raw,mtime=0));print(name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
cases=[]
for seed in [0,123456789,-987654321,9223372036854775807]:
 o=SurfaceOracle(a.eboot,a.libc)
 for id in [6,134,37,38,39,165,166,167,48,49,1,2,14,16,25,3,20,34,131,162,32,33,160,161,5,30,163,164,12,13,140,42,43,44,45,26]:
  for x,z,n,h,water in [(0,0,-18,40,True),(10,-20,4,100,False),(-143,126,2,63,False),(64,-17,6,40,True),(510,255,10,55,False),(-255,-510,-1.7,128,False),(191,-35,30,62,False),(33,128,0,80,False),(24,53,3.9,127,False),(80,109,100,40,True)]:
   before=[1 if y<h else 9 if water and y<63 else 0 for y in range(128)]
   if h>=100:
    for y in [44,45,46,82,83,84,85,86,87]:before[y]=0
   after,ice_after,rand=o.column(seed,id,x,z,n,before)
   cases.append({'seed':seed,'biome':id,'x':x,'z':z,'noise':n,'random_seed':1171,'before':before,'after':after,'ice_before':before,'ice_after':ice_after,'random_after':rand,'top_tile_after':o.read(o.biome_for(id)+0x50,'I')})
 print('Native surface seed',seed,len(cases),'cases',flush=True)

save('ps4-biome-surfaces.json.gz',cases)
cases=[]
for seed,cx,cz in [(0,0,0),(123456789,2,-3),(-987654321,-3,2),(9223372036854775807,10,10)]:
 o=SurfaceOracle(a.eboot,a.libc);o.worldSeed=seed
 ids=[6,37,48,1,166,134,49,165,3,32,163,26,14,140,42,45]*16
 ids=[ids[(x*3+z*5)%256] for z in range(16) for x in range(16)]
 noise=[[-18.,-4.,0.,1.7,2.6,4.,10.,30.][(x+z*3)%8] for x in range(16) for z in range(16)]
 before=[]
 for x in range(16):
  for z in range(16):
   h=40+((x*7+z*11)%80)
   for y in range(128):before.append(1 if y<h else 9 if y<63 else 0)
 o.host.memset(o.buffer,0,65536)
 C.memmove(o.buffer,struct.pack('<32768H',*[b<<4 for b in before]),65536)
 gen=o.alloc(512);o.w(gen+0x90,'Q',o.world);noiseWrapper=o.alloc(32);octaves=o.alloc(8);o.w(gen+0x60,'Q',noiseWrapper);o.w(noiseWrapper+8,'Q',octaves);o.w(noiseWrapper+0x10,'I',1)
 def fill_noise(self,output,size,nx,nz):C.memmove(output,struct.pack('<256d',*noise),2048)
 o.patch(0x627bf0,None,[P,P,U,U,U],fill_noise)
 def biome_name(output,biome,flag):
  o.host.memset(output,0,40);o.w(output+32,'Q',7)
 o.patch(0x85010,None,[P,P,U],biome_name)
 ptrs=o.alloc(256*8)
 for i,id in enumerate(ids):
  b=o.biome_for(id);vt=o.read(b,'Q');entry=0x46aa40 if id in [37,38,39,165,166,167] else 0x6ca1e0 if id in [6,134] else 0x4d7660 if id in [48,49] else 0x2a0240 if id in [3,20,34,131,162] else 0x6d1c50 if id in [5,30,32,33,160,161] else 0x4a2290 if id in [163,164] else 0x855d0
  o.w(vt+0x80,'Q',o.game.base+entry);o.w(ptrs+i*8,'Q',b)
 random_seed=(cx*341873128712+cz*132897987541)&0xffffffffffffffff
 o.call(0x596c70,None,[P,U],[gen+0x18,random_seed])
 o.call(0x508110,None,[P,I,I,P,P],[gen,cx,cz,o.container,ptrs])
 after=[(v>>4)|((v&15)<<16) for v in struct.unpack('<32768H',C.string_at(o.buffer,65536))]
 r=o.call(0x58fd50,D,[P],[gen+0x18]);top_tiles={str(id):o.read(o.biome_for(id)+0x50,'I') for id in set(ids)}
 def biome_at(level,pos):
  x=o.read(pos,'i');z=o.read(pos+8,'i')
  id=ids[(x&15)+(z&15)*16] if x>>4==cx and z>>4==cz else 1
  return o.biome_for(id)
 o.w(o.vt+0xb8,'Q',o.callback(P,[P,P],biome_at))
 for kind in ['underwater_caves','underwater_canyon','caves','canyon']:o.carve(seed,cx,cz,kind)
 carved=[(v>>4)|((v&15)<<16) for v in struct.unpack('<32768H',C.string_at(o.buffer,65536))]
 cases.append({'seed':seed,'chunk':[cx,cz],'biomes':ids,'noise':noise,'before':before,'after':after,'random_after':r,'top_tiles':top_tiles,'carved':carved})
 print('Original full surface buffer',seed,cx,cz,flush=True)

save('ps4-surface-chunks.json.gz',cases)
