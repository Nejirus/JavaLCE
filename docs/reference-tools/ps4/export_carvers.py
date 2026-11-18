#!/usr/bin/env python3
"""Capture original mcps4 carver/math vectors. Python 3.12, Linux x86-64.

Only bounded functions run. Module startup and platform services are never
called. World access is supplied by the controlled callbacks below.
"""
from native_carvers import Oracle,C,P,F,D
from pathlib import Path
import argparse,struct,json,hashlib,gzip,zipfile,random,platform,math

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--eboot',required=True,type=Path)
parser.add_argument('--libc',required=True,type=Path)
parser.add_argument('--output-dir',required=True,type=Path)
args=parser.parse_args()
if platform.system()!='Linux' or platform.machine() not in ('x86_64','AMD64'):
 parser.error('The native capture tool requires Linux x86-64')
expected={args.eboot:'c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8',args.libc:'1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816'}
for path,digest in expected.items():
 if hashlib.sha256(path.read_bytes()).hexdigest()!=digest:parser.error(f'Unexpected mcps4 input: {path}')
out=args.output_dir;out.mkdir(parents=True,exist_ok=True)
o=Oracle(args.eboot,args.libc);currentProfile='1'
props={0:(10,-1.,.5,False),1:(11,.125,.8,False),12:(6,.125,0.,True),14:(9,.2,.9,False),24:(10,-1.8,.5,False),26:(0,0.,.05,True),44:(10,-1.,.5,False),49:(10,-1.8,0.,False),163:(13,.3625,1.1,False)}
def biome_at(x,z):
 return [0,1,12,14,24,44,49,1][((x>>4)+3*(z>>4))%8] if currentProfile=='mosaic' else int(currentProfile)
def get_biome(world,p):
 x=o.read(p,'i');z=o.read(p+8,'i');id=biome_at(x,z);family,depth,temp,snow=props[id]
 o.w(o.biome+0x10,'f',depth);o.w(o.biome+0x18,'f',temp);o.w(o.biome+0x20,'B',snow);o.w(o.biome+0x1b0,'f',.125 if id==49 else 0);o.w(o.biome+0x1ac,'i',family);o.w(o.biome+0x50,'i',110 if id==14 else 12 if id==26 else 3 if id==163 else 2)
 return o.biome
o.w(o.vt+0xb8,'Q',o.callback(P,[P,P],get_biome))
cases=[]
for seed,cx,cz,profile,terrain in [(0,0,0,'1','flat'),(-987654321,-1,1,'0','ocean'),(-1,2,-3,'0','ocean'),(82958734,3,2,'14','flat'),(0,0,0,'mosaic','mixed'),(123456789,-1,-1,'mosaic','mixed'),(1,0,0,'12','wet'),(323412,0,0,'49','wet'),(-987654321,-1,1,'12','flat'),(9223372036854775807,2,0,'mosaic','mixed'),(9,0,0,'0','ocean'),(1,0,0,'26','wet')]:
 currentProfile=profile;data=[]
 for x in range(16):
  for z in range(16):
   for y in range(128):
    if terrain=='flat':v=7 if y==0 else 110 if y==69 and profile=='14' else 2 if y==69 else 3 if 66<=y<69 else 1 if y<70 else 0
    elif terrain in ['ocean','wet']:v=7 if y==0 else 1 if y<40 else 9 if y<63 else 0
    else:
     choices=[1,3,12,13,24,78,110,159,172,179,174,49,213,79]
     v=7 if y==0 else choices[((x>>1)+z*3+(y>>2))%len(choices)] if y<101 else 0
     if (x+z+y)%17==0 and y>5:v=0
    data.append(v)
 C.memmove(o.buffer,struct.pack('<32768H',*[v<<4 for v in data]),65536)
 for kind in ['underwater_caves','underwater_canyon','caves','canyon']:o.carve(seed,cx,cz,kind)
 after=bytes(o.output());key=f'{seed}-{cx}-{cz}-{profile}-{terrain}';(out/(key+'-input.bin')).write_bytes(bytes(data));(out/(key+'-native.bin')).write_bytes(after)
 n=sum(a!=b for a,b in zip(data,after));print(key,n,hashlib.sha256(after).hexdigest(),flush=True)
 cases.append({'seed':seed,'chunk':[cx,cz],'biomes':profile,'terrain':terrain,'changed':n,'input':key+'-input.bin','native':key+'-native.bin'})

