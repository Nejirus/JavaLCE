#!/usr/bin/env python3
"""Capture the original block sort, including native equal-key ordering."""
import argparse,gzip,hashlib,json,platform,random
from pathlib import Path
from native_module import Module,C,P,U,I
from nbt_input import read_nbt
parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--eboot',type=Path,required=True);parser.add_argument('--assets',type=Path,required=True);parser.add_argument('--output-dir',type=Path,required=True);a=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):parser.error('Linux x86-64 is required')
digest='c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8'
if hashlib.sha256(a.eboot.read_bytes()).hexdigest()!=digest:parser.error('Unexpected mcps4 executable')
n=Module(a.eboot);host=C.CDLL(None);host.memmove.argtypes=[P,P,U];host.memmove.restype=P
# Import at original insertion-sort memmove PLT 0xe6d0a0.
target=0xe6d0a0+6+n.read(0xe6d0a2,'i');nid=next(k for k,v in n.imports.items() if target in v);n.bind(nid,host.memmove);n.trap_unbound()
cases=[]
def capture(name,positions,ideal):
 blocks=[(C.c_int*3)(*p) for p in positions];pointers=(P*len(blocks))(*[C.addressof(b) for b in blocks]);begin=C.addressof(pointers)
 indices={C.addressof(b):i for i,b in enumerate(blocks)}
 n.function(0x6ff170,None,[P,P,U])(begin,begin+8*len(blocks),ideal)
 cases.append({'name':name,'positions':positions,'ideal':ideal,'order':[indices[p] for p in pointers]})
for entry in json.loads((a.assets/'templates.json').read_text()):
 if not entry['path'].startswith(('coralcrust/','fossils/')):continue
 p=a.assets/'templates'/(entry['path']+'.nbt')
 if hashlib.sha256(p.read_bytes()).hexdigest()!=entry['gzip_sha256']:parser.error('Modified original template '+str(p))
 blocks=[b['pos'] for b in read_nbt(p)['blocks']];capture(entry['path'],blocks,len(blocks))
r=random.Random(1648220)
for size in [0,1,2,3,7,16,31,32,33,40,41,42,63,64,65,128,256,512,1024]:
 for profile in ['random','sorted','reversed','equal','columns','signed']:
  positions=[[r.randrange(16),i,r.randrange(16)] for i in range(size)]
  if profile=='sorted':positions.sort(key=lambda p:(p[0],p[2]))
  if profile=='reversed':positions.sort(key=lambda p:(p[0],p[2]),reverse=True)
  if profile=='equal':positions=[[9,i,7] for i in range(size)]
  if profile=='columns':positions=[[i%3,i,i%5] for i in range(size)]
  if profile=='signed':positions=[[r.choice([-2147483648,-65537,-32768,-1,0,32767,65536,2147483647]),i,r.choice([-65537,-1,0,1,65535,65536])] for i in range(size)]
  for ideal in sorted(set([0,1,size])):capture(profile+'_'+str(size),positions,ideal)
data={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','eboot_sha256':digest,'entrypoints':['0x6ff170','0x6ff5e0','0x6ff840'],
 'input_kind':'Original template block sorting/partition/median/insertion/heap code executes on supplied position structs and pointer vectors. Unchanged asset NBT positions/order and explicit synthetic arrays/ideal budgets are inputs. No Java sort runs here. Original loader 0x6bdc00 invokes this sort with ideal=count. Canonical block states, NBT parsing and placement are outside this isolated comparison.','cases':cases}
a.output_dir.mkdir(parents=True,exist_ok=True);p=a.output_dir/'ps4-template-order.json.gz';p.write_bytes(gzip.compress(json.dumps(data,separators=(',',':')).encode(),mtime=0));print(p.name,len(cases),sum(len(c['order']) for c in cases),hashlib.sha256(p.read_bytes()).hexdigest(),flush=True)