meta={'repository':'https://github.com/Nejirus/mcps4','commit':'37e258085773f11599cf0ffe29119926769554b7','executable_sha256':expected[args.eboot],'libc_sha256':expected[args.libc],'entrypoints':['0x714f30','0x713fd0','0x3df5b0 (caves vtable)','0x3df5b0 (canyon vtable)'],'input_kind':'Controlled 16x128x16 voxel buffers and biome callbacks, not PS4 world-save captures','cases':cases}
with zipfile.ZipFile(out/'ps4-carvers.zip','w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 def write(name,data):
  info=zipfile.ZipInfo(name);info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
 write('manifest.json',(json.dumps(meta,indent=2)+'\n').encode())
 for c in cases:
  for key in ['input','native']:write(c[key],(out/c[key]).read_bytes())
# Mth's lazy initializer has run through the original cave calls above.
ptr=o.game.read(0x1419850,'Q')
if not ptr:raise RuntimeError('Original Mth table was not initialized')
sine=C.string_at(ptr,65536*4)
if hashlib.sha256(sine).hexdigest()!='a994dcf0a00f927939600f8f242e634741a8e6e544745851a263bb4ee7206051':raise RuntimeError('Original PS4 Mth table changed')
(out/'ps4-sine.f32.gz').write_bytes(gzip.compress(sine,mtime=0))
sin=o.lib.function(0xc73b0,F,[F]);cos=o.lib.function(0xc1770,F,[F])
bits=[0,0x80000000,1,0x80000001,0x007fffff,0x807fffff]
for center in [0x39800000,0x3f490fdb,0x4016cbe4,0x407b53d2,0x40afede0,0x40e231d6,0x4dc90fda]:
 for b in range(center-4,center+5):
  if (b&0x7fffffff)<0x4dc90fdb:bits.extend([b,b|0x80000000])
r=random.Random(233)
for i in range(100000):
 b=r.randrange(0x4dc90fdb);bits.append(b|(r.randrange(2)<<31))
raw=bytearray()
for b in bits:
 x=struct.unpack('<f',struct.pack('<I',b))[0];raw+=struct.pack('<Iff',b,sin(x),cos(x))
(out/'ps4-direct-math.f32.gz').write_bytes(gzip.compress(raw,mtime=0))
cos_double=o.lib.function(0xc13c0,D,[D])
bits=[0,1,0x8000000000000000,0x8000000000000001,0x0010000000000000]
for center in [0x3fe921fc00000000,0x3e46a09d00000000,0x400f6a7b00000000,0x4002d97d00000000,0x4015fdbd00000000,0x401c463c00000000,0x413921fb00000000]:
 for b in range(center-3,center+4):
  if b<0x413921fb00000000:bits.extend([b,b|0x8000000000000000])
for i in range(1,100):
 p=struct.unpack('<Q',struct.pack('<d',i*math.pi/2))[0]
 for b in range(p-2,p+3):bits.extend([b,b|0x8000000000000000])
r=random.Random(233)
for i in range(100000):bits.append(r.randrange(0x413921fb00000000)|(r.randrange(2)<<63))
raw=bytearray()
for b in bits:
 x=struct.unpack('<d',struct.pack('<Q',b))[0];raw+=struct.pack('<Qd',b,cos_double(x))
(out/'ps4-double-cos.f64.gz').write_bytes(gzip.compress(raw,mtime=0))
for name in ['ps4-carvers.zip','ps4-sine.f32.gz','ps4-direct-math.f32.gz','ps4-double-cos.f64.gz']:
 path=out/name;print(name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)

# Canonical ID+metadata buffers expose default-state writes that leave the tile
# ID unchanged. Byte-only fixtures cannot detect these mutations.
state_cases=[]
for seed,cx,cz in [(0,0,0),(1,0,0),(-987654321,-1,1),(123456789,-1,-1),(9223372036854775807,2,0)]:
 currentProfile='163';before=[]
 for x in range(16):
  for z in range(16):
   for y in range(128):
    state=7 if y==0 else 0 if y>=100 else 2 if y%4==3 else (3|((1+(x+z)%2)<<16)) if y%4==2 else 1|(5<<16)
    before.append(state)
 packed=[((state&65535)<<4)|((state>>16)&15) for state in before]
 C.memmove(o.buffer,struct.pack('<32768H',*packed),65536)
 for kind in ['underwater_caves','underwater_canyon','caves','canyon']:o.carve(seed,cx,cz,kind)
 after=[(v>>4)|((v&15)<<16) for v in struct.unpack('<32768H',C.string_at(o.buffer,65536))]
 changed=sum(a!=b and (a&65535)==(b&65535) for a,b in zip(before,after))
 state_cases.append({'seed':seed,'chunk':[cx,cz],'biome':163,'top_tile':3,'before':before,'after':after,'metadata_only_changes':changed})
 print('Original canonical carver buffer',seed,cx,cz,'metadata-only changes',changed,flush=True)
state_meta={k:v for k,v in meta.items() if k!='cases'}
state_meta['input_kind']='Controlled canonical ID/metadata buffers and biome callbacks; not PS4 world saves'
path=out/'ps4-carver-states.json.gz'
path.write_bytes(gzip.compress(json.dumps({**state_meta,'cases':state_cases},separators=(',',':')).encode(),mtime=0))
print(path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
